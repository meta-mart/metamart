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
import { isArray, isEmpty, isEqual, isNil, isString } from 'lodash';
import Qs from 'qs';
import React, {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from 'react';
import {
  Config,
  FieldGroup,
  ImmutableTree,
  JsonTree,
  Utils as QbUtils,
  ValueSource,
} from 'react-awesome-query-builder';
import { useHistory, useParams } from 'react-router-dom';
import { emptyJsonTree } from '../../../constants/AdvancedSearch.constants';
import { EntityType } from '../../../enums/entity.enum';
import { SearchIndex } from '../../../enums/search.enum';
import useCustomLocation from '../../../hooks/useCustomLocation/useCustomLocation';
import { getTypeByFQN } from '../../../rest/metadataTypeAPI';
import advancedSearchClassBase from '../../../utils/AdvancedSearchClassBase';
import { getTierOptions } from '../../../utils/AdvancedSearchUtils';
import { EntitiesSupportedCustomProperties } from '../../../utils/CustomProperties/CustomProperty.utils';
import { elasticSearchFormat } from '../../../utils/QueryBuilderElasticsearchFormatUtils';
import searchClassBase from '../../../utils/SearchClassBase';
import { getEntityTypeFromSearchIndex } from '../../../utils/SearchUtils';
import Loader from '../../common/Loader/Loader';
import { AdvancedSearchModal } from '../AdvanceSearchModal.component';
import { UrlParams } from '../ExplorePage.interface';
import {
  AdvanceSearchContext,
  AdvanceSearchProviderProps,
} from './AdvanceSearchProvider.interface';

const AdvancedSearchContext = React.createContext<AdvanceSearchContext>(
  {} as AdvanceSearchContext
);

export const AdvanceSearchProvider = ({
  children,
  isExplorePage = true,
  modalProps,
  updateURL = true,
}: AdvanceSearchProviderProps) => {
  const tierOptions = useMemo(getTierOptions, []);

  const tabsInfo = useMemo(
    () => searchClassBase.getTabsInfo(),
    [searchClassBase]
  );
  const location = useCustomLocation();
  const history = useHistory();
  const { tab } = useParams<UrlParams>();
  const [loading, setLoading] = useState(true);
  const getSearchIndexFromTabInfo = useCallback(() => {
    const tabInfo = Object.entries(tabsInfo).find(
      ([, tabInfo]) => tabInfo.path === tab
    );
    if (isNil(tabInfo)) {
      return SearchIndex.DATA_ASSET;
    }

    return tabInfo[0] as SearchIndex;
  }, [tabsInfo, tab]);

  const [searchIndex, setSearchIndex] = useState<
    SearchIndex | Array<SearchIndex>
  >(getSearchIndexFromTabInfo());

  const changeSearchIndex = useCallback(
    (index: SearchIndex | Array<SearchIndex>) => {
      setSearchIndex(index);
    },
    []
  );

  const [config, setConfig] = useState<Config>(
    advancedSearchClassBase.getQbConfigs(
      tierOptions,
      isArray(searchIndex) ? searchIndex : [searchIndex],
      isExplorePage
    )
  );
  const [initialised, setInitialised] = useState(false);

  const defaultTree = useMemo(
    () => QbUtils.checkTree(QbUtils.loadTree(emptyJsonTree), config),
    []
  );

  const parsedSearch = useMemo(
    () =>
      Qs.parse(
        location.search.startsWith('?')
          ? location.search.slice(1)
          : location.search
      ),
    [location.search]
  );

  const jsonTree = useMemo(() => {
    if (!isString(parsedSearch.queryFilter)) {
      return undefined;
    }

    try {
      const filter = JSON.parse(parsedSearch.queryFilter);
      const immutableTree = QbUtils.loadTree(filter as JsonTree);
      if (QbUtils.isValidTree(immutableTree)) {
        return filter as JsonTree;
      }
    } catch {
      return undefined;
    }

    return undefined;
  }, [parsedSearch]);

  const [showModal, setShowModal] = useState(false);
  const [treeInternal, setTreeInternal] = useState<ImmutableTree>(() =>
    jsonTree
      ? QbUtils.checkTree(QbUtils.loadTree(jsonTree), config)
      : defaultTree
  );
  const [queryFilter, setQueryFilter] = useState<
    Record<string, unknown> | undefined
  >();
  const [sqlQuery, setSQLQuery] = useState(
    treeInternal ? QbUtils.sqlFormat(treeInternal, config) ?? '' : ''
  );

  useEffect(() => {
    setConfig(
      advancedSearchClassBase.getQbConfigs(
        tierOptions,
        isArray(searchIndex) ? searchIndex : [searchIndex],
        isExplorePage
      )
    );
  }, [searchIndex, isExplorePage]);

  const handleChange = useCallback(
    (nTree, nConfig) => {
      setConfig(nConfig);
      setTreeInternal(nTree);
    },
    [setConfig, setTreeInternal]
  );

  const handleTreeUpdate = useCallback(
    (tree?: ImmutableTree) => {
      history.push({
        pathname: location.pathname,
        search: Qs.stringify({
          ...parsedSearch,
          queryFilter: tree ? JSON.stringify(tree) : undefined,
          page: 1,
        }),
      });
    },
    [history, parsedSearch, location.pathname]
  );

  const toggleModal = (show: boolean) => {
    setShowModal(show);
  };

  const handleReset = useCallback(() => {
    setTreeInternal(QbUtils.checkTree(QbUtils.loadTree(emptyJsonTree), config));
    setQueryFilter(undefined);
    setSQLQuery('');
  }, [config]);

  const handleConfigUpdate = (updatedConfig: Config) => {
    setConfig(updatedConfig);
  };

  // Reset all filters, quick filter and query filter
  const handleResetAllFilters = useCallback(() => {
    setQueryFilter(undefined);
    setSQLQuery('');
    history.push({
      pathname: location.pathname,
      search: Qs.stringify({
        quickFilter: undefined,
        queryFilter: undefined,
        page: 1,
      }),
    });
  }, [history, location.pathname]);

  const fetchCustomPropertyType = async (entityType: EntityType) => {
    const subfields: Record<
      string,
      { type: string; valueSources: ValueSource[] }
    > = {};

    const res = await getTypeByFQN(entityType);
    const customAttributes = res.customProperties;

    if (customAttributes) {
      customAttributes.forEach((attr) => {
        subfields[attr.name] = {
          type: 'text',
          valueSources: ['value'],
        };
      });
    }

    return subfields;
  };

  async function getCustomAttributesSubfields() {
    const subfields: Record<
      string,
      { type: string; valueSources: ValueSource[] }
    > = {};

    try {
      if (isArray(searchIndex)) {
        for await (const index of searchIndex) {
          if (!EntitiesSupportedCustomProperties.includes(index)) {
            continue; // Skip if entity type does not support custom properties
          }

          const entityType = getEntityTypeFromSearchIndex(index);

          if (!entityType) {
            continue; // Skip if entity type is not found
          }

          try {
            const propertyTypes = await fetchCustomPropertyType(entityType);
            Object.assign(subfields, propertyTypes); // Merge the subfields after each API call
          } catch (error) {
            continue; // continue the loop if error occurs in one API call
          }
        }

        return subfields;
      } else {
        if (!EntitiesSupportedCustomProperties.includes(searchIndex)) {
          return subfields;
        }

        const entityType = getEntityTypeFromSearchIndex(searchIndex);
        if (!entityType) {
          return subfields;
        }

        return await fetchCustomPropertyType(entityType);
      }
    } catch (error) {
      // Error
      return subfields;
    }
  }

  const loadData = async () => {
    const actualConfig = advancedSearchClassBase.getQbConfigs(
      tierOptions,
      isArray(searchIndex) ? searchIndex : [searchIndex],
      isExplorePage
    );

    const extensionSubField = await getCustomAttributesSubfields();

    if (!isEmpty(extensionSubField)) {
      (actualConfig.fields.extension as FieldGroup).subfields =
        extensionSubField;
    }

    setConfig(actualConfig);
    setInitialised(true);
  };

  const loadTree = useCallback(
    async (treeObj: JsonTree) => {
      const updatedConfig = config;
      const tree = QbUtils.checkTree(QbUtils.loadTree(treeObj), updatedConfig);

      setTreeInternal(tree);
      const qFilter = {
        query: elasticSearchFormat(tree, updatedConfig),
      };
      if (isEqual(qFilter, queryFilter)) {
        return;
      }

      setQueryFilter(qFilter);
      setSQLQuery(QbUtils.sqlFormat(tree, updatedConfig) ?? '');
    },
    [config, queryFilter]
  );

  useEffect(() => {
    setSearchIndex(getSearchIndexFromTabInfo());
  }, [tabsInfo, tab]);

  useEffect(() => {
    loadData();
  }, [searchIndex]);

  useEffect(() => {
    if (!initialised) {
      return;
    }
    if (jsonTree) {
      loadTree(jsonTree);
    } else {
      handleReset();
    }

    setLoading(false);
  }, [jsonTree, initialised]);

  const handleSubmit = useCallback(() => {
    const qFilter = {
      query: elasticSearchFormat(treeInternal, config),
    };
    setQueryFilter(qFilter);
    setSQLQuery(
      treeInternal ? QbUtils.sqlFormat(treeInternal, config) ?? '' : ''
    );

    updateURL && handleTreeUpdate(treeInternal);
    setShowModal(false);
  }, [treeInternal, config, handleTreeUpdate, updateURL]);

  const contextValues = useMemo(
    () => ({
      queryFilter,
      sqlQuery,
      onTreeUpdate: handleChange,
      toggleModal,
      treeInternal,
      config,
      searchIndex,
      onReset: handleReset,
      onResetAllFilters: handleResetAllFilters,
      onUpdateConfig: handleConfigUpdate,
      onChangeSearchIndex: changeSearchIndex,
      onSubmit: handleSubmit,
      modalProps,
    }),
    [
      queryFilter,
      sqlQuery,
      handleChange,
      toggleModal,
      treeInternal,
      config,
      searchIndex,
      handleReset,
      handleResetAllFilters,
      handleConfigUpdate,
      changeSearchIndex,
      handleSubmit,
      modalProps,
    ]
  );

  return (
    <AdvancedSearchContext.Provider value={contextValues}>
      {loading ? <Loader /> : children}
      <AdvancedSearchModal
        visible={showModal}
        onCancel={() => setShowModal(false)}
        onSubmit={handleSubmit}
      />
    </AdvancedSearchContext.Provider>
  );
};

export const useAdvanceSearch = () => useContext(AdvancedSearchContext);
