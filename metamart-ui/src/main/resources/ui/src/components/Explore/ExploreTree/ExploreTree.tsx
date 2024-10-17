/*
 *  Copyright 2024 DigiTrans.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import { Tree, Typography } from 'antd';
import { AxiosError } from 'axios';
import classNames from 'classnames';
import { isEmpty, isString, isUndefined } from 'lodash';
import Qs from 'qs';
import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { ReactComponent as IconDown } from '../../../assets/svg/ic-arrow-down.svg';
import { ReactComponent as IconRight } from '../../../assets/svg/ic-arrow-right.svg';
import { EntityFields } from '../../../enums/AdvancedSearch.enum';
import { EntityType } from '../../../enums/entity.enum';
import { ExplorePageTabs } from '../../../enums/Explore.enum';
import { SearchIndex } from '../../../enums/search.enum';
import { searchQuery } from '../../../rest/searchAPI';
import { getCountBadge } from '../../../utils/CommonUtils';
import { getPluralizeEntityName } from '../../../utils/EntityUtils';
import {
  getAggregations,
  getQuickFilterObject,
  getQuickFilterObjectForEntities,
  getSubLevelHierarchyKey,
  updateCountsInTreeData,
  updateTreeData,
} from '../../../utils/ExploreUtils';
import searchClassBase from '../../../utils/SearchClassBase';

import { EXPLORE_ROOT_INDEX_MAPPING } from '../../../constants/AdvancedSearch.constants';
import serviceUtilClassBase from '../../../utils/ServiceUtilClassBase';
import { generateUUID } from '../../../utils/StringsUtils';
import { showErrorToast } from '../../../utils/ToastUtils';
import { useAdvanceSearch } from '../AdvanceSearchProvider/AdvanceSearchProvider.component';
import { ExploreSearchIndex, UrlParams } from '../ExplorePage.interface';
import './explore-tree.less';
import {
  ExploreTreeNode,
  ExploreTreeProps,
  TreeNodeData,
} from './ExploreTree.interface';

const ExploreTreeTitle = ({ node }: { node: ExploreTreeNode }) => (
  <div className="d-flex justify-between">
    <Typography.Text
      className={classNames({
        'm-l-xss': node.data?.isRoot,
      })}
      data-testid={`explore-tree-title-${node.data?.dataId ?? node.title}`}>
      {node.title}
    </Typography.Text>
    {!isUndefined(node.count) && (
      <span className="explore-node-count">{getCountBadge(node.count)}</span>
    )}
  </div>
);

const ExploreTree = ({ onFieldValueSelect }: ExploreTreeProps) => {
  const { tab } = useParams<UrlParams>();
  const { onChangeSearchIndex } = useAdvanceSearch();
  const initTreeData = searchClassBase.getExploreTree();
  const staticKeysHavingCounts = searchClassBase.staticKeysHavingCounts();
  const [treeData, setTreeData] = useState(initTreeData);
  const [selectedKeys, setSelectedKeys] = useState<string[]>([]);

  const defaultExpandedKeys = useMemo(() => {
    return searchClassBase.getExploreTreeKey(tab as ExplorePageTabs);
  }, [tab]);

  const [parsedSearch, searchQueryParam, defaultServiceType] = useMemo(() => {
    const parsedSearch = Qs.parse(
      location.search.startsWith('?')
        ? location.search.substring(1)
        : location.search
    );

    const defaultServiceType = parsedSearch.defaultServiceType;

    const searchQueryParam = isString(parsedSearch.search)
      ? parsedSearch.search
      : '';

    return [parsedSearch, searchQueryParam, defaultServiceType];
  }, [location.search]);

  const handleChangeSearchIndex = (
    key: string,
    rootIndex = SearchIndex.DATABASE,
    isRoot = false
  ) => {
    if (isRoot) {
      onChangeSearchIndex(
        EXPLORE_ROOT_INDEX_MAPPING[
          key as keyof typeof EXPLORE_ROOT_INDEX_MAPPING
        ] ?? (key as ExploreSearchIndex)
      );
    } else {
      onChangeSearchIndex(
        EXPLORE_ROOT_INDEX_MAPPING[
          rootIndex as keyof typeof EXPLORE_ROOT_INDEX_MAPPING
        ] ?? rootIndex
      );
    }
  };

  const onLoadData = useCallback(
    async (treeNode: ExploreTreeNode) => {
      try {
        handleChangeSearchIndex(
          treeNode.key,
          treeNode.data?.rootIndex as SearchIndex,
          treeNode.data?.isRoot
        );

        if (treeNode.children) {
          return;
        }

        const {
          isRoot = false,
          currentBucketKey,
          currentBucketValue,
          filterField = [],
          rootIndex,
        } = treeNode?.data as TreeNodeData;

        const searchIndex = isRoot
          ? treeNode.key
          : treeNode?.data?.parentSearchIndex;

        const { bucket: bucketToFind, queryFilter } =
          searchQueryParam !== ''
            ? {
                bucket: EntityFields.ENTITY_TYPE,
                queryFilter: {
                  query: { bool: {} },
                },
              }
            : getSubLevelHierarchyKey(
                rootIndex === SearchIndex.DATABASE,
                treeNode?.data?.filterField,
                currentBucketKey as EntityFields,
                currentBucketValue
              );

        const res = await searchQuery({
          query: searchQueryParam ?? '',
          pageNumber: 0,
          pageSize: 0,
          queryFilter: queryFilter,
          searchIndex: searchIndex as SearchIndex,
          includeDeleted: false,
          trackTotalHits: true,
          fetchSource: false,
        });

        const aggregations = getAggregations(res.aggregations);
        const buckets = aggregations[bucketToFind].buckets.filter(
          (item) =>
            !searchClassBase
              .notIncludeAggregationExploreTree()
              .includes(item.key as EntityType)
        );
        const isServiceType = bucketToFind === EntityFields.SERVICE_TYPE;
        const isEntityType = bucketToFind === EntityFields.ENTITY_TYPE;

        const sortedBuckets = buckets.sort((a, b) =>
          a.key.localeCompare(b.key, undefined, { sensitivity: 'base' })
        );

        const children = sortedBuckets.map((bucket) => {
          const id = generateUUID();

          let logo = undefined;
          if (isEntityType) {
            logo = searchClassBase.getEntityIcon(
              bucket.key,
              'service-icon w-4 h-4'
            ) ?? <></>;
          } else if (isServiceType) {
            const serviceIcon = serviceUtilClassBase.getServiceLogo(bucket.key);
            logo = (
              <img
                alt="logo"
                src={serviceIcon}
                style={{ width: 18, height: 18 }}
              />
            );
          }

          if (bucket.key.toLowerCase() === defaultServiceType) {
            setSelectedKeys([id]);
          }

          return {
            title: isEntityType
              ? getPluralizeEntityName(bucket.key)
              : bucket.key,
            count: isEntityType ? bucket.doc_count : undefined,
            key: id,
            icon: logo,
            isLeaf: bucketToFind === EntityFields.ENTITY_TYPE,
            data: {
              currentBucketKey: bucketToFind,
              parentSearchIndex: isRoot ? treeNode.key : SearchIndex.DATA_ASSET,
              currentBucketValue: bucket.key,
              filterField: [
                ...filterField,
                getQuickFilterObject(bucketToFind, bucket.key),
              ],
              isRoot: false,
              rootIndex: isRoot ? treeNode.key : treeNode.data?.rootIndex,
              dataId: bucket.key,
            },
          };
        });

        setTreeData((origin) => updateTreeData(origin, treeNode.key, children));
      } catch (error) {
        showErrorToast(error as AxiosError);
      }
    },
    [updateTreeData, searchQueryParam, defaultServiceType]
  );

  const switcherIcon = useCallback(({ expanded }) => {
    return expanded ? <IconDown /> : <IconRight />;
  }, []);

  const onNodeSelect = useCallback(
    (_, { node }) => {
      const filterField = node.data?.filterField;
      if (filterField) {
        onFieldValueSelect(filterField);
      } else if (node.isLeaf) {
        const filterField = [
          getQuickFilterObject(EntityFields.ENTITY_TYPE, node.data?.entityType),
        ];
        onFieldValueSelect(filterField);
      } else if (node.data?.childEntities) {
        onFieldValueSelect([
          getQuickFilterObjectForEntities(
            EntityFields.ENTITY_TYPE,
            node.data?.childEntities
          ),
        ]);
      }

      handleChangeSearchIndex(
        node.key,
        node.data?.rootIndex as SearchIndex,
        node.data?.isRoot
      );

      setSelectedKeys([node.key]);
    },
    [onFieldValueSelect]
  );

  const fetchEntityCounts = useCallback(async () => {
    try {
      const res = await searchQuery({
        query: searchQueryParam ?? '',
        pageNumber: 0,
        pageSize: 0,
        queryFilter: {},
        searchIndex: SearchIndex.DATA_ASSET,
        includeDeleted: false,
        trackTotalHits: true,
        fetchSource: false,
      });

      const buckets = res.aggregations['entityType'].buckets;
      const counts: Record<string, number> = {};

      buckets.forEach((item) => {
        counts[item.key] = item.doc_count;
        if (staticKeysHavingCounts.includes(item.key)) {
          setTreeData((origin) =>
            updateCountsInTreeData(origin, item.key, item.doc_count)
          );
        }
      });
    } catch (error) {
      // Do nothing
    }
  }, [staticKeysHavingCounts]);

  useEffect(() => {
    fetchEntityCounts();
  }, []);

  useEffect(() => {
    // Tree works on the quickFilter, so we need to reset the selectedKeys when the quickFilter is empty
    if (isEmpty(parsedSearch.quickFilter)) {
      setSelectedKeys([]);
    }
  }, [parsedSearch]);

  return (
    <Tree
      blockNode
      showIcon
      className="explore-tree p-sm p-t-0"
      data-testid="explore-tree"
      defaultExpandedKeys={defaultExpandedKeys}
      loadData={onLoadData}
      selectedKeys={selectedKeys}
      switcherIcon={switcherIcon}
      titleRender={(node) => <ExploreTreeTitle node={node} />}
      treeData={treeData}
      onSelect={onNodeSelect}
    />
  );
};

export default ExploreTree;
