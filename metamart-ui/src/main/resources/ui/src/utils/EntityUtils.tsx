/*
 *  Copyright 2022 DigiTrans.
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

import { Popover } from 'antd';
import i18next, { t } from 'i18next';
import {
  isEmpty,
  isNil,
  isObject,
  isUndefined,
  lowerCase,
  startCase,
} from 'lodash';
import { Bucket, EntityDetailUnion } from 'Models';
import React, { Fragment } from 'react';
import { Link } from 'react-router-dom';
import { OwnerLabel } from '../components/common/OwnerLabel/OwnerLabel.component';
import QueryCount from '../components/common/QueryCount/QueryCount.component';
import { TitleLink } from '../components/common/TitleBreadcrumb/TitleBreadcrumb.interface';
import { DataAssetsWithoutServiceField } from '../components/DataAssets/DataAssetsHeader/DataAssetsHeader.interface';
import { QueryVoteType } from '../components/Database/TableQueries/TableQueries.interface';
import {
  LeafNodes,
  LineagePos,
} from '../components/Entity/EntityLineage/EntityLineage.interface';
import {
  EntityServiceUnion,
  EntityUnion,
  EntityWithServices,
} from '../components/Explore/ExplorePage.interface';
import {
  SearchedDataProps,
  SourceType,
} from '../components/SearchedData/SearchedData.interface';
import TagsV1 from '../components/Tag/TagsV1/TagsV1.component';
import { FQN_SEPARATOR_CHAR } from '../constants/char.constants';
import {
  getBotsPagePath,
  getBotsPath,
  getEntityDetailsPath,
  getGlossaryTermDetailsPath,
  getKpiPath,
  getServiceDetailsPath,
  getTagsDetailsPath,
  NO_DATA,
  ROUTES,
} from '../constants/constants';
import {
  GlobalSettingOptions,
  GlobalSettingsMenuCategory,
} from '../constants/GlobalSettings.constants';
import { TAG_START_WITH } from '../constants/Tag.constants';
import { ResourceEntity } from '../context/PermissionProvider/PermissionProvider.interface';
import { EntityTabs, EntityType, FqnPart } from '../enums/entity.enum';
import { ExplorePageTabs } from '../enums/Explore.enum';
import { SearchIndex } from '../enums/search.enum';
import { ServiceCategory, ServiceCategoryPlural } from '../enums/service.enum';
import { PrimaryTableDataTypes } from '../enums/table.enum';
import { Kpi } from '../generated/dataInsight/kpi/kpi';
import { Classification } from '../generated/entity/classification/classification';
import { APICollection } from '../generated/entity/data/apiCollection';
import { APIEndpoint } from '../generated/entity/data/apiEndpoint';
import { Chart } from '../generated/entity/data/chart';
import { Container } from '../generated/entity/data/container';
import { Dashboard } from '../generated/entity/data/dashboard';
import { DashboardDataModel } from '../generated/entity/data/dashboardDataModel';
import { Database } from '../generated/entity/data/database';
import { DatabaseSchema } from '../generated/entity/data/databaseSchema';
import { GlossaryTerm } from '../generated/entity/data/glossaryTerm';
import { Metric } from '../generated/entity/data/metric';
import { Mlmodel } from '../generated/entity/data/mlmodel';
import { Pipeline } from '../generated/entity/data/pipeline';
import {
  SearchIndex as SearchIndexAsset,
  SearchIndex as SearchIndexEntity,
  SearchIndexField,
} from '../generated/entity/data/searchIndex';
import {
  StoredProcedure,
  StoredProcedureCodeObject,
} from '../generated/entity/data/storedProcedure';
import {
  Column,
  ColumnJoins,
  JoinedWith,
  Table,
  TableType,
} from '../generated/entity/data/table';
import { Topic } from '../generated/entity/data/topic';
import { DataProduct } from '../generated/entity/domains/dataProduct';
import { Team } from '../generated/entity/teams/team';
import {
  AlertType,
  EventSubscription,
} from '../generated/events/eventSubscription';
import { TestCase, TestSuite } from '../generated/tests/testCase';
import { Edge, EntityLineage } from '../generated/type/entityLineage';
import { EntityReference } from '../generated/type/entityUsage';
import { TagLabel } from '../generated/type/tagLabel';
import { UsageDetails } from '../generated/type/usageDetails';
import { Votes } from '../generated/type/votes';
import { DataInsightTabs } from '../interface/data-insight.interface';
import { SearchSourceAlias } from '../interface/search.interface';
import { DataQualityPageTabs } from '../pages/DataQuality/DataQualityPage.interface';
import {
  getPartialNameFromTableFQN,
  getTableFQNFromColumnFQN,
} from './CommonUtils';
import { getDataInsightPathWithFqn } from './DataInsightUtils';
import EntityLink from './EntityLink';
import { BasicEntityOverviewInfo } from './EntityUtils.interface';
import Fqn from './Fqn';
import {
  getApplicationDetailsPath,
  getDataQualityPagePath,
  getDomainDetailsPath,
  getDomainPath,
  getGlossaryPath,
  getIncidentManagerDetailPagePath,
  getNotificationAlertDetailsPath,
  getObservabilityAlertDetailsPath,
  getPersonaDetailsPath,
  getPolicyWithFqnPath,
  getRoleWithFqnPath,
  getSettingPath,
  getTeamsWithFqnPath,
} from './RouterUtils';
import { getServiceRouteFromServiceType } from './ServiceUtils';
import { stringToHTML } from './StringsUtils';
import {
  getDataTypeString,
  getTagsWithoutTier,
  getTierTags,
  getUsagePercentile,
} from './TableUtils';
import { getTableTags } from './TagsUtils';

export enum DRAWER_NAVIGATION_OPTIONS {
  explore = 'Explore',
  lineage = 'Lineage',
}

/**
 * Take entity reference as input and return name for entity
 * @param entity - entity reference
 * @returns - entity name
 */
export const getEntityName = (entity?: {
  name?: string;
  displayName?: string;
}) => {
  return entity?.displayName || entity?.name || '';
};

export const getEntityId = (entity?: { id?: string }) => entity?.id ?? '';

export const getEntityTags = (
  type: string,
  entityDetail: EntityDetailUnion
): Array<TagLabel> => {
  switch (type) {
    case EntityType.TABLE: {
      const tableTags: Array<TagLabel> = [
        ...getTableTags((entityDetail as Table).columns || []),
        ...(entityDetail.tags || []),
      ];

      return tableTags;
    }
    case EntityType.DASHBOARD:
    case EntityType.SEARCH_INDEX:
    case EntityType.PIPELINE:
      return getTagsWithoutTier(entityDetail.tags || []);

    case EntityType.TOPIC:
    case EntityType.MLMODEL:
    case EntityType.STORED_PROCEDURE:
    case EntityType.DASHBOARD_DATA_MODEL: {
      return entityDetail.tags || [];
    }

    default:
      return [];
  }
};

const entityTierRenderer = (tier?: TagLabel) => {
  return tier ? (
    <TagsV1 startWith={TAG_START_WITH.SOURCE_ICON} tag={tier} />
  ) : (
    NO_DATA
  );
};

const getUsageData = (usageSummary: UsageDetails | undefined) =>
  !isNil(usageSummary?.weeklyStats?.percentileRank)
    ? getUsagePercentile(usageSummary?.weeklyStats?.percentileRank ?? 0)
    : NO_DATA;

const getTableFieldsFromTableDetails = (tableDetails: Table) => {
  const {
    fullyQualifiedName,
    owners,
    tags,
    usageSummary,
    profile,
    columns,
    tableType,
    service,
    database,
    databaseSchema,
  } = tableDetails;
  const [serviceName, databaseName, schemaName] = getPartialNameFromTableFQN(
    fullyQualifiedName ?? '',
    [FqnPart.Service, FqnPart.Database, FqnPart.Schema],
    FQN_SEPARATOR_CHAR
  ).split(FQN_SEPARATOR_CHAR);

  const serviceDisplayName = getEntityName(service) || serviceName;
  const databaseDisplayName = getEntityName(database) || databaseName;
  const schemaDisplayName = getEntityName(databaseSchema) || schemaName;

  const tier = getTierTags(tags ?? []);

  return {
    fullyQualifiedName,
    owners,
    service: serviceDisplayName,
    database: databaseDisplayName,
    schema: schemaDisplayName,
    tier,
    usage: getUsageData(usageSummary),
    profile,
    columns,
    tableType,
  };
};

const getTableOverview = (
  tableDetails: Table,
  additionalInfo?: Record<string, number | string>
) => {
  const {
    fullyQualifiedName,
    owners,
    profile,
    columns,
    tableType,
    service,
    database,
    schema,
    tier,
    usage,
  } = getTableFieldsFromTableDetails(tableDetails);

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.type'),
      value: tableType ?? TableType.Regular,
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.service'),
      value: service || NO_DATA,
      url: getServiceDetailsPath(service, ServiceCategory.DATABASE_SERVICES),
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.database'),
      value: database || NO_DATA,
      url: getEntityDetailsPath(
        EntityType.DATABASE,
        getPartialNameFromTableFQN(
          fullyQualifiedName ?? '',
          [FqnPart.Service, FqnPart.Database],
          FQN_SEPARATOR_CHAR
        )
      ),
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.schema'),
      value: schema || NO_DATA,
      url: getEntityDetailsPath(
        EntityType.DATABASE_SCHEMA,
        getPartialNameFromTableFQN(
          fullyQualifiedName ?? '',
          [FqnPart.Service, FqnPart.Database, FqnPart.Schema],
          FQN_SEPARATOR_CHAR
        )
      ),
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.tier'),
      value: entityTierRenderer(tier),
      isLink: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.usage'),
      value: usage || NO_DATA,
      isLink: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.query-plural'),
      value: <QueryCount tableId={tableDetails.id || ''} />,
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.column-plural'),
      value: columns ? columns.length : NO_DATA,
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.row-plural'),
      value:
        !isUndefined(profile) && profile?.rowCount ? profile.rowCount : NO_DATA,
      isLink: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.incident-plural'),
      value: additionalInfo?.incidentCount ?? 0,
      isLink: true,
      url: getEntityDetailsPath(
        EntityType.TABLE,
        fullyQualifiedName ?? '',
        EntityTabs.INCIDENTS
      ),
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
  ];

  return overview;
};

const getPipelineOverview = (pipelineDetails: Pipeline) => {
  const { owners, tags, sourceUrl, service, displayName } = pipelineDetails;
  const tier = getTierTags(tags ?? []);
  const serviceDisplayName = getEntityName(service);

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: `${i18next.t('label.pipeline')} ${i18next.t(
        'label.url-uppercase'
      )}`,
      dataTestId: 'pipeline-url-label',
      value: stringToHTML(displayName ?? '') || NO_DATA,
      url: sourceUrl,
      isLink: true,
      isExternal: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.service'),
      value: serviceDisplayName || NO_DATA,
      url: getServiceDetailsPath(
        service?.name ?? '',
        ServiceCategory.PIPELINE_SERVICES
      ),
      isLink: true,
      isExternal: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.tier'),
      value: entityTierRenderer(tier),
      isLink: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
  ];

  return overview;
};

const getDashboardOverview = (dashboardDetails: Dashboard) => {
  const { owners, tags, sourceUrl, service, displayName, project } =
    dashboardDetails;
  const tier = getTierTags(tags ?? []);
  const serviceDisplayName = getEntityName(service);

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: `${i18next.t('label.dashboard')} ${i18next.t(
        'label.url-uppercase'
      )}`,
      value: stringToHTML(displayName ?? '') || NO_DATA,
      url: sourceUrl,
      isLink: true,
      isExternal: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.service'),
      value: serviceDisplayName || NO_DATA,
      url: getServiceDetailsPath(
        service?.name ?? '',
        ServiceCategory.DASHBOARD_SERVICES
      ),
      isExternal: false,
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.tier'),
      value: entityTierRenderer(tier),
      isLink: false,
      isExternal: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.project'),
      value: project ?? NO_DATA,
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.explore,
        DRAWER_NAVIGATION_OPTIONS.lineage,
      ],
    },
  ];

  return overview;
};

export const getSearchIndexOverview = (
  searchIndexDetails: SearchIndexEntity
) => {
  const { owners, tags, service } = searchIndexDetails;
  const tier = getTierTags(tags ?? []);

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.tier'),
      value: entityTierRenderer(tier),
      isLink: false,
      isExternal: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.service'),
      value: service?.fullyQualifiedName ?? NO_DATA,
      url: getServiceDetailsPath(
        service?.name ?? '',
        ServiceCategory.SEARCH_SERVICES
      ),
      isExternal: false,
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
  ];

  return overview;
};

const getMlModelOverview = (mlModelDetails: Mlmodel) => {
  const { algorithm, target, server, dashboard, owners } = mlModelDetails;

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.algorithm'),
      value: algorithm || NO_DATA,
      url: '',
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.target'),
      value: target ?? NO_DATA,
      url: '',
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.server'),
      value: server ?? NO_DATA,
      url: server,
      isLink: Boolean(server),
      isExternal: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.dashboard'),
      value: getEntityName(dashboard) || NO_DATA,
      url: getEntityDetailsPath(
        EntityType.DASHBOARD,
        dashboard?.fullyQualifiedName ?? ''
      ),
      isLink: true,
      isExternal: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
  ];

  return overview;
};

const getContainerOverview = (containerDetails: Container) => {
  const { numberOfObjects, serviceType, dataModel } = containerDetails;

  const visible = [
    DRAWER_NAVIGATION_OPTIONS.lineage,
    DRAWER_NAVIGATION_OPTIONS.explore,
  ];

  const overview = [
    {
      name: i18next.t('label.object-plural'),
      value: numberOfObjects,
      isLink: false,
      visible,
    },
    {
      name: i18next.t('label.service-type'),
      value: serviceType,
      isLink: false,
      visible,
    },
    {
      name: i18next.t('label.column-plural'),
      value:
        !isUndefined(dataModel) && dataModel.columns
          ? dataModel.columns.length
          : NO_DATA,
      isLink: false,
      visible,
    },
  ];

  return overview;
};

const getChartOverview = (chartDetails: Chart) => {
  const { owners, sourceUrl, chartType, service, serviceType, displayName } =
    chartDetails;
  const serviceDisplayName = getEntityName(service);

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: `${i18next.t('label.chart')} ${i18next.t('label.url-uppercase')}`,
      value: stringToHTML(displayName ?? '') || NO_DATA,
      url: sourceUrl,
      isLink: true,
      isExternal: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.service'),
      value: serviceDisplayName || NO_DATA,
      url: getServiceDetailsPath(
        service?.name ?? '',
        ServiceCategory.DASHBOARD_SERVICES
      ),
      isExternal: false,
      isLink: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.chart-type'),
      value: chartType ?? NO_DATA,
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.explore,
        DRAWER_NAVIGATION_OPTIONS.lineage,
      ],
    },
    {
      name: i18next.t('label.service-type'),
      value: serviceType ?? NO_DATA,
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.explore,
        DRAWER_NAVIGATION_OPTIONS.lineage,
      ],
    },
  ];

  return overview;
};

const getDataModelOverview = (dataModelDetails: DashboardDataModel) => {
  const {
    owners,
    tags,
    service,
    displayName,
    dataModelType,
    fullyQualifiedName,
  } = dataModelDetails;
  const tier = getTierTags(tags ?? []);

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: `${i18next.t('label.data-model')} ${i18next.t(
        'label.url-uppercase'
      )}`,
      value: stringToHTML(displayName ?? '') || NO_DATA,
      url: getEntityDetailsPath(
        EntityType.DASHBOARD_DATA_MODEL,
        fullyQualifiedName ?? ''
      ),
      isLink: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.service'),
      value: service?.fullyQualifiedName ?? NO_DATA,
      url: getServiceDetailsPath(
        service?.name ?? '',
        ServiceCategory.DASHBOARD_SERVICES
      ),
      isExternal: false,
      isLink: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },

    {
      name: i18next.t('label.tier'),
      value: entityTierRenderer(tier),
      isLink: false,
      isExternal: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.data-model-type'),
      value: dataModelType,
      isLink: false,
      isExternal: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
  ];

  return overview;
};

const getStoredProcedureOverview = (
  storedProcedureDetails: StoredProcedure
) => {
  const { fullyQualifiedName, owners, tags, storedProcedureCode } =
    storedProcedureDetails;
  const [service, database, schema] = getPartialNameFromTableFQN(
    fullyQualifiedName ?? '',
    [FqnPart.Service, FqnPart.Database, FqnPart.Schema],
    FQN_SEPARATOR_CHAR
  ).split(FQN_SEPARATOR_CHAR);

  const tier = getTierTags(tags ?? []);

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.service'),
      value: service || NO_DATA,
      url: getServiceDetailsPath(service, ServiceCategory.DATABASE_SERVICES),
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.database'),
      value: database || NO_DATA,
      url: getEntityDetailsPath(
        EntityType.DATABASE,
        getPartialNameFromTableFQN(
          fullyQualifiedName ?? '',
          [FqnPart.Service, FqnPart.Database],
          FQN_SEPARATOR_CHAR
        )
      ),
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.lineage],
    },
    {
      name: i18next.t('label.schema'),
      value: schema || NO_DATA,
      url: getEntityDetailsPath(
        EntityType.DATABASE_SCHEMA,
        getPartialNameFromTableFQN(
          fullyQualifiedName ?? '',
          [FqnPart.Service, FqnPart.Database, FqnPart.Schema],
          FQN_SEPARATOR_CHAR
        )
      ),
      isLink: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    {
      name: i18next.t('label.tier'),
      value: entityTierRenderer(tier),
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.lineage,
        DRAWER_NAVIGATION_OPTIONS.explore,
      ],
    },
    ...(isObject(storedProcedureCode)
      ? [
          {
            name: i18next.t('label.language'),
            value:
              (storedProcedureCode as StoredProcedureCodeObject).language ??
              NO_DATA,
            isLink: false,
            visible: [
              DRAWER_NAVIGATION_OPTIONS.lineage,
              DRAWER_NAVIGATION_OPTIONS.explore,
            ],
          },
        ]
      : []),
  ];

  return overview;
};

const getDatabaseOverview = (databaseDetails: Database) => {
  const { owners, service, tags, usageSummary } = databaseDetails;

  const tier = getTierTags(tags ?? []);

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
    {
      name: i18next.t('label.tier'),
      value: entityTierRenderer(tier),
      isLink: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
    {
      name: i18next.t('label.service'),
      value: service.fullyQualifiedName || NO_DATA,
      url: getServiceDetailsPath(
        service.fullyQualifiedName ?? '',
        ServiceCategory.DATABASE_SERVICES
      ),
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },

    {
      name: i18next.t('label.usage'),
      value: getUsageData(usageSummary),
      isLink: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
  ];

  return overview;
};

const getDatabaseSchemaOverview = (databaseSchemaDetails: DatabaseSchema) => {
  const { owners, service, tags, usageSummary, database } =
    databaseSchemaDetails;

  const tier = getTierTags(tags ?? []);

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },

    {
      name: i18next.t('label.tier'),
      value: entityTierRenderer(tier),
      isLink: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
    {
      name: i18next.t('label.service'),
      value: service.fullyQualifiedName ?? NO_DATA,
      url: getServiceDetailsPath(
        service.fullyQualifiedName ?? '',
        ServiceCategory.DATABASE_SERVICES
      ),
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
    {
      name: i18next.t('label.database'),
      value: database.fullyQualifiedName ?? NO_DATA,
      url: getEntityDetailsPath(
        EntityType.DATABASE,
        database.fullyQualifiedName ?? ''
      ),
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
    {
      name: i18next.t('label.usage'),
      value: getUsageData(usageSummary),
      isLink: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
  ];

  return overview;
};

const getEntityServiceOverview = (serviceDetails: EntityServiceUnion) => {
  const { owners, tags, serviceType } = serviceDetails;

  const tier = getTierTags(tags ?? []);

  const overview = [
    {
      name: i18next.t('label.owner-plural'),
      value: <OwnerLabel hasPermission={false} owners={owners} />,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
    {
      name: i18next.t('label.tier'),
      value: entityTierRenderer(tier),
      isLink: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
    {
      name: i18next.t('label.service-type'),
      value: serviceType,
      isLink: false,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
  ];

  return overview;
};

const getApiCollectionOverview = (apiCollection: APICollection) => {
  if (isNil(apiCollection) || isEmpty(apiCollection)) {
    return [];
  }

  const { service } = apiCollection;

  const overview = [
    {
      name: i18next.t('label.endpoint-url'),
      value: apiCollection.endpointURL || NO_DATA,
      url: apiCollection.endpointURL,
      isLink: true,
      isExternal: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
    {
      name: i18next.t('label.service'),
      value: service.fullyQualifiedName ?? NO_DATA,
      url: getServiceDetailsPath(
        service.fullyQualifiedName ?? '',
        ServiceCategory.API_SERVICES
      ),
      isLink: true,
      visible: [DRAWER_NAVIGATION_OPTIONS.explore],
    },
  ];

  return overview;
};
const getApiEndpointOverview = (apiEndpoint: APIEndpoint) => {
  if (isNil(apiEndpoint) || isEmpty(apiEndpoint)) {
    return [];
  }
  const { service, apiCollection } = apiEndpoint;

  const overview = [
    {
      name: i18next.t('label.endpoint-url'),
      value: apiEndpoint.endpointURL || NO_DATA,
      url: apiEndpoint.endpointURL,
      isLink: true,
      isExternal: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.explore,
        DRAWER_NAVIGATION_OPTIONS.lineage,
      ],
    },
    {
      name: i18next.t('label.api-collection'),
      value: apiEndpoint.apiCollection?.fullyQualifiedName ?? '',
      url: getEntityDetailsPath(
        EntityType.API_COLLECTION,
        apiCollection?.fullyQualifiedName ?? ''
      ),
      isLink: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.explore,
        DRAWER_NAVIGATION_OPTIONS.lineage,
      ],
    },
    {
      name: i18next.t('label.service'),
      value: service.fullyQualifiedName ?? '',
      url: getServiceDetailsPath(
        service.fullyQualifiedName ?? '',
        ServiceCategory.API_SERVICES
      ),
      isLink: true,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.explore,
        DRAWER_NAVIGATION_OPTIONS.lineage,
      ],
    },
    {
      name: i18next.t('label.request-method'),
      value: apiEndpoint.requestMethod || NO_DATA,
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.explore,
        DRAWER_NAVIGATION_OPTIONS.lineage,
      ],
    },
  ];

  return overview;
};
const getMetricOverview = (metric: Metric) => {
  if (isNil(metric) || isEmpty(metric)) {
    return [];
  }

  const overview = [
    {
      name: i18next.t('label.metric-type'),
      value: metric.metricType || NO_DATA,
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.explore,
        DRAWER_NAVIGATION_OPTIONS.lineage,
      ],
    },
    {
      name: i18next.t('label.unit-of-measurement'),
      value: metric.unitOfMeasurement || NO_DATA,
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.explore,
        DRAWER_NAVIGATION_OPTIONS.lineage,
      ],
    },
    {
      name: i18next.t('label.granularity'),
      value: metric.granularity || NO_DATA,
      isLink: false,
      visible: [
        DRAWER_NAVIGATION_OPTIONS.explore,
        DRAWER_NAVIGATION_OPTIONS.lineage,
      ],
    },
  ];

  return overview;
};

export const getEntityOverview = (
  type: string,
  entityDetail: EntityUnion,
  additionalInfo?: Record<string, number | string>
): Array<BasicEntityOverviewInfo> => {
  switch (type) {
    case ExplorePageTabs.TABLES: {
      return getTableOverview(entityDetail as Table, additionalInfo);
    }

    case ExplorePageTabs.PIPELINES: {
      return getPipelineOverview(entityDetail as Pipeline);
    }

    case ExplorePageTabs.DASHBOARDS: {
      return getDashboardOverview(entityDetail as Dashboard);
    }

    case ExplorePageTabs.SEARCH_INDEX: {
      return getSearchIndexOverview(entityDetail as SearchIndexEntity);
    }

    case ExplorePageTabs.MLMODELS: {
      return getMlModelOverview(entityDetail as Mlmodel);
    }
    case ExplorePageTabs.CONTAINERS: {
      return getContainerOverview(entityDetail as Container);
    }
    case ExplorePageTabs.CHARTS: {
      return getChartOverview(entityDetail as Chart);
    }

    case ExplorePageTabs.DASHBOARD_DATA_MODEL: {
      return getDataModelOverview(entityDetail as DashboardDataModel);
    }

    case ExplorePageTabs.STORED_PROCEDURE: {
      return getStoredProcedureOverview(entityDetail as StoredProcedure);
    }

    case ExplorePageTabs.DATABASE: {
      return getDatabaseOverview(entityDetail as Database);
    }

    case ExplorePageTabs.DATABASE_SCHEMA: {
      return getDatabaseSchemaOverview(entityDetail as DatabaseSchema);
    }

    case ExplorePageTabs.API_COLLECTION: {
      return getApiCollectionOverview(entityDetail as APICollection);
    }

    case ExplorePageTabs.API_ENDPOINT: {
      return getApiEndpointOverview(entityDetail as APIEndpoint);
    }

    case ExplorePageTabs.METRIC: {
      return getMetricOverview(entityDetail as Metric);
    }

    case ExplorePageTabs.DATABASE_SERVICE:
    case ExplorePageTabs.MESSAGING_SERVICE:
    case ExplorePageTabs.DASHBOARD_SERVICE:
    case ExplorePageTabs.ML_MODEL_SERVICE:
    case ExplorePageTabs.PIPELINE_SERVICE:
    case ExplorePageTabs.SEARCH_INDEX_SERVICE:
    case ExplorePageTabs.API_SERVICE: {
      return getEntityServiceOverview(entityDetail as EntityServiceUnion);
    }

    default:
      return [];
  }
};

// Note: This method is enhanced from "getEntityCountByService" of ServiceUtils.ts
export const getEntityCountByType = (buckets: Array<Bucket>) => {
  const entityCounts = {
    tableCount: 0,
    topicCount: 0,
    dashboardCount: 0,
    pipelineCount: 0,
  };
  buckets?.forEach((bucket) => {
    switch (bucket.key) {
      case EntityType.TABLE:
        entityCounts.tableCount += bucket.doc_count;

        break;
      case EntityType.TOPIC:
        entityCounts.topicCount += bucket.doc_count;

        break;
      case EntityType.DASHBOARD:
        entityCounts.dashboardCount += bucket.doc_count;

        break;
      case EntityType.PIPELINE:
        entityCounts.pipelineCount += bucket.doc_count;

        break;
      default:
        break;
    }
  });

  return entityCounts;
};

export const getTotalEntityCountByType = (buckets: Array<Bucket> = []) => {
  let entityCounts = 0;
  buckets.forEach((bucket) => {
    entityCounts += bucket.doc_count;
  });

  return entityCounts;
};

export const getEntityLineage = (
  oldVal: EntityLineage,
  newVal: EntityLineage,
  pos: LineagePos
) => {
  if (pos === 'to') {
    const downEdges = newVal.downstreamEdges;
    const newNodes = newVal.nodes?.filter((n) =>
      downEdges?.find((e) => e.toEntity === n.id)
    );

    return {
      ...oldVal,
      downstreamEdges: [
        ...(oldVal.downstreamEdges as Edge[]),
        ...(downEdges as Edge[]),
      ],
      nodes: [
        ...(oldVal.nodes as EntityReference[]),
        ...(newNodes as EntityReference[]),
      ],
    };
  } else {
    const upEdges = newVal.upstreamEdges;
    const newNodes = newVal.nodes?.filter((n) =>
      upEdges?.find((e) => e.fromEntity === n.id)
    );

    return {
      ...oldVal,
      upstreamEdges: [
        ...(oldVal.upstreamEdges as Edge[]),
        ...(upEdges as Edge[]),
      ],
      nodes: [
        ...(oldVal.nodes as EntityReference[]),
        ...(newNodes as EntityReference[]),
      ],
    };
  }
};

export const isLeafNode = (
  leafNodes: LeafNodes,
  id: string,
  pos: LineagePos
) => {
  if (!isEmpty(leafNodes)) {
    return pos === 'from'
      ? leafNodes.upStreamNode?.includes(id)
      : leafNodes.downStreamNode?.includes(id);
  } else {
    return false;
  }
};

export const ENTITY_LINK_SEPARATOR = '::';

export const getEntityFeedLink = (
  type?: string,
  fqn?: string,
  field?: string
): string => {
  if (isUndefined(type) || isUndefined(fqn)) {
    return '';
  }

  return `<#E${ENTITY_LINK_SEPARATOR}${type}${ENTITY_LINK_SEPARATOR}${fqn}${
    field ? `${ENTITY_LINK_SEPARATOR}${field}` : ''
  }>`;
};

/*
  params: userName - fullyQualifiedName
  return : <#E::user::userName>
*/
export const getEntityUserLink = (userName: string): string => {
  return `<#E${ENTITY_LINK_SEPARATOR}user${ENTITY_LINK_SEPARATOR}${userName}>`;
};

export const isSupportedTest = (dataType: string) => {
  return dataType === 'ARRAY' || dataType === 'STRUCT';
};

export const isColumnTestSupported = (dataType: string) => {
  const supportedType = Object.values(PrimaryTableDataTypes);

  return supportedType.includes(
    getDataTypeString(dataType) as PrimaryTableDataTypes
  );
};

export const getTitleCase = (text?: string) => {
  return text ? startCase(text) : '';
};

export const getResourceEntityFromEntityType = (entityType: string) => {
  switch (entityType) {
    case EntityType.TABLE:
    case SearchIndex.TABLE:
      return ResourceEntity.TABLE;

    case EntityType.TOPIC:
    case SearchIndex.TOPIC:
      return ResourceEntity.TOPIC;

    case EntityType.DASHBOARD:
    case SearchIndex.DASHBOARD:
      return ResourceEntity.DASHBOARD;

    case EntityType.PIPELINE:
    case SearchIndex.PIPELINE:
      return ResourceEntity.PIPELINE;

    case EntityType.MLMODEL:
    case SearchIndex.MLMODEL:
      return ResourceEntity.ML_MODEL;
  }

  return ResourceEntity.ALL;
};

/**
 * It searches for a given text in a given table and returns a new table with only the columns that
 * contain the given text
 * @param {Column[]} table - Column[] - the table to search in
 * @param {string} searchText - The text to search for.
 * @returns An array of columns that have been searched for a specific string.
 */
export const searchInColumns = <T extends Column | SearchIndexField>(
  table: T[],
  searchText: string
): T[] => {
  const searchedValue: T[] = table.reduce((searchedCols, column) => {
    const searchLowerCase = lowerCase(searchText);
    const isContainData =
      lowerCase(column.name).includes(searchLowerCase) ||
      lowerCase(column.displayName).includes(searchLowerCase) ||
      lowerCase(column.description).includes(searchLowerCase) ||
      lowerCase(getDataTypeString(column.dataType)).includes(searchLowerCase);

    if (isContainData) {
      return [...searchedCols, column];
    } else if (!isUndefined(column.children)) {
      const searchedChildren = searchInColumns<T>(
        column.children as T[],
        searchText
      );
      if (searchedChildren.length > 0) {
        return [
          ...searchedCols,
          {
            ...column,
            children: searchedChildren,
          },
        ];
      }
    }

    return searchedCols;
  }, [] as T[]);

  return searchedValue;
};

/**
 * It checks if a column has a join
 * @param {string} columnName - The name of the column you want to check if joins are available for.
 * @param joins - Array<ColumnJoins>
 * @returns A boolean value.
 */
export const checkIfJoinsAvailable = (
  columnName: string,
  joins: Array<ColumnJoins>
): boolean => {
  return (
    joins &&
    Boolean(joins.length) &&
    Boolean(joins.find((join) => join.columnName === columnName))
  );
};

/**
 * It takes a column name and a list of joins and returns the list of joinedWith for the column name
 * @param {string} columnName - The name of the column you want to get the frequently joined with
 * columns for.
 * @param joins - Array<ColumnJoins>
 * @returns An array of joinedWith objects
 */
export const getFrequentlyJoinedWithColumns = (
  columnName: string,
  joins: Array<ColumnJoins>
): Array<JoinedWith> => {
  return (
    (joins &&
      Boolean(joins.length) &&
      joins?.find((join) => join.columnName === columnName)?.joinedWith) ||
    []
  );
};

export const getFrequentlyJoinedColumns = (
  columnName: string,
  joins: Array<ColumnJoins>,
  columnLabel: string
) => {
  const frequentlyJoinedWithColumns = getFrequentlyJoinedWithColumns(
    columnName,
    joins
  );

  return checkIfJoinsAvailable(columnName, joins) ? (
    <div className="m-t-sm" data-testid="frequently-joined-columns">
      <span className="text-grey-muted m-r-xss">{columnLabel}:</span>
      <span>
        {frequentlyJoinedWithColumns.slice(0, 3).map((columnJoin, index) => (
          <Fragment key={columnJoin.fullyQualifiedName}>
            {index > 0 && <span className="m-r-xss">,</span>}
            <Link
              className="link-text"
              to={getEntityDetailsPath(
                EntityType.TABLE,
                columnJoin.fullyQualifiedName
              )}>
              {getPartialNameFromTableFQN(
                columnJoin.fullyQualifiedName,
                [
                  FqnPart.Database,
                  FqnPart.Table,
                  FqnPart.Schema,
                  FqnPart.Column,
                ],
                FQN_SEPARATOR_CHAR
              )}
            </Link>
          </Fragment>
        ))}

        {frequentlyJoinedWithColumns.length > 3 && (
          <Popover
            content={
              <div className="text-left">
                {frequentlyJoinedWithColumns?.slice(3).map((columnJoin) => (
                  <Fragment key={columnJoin.fullyQualifiedName}>
                    <a
                      className="link-text d-block p-y-xss"
                      href={getEntityDetailsPath(
                        EntityType.TABLE,
                        getTableFQNFromColumnFQN(
                          columnJoin?.fullyQualifiedName
                        ),
                        getPartialNameFromTableFQN(
                          columnJoin?.fullyQualifiedName,
                          [FqnPart.Column]
                        )
                      )}>
                      {getPartialNameFromTableFQN(
                        columnJoin?.fullyQualifiedName,
                        [FqnPart.Database, FqnPart.Table, FqnPart.Column]
                      )}
                    </a>
                  </Fragment>
                ))}
              </div>
            }
            placement="bottom"
            trigger="click">
            <span className="show-more m-l-xss text-underline">...</span>
          </Popover>
        )}
      </span>
    </div>
  ) : null;
};

/**
 * Convert entity to EntityReference
 * @param entities -- T extends EntityReference
 * @param type -- EntityType
 * @returns EntityReference
 */
export const getEntityReferenceFromEntity = <
  T extends Omit<EntityReference, 'type'>
>(
  entity: T,
  type: EntityType
): EntityReference => {
  return {
    id: entity.id,
    type,
    deleted: entity.deleted,
    description: entity.description,
    displayName: entity.displayName,
    fullyQualifiedName: entity.fullyQualifiedName,
    href: entity.href,
    name: entity.name,
  };
};

/**
 * Convert all the entity list to EntityReferenceList
 * @param entities -- T extends EntityReference
 * @param type -- EntityType
 * @returns EntityReference[]
 */
export const getEntityReferenceListFromEntities = <
  T extends Omit<EntityReference, 'type'>
>(
  entities: T[],
  type: EntityType
) => {
  if (isEmpty(entities)) {
    return [] as EntityReference[];
  }

  return entities.map((entity) => getEntityReferenceFromEntity(entity, type));
};

export const getEntityLinkFromType = (
  fullyQualifiedName: string,
  entityType: EntityType,
  entity?: SearchSourceAlias
) => {
  switch (entityType) {
    case EntityType.TABLE:
    case EntityType.TOPIC:
    case EntityType.DASHBOARD:
    case EntityType.PIPELINE:
    case EntityType.MLMODEL:
    case EntityType.CONTAINER:
    case EntityType.DATABASE:
    case EntityType.DATABASE_SCHEMA:
    case EntityType.DATA_PRODUCT:
    case EntityType.DASHBOARD_DATA_MODEL:
    case EntityType.STORED_PROCEDURE:
    case EntityType.SEARCH_INDEX:
    case EntityType.API_COLLECTION:
    case EntityType.API_ENDPOINT:
      return getEntityDetailsPath(entityType, fullyQualifiedName);
    case EntityType.METRIC:
      return getEntityDetailsPath(entityType, fullyQualifiedName);
    case EntityType.GLOSSARY:
    case EntityType.GLOSSARY_TERM:
      return getGlossaryTermDetailsPath(fullyQualifiedName);
    case EntityType.TAG:
    case EntityType.CLASSIFICATION:
      return getTagsDetailsPath(fullyQualifiedName);

    case EntityType.DATABASE_SERVICE:
      return getServiceDetailsPath(
        fullyQualifiedName,
        ServiceCategory.DATABASE_SERVICES
      );
    case EntityType.MESSAGING_SERVICE:
      return getServiceDetailsPath(
        fullyQualifiedName,
        ServiceCategory.MESSAGING_SERVICES
      );
    case EntityType.DASHBOARD_SERVICE:
      return getServiceDetailsPath(
        fullyQualifiedName,
        ServiceCategory.DASHBOARD_SERVICES
      );
    case EntityType.PIPELINE_SERVICE:
      return getServiceDetailsPath(
        fullyQualifiedName,
        ServiceCategory.PIPELINE_SERVICES
      );
    case EntityType.MLMODEL_SERVICE:
      return getServiceDetailsPath(
        fullyQualifiedName,
        ServiceCategory.ML_MODEL_SERVICES
      );
    case EntityType.STORAGE_SERVICE:
      return getServiceDetailsPath(
        fullyQualifiedName,
        ServiceCategory.STORAGE_SERVICES
      );
    case EntityType.SEARCH_SERVICE:
      return getServiceDetailsPath(
        fullyQualifiedName,
        ServiceCategory.SEARCH_SERVICES
      );
    case EntityType.METADATA_SERVICE:
      return getServiceDetailsPath(
        fullyQualifiedName,
        ServiceCategory.METADATA_SERVICES
      );
    case EntityType.API_SERVICE:
      return getServiceDetailsPath(
        fullyQualifiedName,
        ServiceCategory.API_SERVICES
      );
    case EntityType.BOT:
      return getBotsPath(fullyQualifiedName);
    case EntityType.TEAM:
      return getTeamsWithFqnPath(fullyQualifiedName);
    case EntityType.APPLICATION:
      return getApplicationDetailsPath(fullyQualifiedName);
    case EntityType.TEST_CASE:
      return getIncidentManagerDetailPagePath(fullyQualifiedName);
    case EntityType.TEST_SUITE:
      return getEntityDetailsPath(
        EntityType.TABLE,
        fullyQualifiedName,
        EntityTabs.PROFILER
      );
    case EntityType.DOMAIN:
      return getDomainDetailsPath(fullyQualifiedName);
    case EntityType.EVENT_SUBSCRIPTION:
      return (entity as EventSubscription)?.alertType ===
        AlertType.Observability
        ? getObservabilityAlertDetailsPath(fullyQualifiedName)
        : getNotificationAlertDetailsPath(fullyQualifiedName);
    case EntityType.ROLE:
      return getRoleWithFqnPath(fullyQualifiedName);
    case EntityType.POLICY:
      return getPolicyWithFqnPath(fullyQualifiedName);
    case EntityType.PERSONA:
      return getPersonaDetailsPath(fullyQualifiedName);
    case EntityType.KPI:
      return getKpiPath(fullyQualifiedName);
    default:
      return '';
  }
};

export const getBreadcrumbForTable = (
  entity: Table,
  includeCurrent = false
) => {
  const { service, database, databaseSchema } = entity;

  return [
    {
      name: getEntityName(service),
      url: service?.name
        ? getServiceDetailsPath(
            service?.name,
            ServiceCategory.DATABASE_SERVICES
          )
        : '',
    },
    {
      name: getEntityName(database),
      url: getEntityDetailsPath(
        EntityType.DATABASE,
        database?.fullyQualifiedName ?? ''
      ),
    },
    {
      name: getEntityName(databaseSchema),
      url: getEntityDetailsPath(
        EntityType.DATABASE_SCHEMA,
        databaseSchema?.fullyQualifiedName ?? ''
      ),
    },
    ...(includeCurrent
      ? [
          {
            name: entity.name,
            url: getEntityLinkFromType(
              entity.fullyQualifiedName ?? '',
              (entity as SourceType).entityType as EntityType
            ),
          },
        ]
      : []),
  ];
};

export const getBreadCrumbForAPICollection = (entity: APICollection) => {
  const { service } = entity;

  return [
    {
      name: startCase(ServiceCategory.API_SERVICES),
      url: getSettingPath(
        GlobalSettingsMenuCategory.SERVICES,
        getServiceRouteFromServiceType(ServiceCategory.API_SERVICES)
      ),
    },
    {
      name: getEntityName(service),
      url: service?.name
        ? getServiceDetailsPath(
            service?.name ?? '',
            ServiceCategoryPlural[
              service?.type as keyof typeof ServiceCategoryPlural
            ]
          )
        : '',
    },
  ];
};

export const getBreadCrumbForAPIEndpoint = (entity: APIEndpoint) => {
  const { service, apiCollection } = entity;

  return [
    {
      name: startCase(ServiceCategory.API_SERVICES),
      url: getSettingPath(
        GlobalSettingsMenuCategory.SERVICES,
        getServiceRouteFromServiceType(ServiceCategory.API_SERVICES)
      ),
    },
    {
      name: getEntityName(service),
      url: service?.name
        ? getServiceDetailsPath(
            service?.name ?? '',
            ServiceCategoryPlural[
              service?.type as keyof typeof ServiceCategoryPlural
            ]
          )
        : '',
    },
    {
      name: getEntityName(apiCollection),
      url: getEntityDetailsPath(
        EntityType.API_COLLECTION,
        apiCollection?.fullyQualifiedName ?? ''
      ),
    },
  ];
};

export const getBreadcrumbForEntitiesWithServiceOnly = (
  entity: EntityWithServices,
  includeCurrent = false
) => {
  const { service } = entity;

  return [
    {
      name: getEntityName(service),
      url: service?.name
        ? getServiceDetailsPath(
            service?.name,
            ServiceCategoryPlural[
              service?.type as keyof typeof ServiceCategoryPlural
            ]
          )
        : '',
    },
    ...(includeCurrent
      ? [
          {
            name: entity.name,
            url: getEntityLinkFromType(
              entity.fullyQualifiedName ?? '',
              (entity as SourceType).entityType as EntityType
            ),
          },
        ]
      : []),
  ];
};

export const getBreadcrumbForContainer = (data: {
  entity: Container;
  includeCurrent?: boolean;
  parents?: Container[] | EntityReference[];
}) => {
  const { entity, includeCurrent = false, parents = [] } = data;
  const { service } = entity;

  return [
    {
      name: getEntityName(service),
      url: service?.name
        ? getServiceDetailsPath(
            service?.name,
            ServiceCategoryPlural[
              service?.type as keyof typeof ServiceCategoryPlural
            ]
          )
        : '',
    },
    ...(parents.length > 0
      ? parents.map((parent) => ({
          name: getEntityName(parent),
          url: getEntityLinkFromType(
            parent?.fullyQualifiedName ?? '',
            EntityType.CONTAINER
          ),
        }))
      : []),
    ...(includeCurrent
      ? [
          {
            name: entity.name,
            url: getEntityLinkFromType(
              entity.fullyQualifiedName ?? '',
              (entity as SourceType).entityType as EntityType
            ),
          },
        ]
      : []),
  ];
};

export const getBreadcrumbForTestCase = (entity: TestCase): TitleLink[] => [
  {
    name: i18next.t('label.data-quality'),
    url: `${ROUTES.DATA_QUALITY}/${DataQualityPageTabs.TEST_CASES}`,
  },
  {
    name: entity.name,
    url: {
      pathname: getEntityLinkFromType(
        entity.fullyQualifiedName ?? '',
        (entity as SourceType)?.entityType as EntityType
      ),
      state: {
        breadcrumbData: [
          {
            name: i18next.t('label.data-quality'),
            url: `${ROUTES.DATA_QUALITY}/${DataQualityPageTabs.TEST_CASES}`,
          },
        ],
      },
    },
  },
];

export const getBreadcrumbForTestSuite = (entity: TestSuite) => {
  return entity.executable
    ? [
        {
          name: getEntityName(entity.executableEntityReference),
          url: getEntityLinkFromType(
            entity.executableEntityReference?.fullyQualifiedName ?? '',
            EntityType.TABLE
          ),
        },
        {
          name: t('label.test-suite'),
          url: '',
        },
      ]
    : [
        {
          name: t('label.test-suite-plural'),
          url: getDataQualityPagePath(DataQualityPageTabs.TEST_SUITES),
        },
        {
          name: getEntityName(entity),
          url: '',
        },
      ];
};

export const getBreadCrumbForKpi = (entity: Kpi) => {
  return [
    {
      name: i18next.t('label.kpi-uppercase'),
      url: getDataInsightPathWithFqn(DataInsightTabs.KPIS),
    },
    {
      name: getEntityName(entity),
      url: getKpiPath(entity.name),
    },
  ];
};

export const getEntityBreadcrumbs = (
  entity:
    | SearchedDataProps['data'][number]['_source']
    | DashboardDataModel
    | StoredProcedure
    | Database
    | DatabaseSchema
    | SearchIndexAsset
    | DataAssetsWithoutServiceField
    | APICollection
    | APIEndpoint,
  entityType?: EntityType,
  includeCurrent = false
) => {
  switch (entityType) {
    case EntityType.TABLE:
    case EntityType.STORED_PROCEDURE:
      return getBreadcrumbForTable(entity as Table, includeCurrent);
    case EntityType.GLOSSARY:
    case EntityType.GLOSSARY_TERM:
      // eslint-disable-next-line no-case-declarations
      const glossary = (entity as GlossaryTerm).glossary;
      if (!glossary) {
        return [];
      }
      // eslint-disable-next-line no-case-declarations
      const fqnList = entity.fullyQualifiedName
        ? Fqn.split(entity.fullyQualifiedName)
        : [];
      // eslint-disable-next-line no-case-declarations
      const tree = fqnList.slice(1, fqnList.length);

      return [
        {
          name: glossary.fullyQualifiedName,
          url: getGlossaryPath(glossary.fullyQualifiedName),
        },
        ...tree.map((fqn, index, source) => ({
          name: fqn,
          url: getGlossaryPath(
            `${glossary.fullyQualifiedName}.${source
              .slice(0, index + 1)
              .join('.')}`
          ),
        })),
      ];
    case EntityType.TAG:
      // eslint-disable-next-line no-case-declarations
      const fqnTagList = entity.fullyQualifiedName
        ? Fqn.split(entity.fullyQualifiedName)
        : [];

      return [
        ...fqnTagList.map((fqn) => ({
          name: fqn,
          url: getTagsDetailsPath(entity?.fullyQualifiedName ?? ''),
        })),
      ];

    case EntityType.CLASSIFICATION:
      return [
        {
          name: getEntityName(entity as Classification),
          url: '',
        },
      ];

    case EntityType.DATABASE:
      return [
        {
          name: startCase(ServiceCategory.DATABASE_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.DATABASE_SERVICES)
          ),
        },
        ...getBreadcrumbForEntitiesWithServiceOnly(entity as Database),
      ];

    case EntityType.DATABASE_SCHEMA:
      return [
        {
          name: startCase(ServiceCategory.DATABASE_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.DATABASE_SERVICES)
          ),
        },
        {
          name: getEntityName((entity as DatabaseSchema).service),
          url: (entity as DatabaseSchema).service?.name
            ? getServiceDetailsPath(
                (entity as DatabaseSchema).service?.name ?? '',
                ServiceCategoryPlural[
                  (entity as DatabaseSchema).service
                    ?.type as keyof typeof ServiceCategoryPlural
                ]
              )
            : '',
        },
        {
          name: getEntityName((entity as DatabaseSchema).database),
          url: getEntityDetailsPath(
            EntityType.DATABASE,
            (entity as DatabaseSchema).database?.fullyQualifiedName ?? ''
          ),
        },
      ];

    case EntityType.DATABASE_SERVICE:
      return [
        {
          name: startCase(ServiceCategory.DATABASE_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.DATABASE_SERVICES)
          ),
        },
      ];

    case EntityType.DASHBOARD_SERVICE:
      return [
        {
          name: startCase(ServiceCategory.DASHBOARD_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.DASHBOARD_SERVICES)
          ),
        },
      ];

    case EntityType.MESSAGING_SERVICE:
      return [
        {
          name: startCase(ServiceCategory.MESSAGING_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.MESSAGING_SERVICES)
          ),
        },
      ];

    case EntityType.PIPELINE_SERVICE:
      return [
        {
          name: startCase(ServiceCategory.PIPELINE_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.PIPELINE_SERVICES)
          ),
        },
      ];

    case EntityType.MLMODEL_SERVICE:
      return [
        {
          name: startCase(ServiceCategory.ML_MODEL_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.ML_MODEL_SERVICES)
          ),
        },
      ];

    case EntityType.METADATA_SERVICE:
      return [
        {
          name: startCase(ServiceCategory.METADATA_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.METADATA_SERVICES)
          ),
        },
      ];

    case EntityType.STORAGE_SERVICE:
      return [
        {
          name: startCase(ServiceCategory.STORAGE_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.STORAGE_SERVICES)
          ),
        },
      ];

    case EntityType.SEARCH_SERVICE:
      return [
        {
          name: startCase(ServiceCategory.SEARCH_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.SEARCH_SERVICES)
          ),
        },
      ];
    case EntityType.API_SERVICE:
      return [
        {
          name: startCase(ServiceCategory.API_SERVICES),
          url: getSettingPath(
            GlobalSettingsMenuCategory.SERVICES,
            getServiceRouteFromServiceType(ServiceCategory.API_SERVICES)
          ),
        },
      ];

    case EntityType.CONTAINER: {
      const data = entity as Container;

      return getBreadcrumbForContainer({
        entity: data,
        includeCurrent: true,
        parents: isUndefined(data.parent) ? [] : [data.parent],
      });
    }

    case EntityType.DOMAIN:
      return [
        {
          name: i18next.t('label.domain-plural'),
          url: getDomainPath(),
        },
      ];

    case EntityType.DATA_PRODUCT: {
      const data = entity as DataProduct;
      if (!data.domain) {
        return [];
      }

      return [
        {
          name: getEntityName(data.domain),
          url: getDomainPath(data.domain.fullyQualifiedName),
        },
      ];
    }

    case EntityType.TEST_CASE: {
      return getBreadcrumbForTestCase(entity as TestCase);
    }
    case EntityType.EVENT_SUBSCRIPTION: {
      return [
        {
          name: startCase(EntityType.ALERT),
          url:
            (entity as EventSubscription).alertType === AlertType.Observability
              ? ROUTES.OBSERVABILITY_ALERTS
              : ROUTES.NOTIFICATION_ALERTS,
        },
        {
          name: entity.name,
          url: getEntityLinkFromType(
            entity.fullyQualifiedName ?? '',
            (entity as SourceType).entityType as EntityType,
            entity as SearchSourceAlias
          ),
        },
      ];
    }

    case EntityType.TEST_SUITE: {
      return getBreadcrumbForTestSuite(entity as TestSuite);
    }

    case EntityType.BOT: {
      return [
        {
          name: startCase(EntityType.BOT),
          url: getBotsPagePath(),
        },
        {
          name: entity.name,
          url: getBotsPath(entity.fullyQualifiedName ?? ''),
        },
      ];
    }

    case EntityType.TEAM: {
      return [
        {
          name: getEntityName((entity as Team).parents?.[0]),
          url: getTeamsWithFqnPath(
            (entity as Team).parents?.[0].fullyQualifiedName ?? ''
          ),
        },
        {
          name: getEntityName(entity),
          url: getTeamsWithFqnPath(entity.fullyQualifiedName ?? ''),
        },
      ];
    }

    case EntityType.APPLICATION: {
      return [
        {
          name: i18next.t('label.application-plural'),
          url: getSettingPath(GlobalSettingsMenuCategory.APPLICATIONS),
        },
        {
          name: getEntityName(entity),
          url: getApplicationDetailsPath(entity.fullyQualifiedName ?? ''),
        },
      ];
    }

    case EntityType.PERSONA: {
      return [
        {
          name: i18next.t('label.persona-plural'),
          url: getSettingPath(
            GlobalSettingsMenuCategory.MEMBERS,
            GlobalSettingOptions.PERSONA
          ),
        },
        {
          name: getEntityName(entity),
          url: getPersonaDetailsPath(entity.fullyQualifiedName ?? ''),
        },
      ];
    }

    case EntityType.ROLE: {
      return [
        {
          name: i18next.t('label.role-plural'),
          url: getSettingPath(
            GlobalSettingsMenuCategory.ACCESS,
            GlobalSettingOptions.ROLES
          ),
        },
        {
          name: getEntityName(entity),
          url: getRoleWithFqnPath(entity.fullyQualifiedName ?? ''),
        },
      ];
    }

    case EntityType.POLICY: {
      return [
        {
          name: i18next.t('label.policy-plural'),
          url: getSettingPath(
            GlobalSettingsMenuCategory.ACCESS,
            GlobalSettingOptions.POLICIES
          ),
        },
        {
          name: getEntityName(entity),
          url: getPolicyWithFqnPath(entity.fullyQualifiedName ?? ''),
        },
      ];
    }

    case EntityType.API_COLLECTION:
      return getBreadCrumbForAPICollection(entity as APICollection);

    case EntityType.API_ENDPOINT:
      return getBreadCrumbForAPIEndpoint(entity as APIEndpoint);

    case EntityType.METRIC: {
      return [
        {
          name: t('label.metric-plural'),
          url: ROUTES.METRICS,
        },
        {
          name: getEntityName(entity),
          url: '',
        },
      ];
    }

    case EntityType.KPI:
      return getBreadCrumbForKpi(entity as Kpi);

    case EntityType.TOPIC:
    case EntityType.DASHBOARD:
    case EntityType.PIPELINE:
    case EntityType.MLMODEL:
    case EntityType.DASHBOARD_DATA_MODEL:
    case EntityType.SEARCH_INDEX:
    default:
      return getBreadcrumbForEntitiesWithServiceOnly(
        entity as Topic,
        includeCurrent
      );
  }
};

export const getBreadcrumbsFromFqn = (fqn: string, includeCurrent = false) => {
  const fqnList = Fqn.split(fqn);
  if (!includeCurrent) {
    fqnList.pop();
  }

  return [
    ...fqnList.map((fqn) => ({
      name: fqn,
      url: '',
    })),
  ];
};

/**
 * Take entity vote and userId as input and return name for vote status type
 * @param votes - entity votes
 * @param userId - current user id
 * @returns - vote status type
 */
export const getEntityVoteStatus = (userId: string, votes?: Votes) => {
  if (isUndefined(votes)) {
    return QueryVoteType.unVoted;
  }

  const upVoters = votes.upVoters ?? [];
  const downVoters = votes.downVoters ?? [];

  if (upVoters.some((user) => user.id === userId)) {
    return QueryVoteType.votedUp;
  } else if (downVoters.some((user) => user.id === userId)) {
    return QueryVoteType.votedDown;
  } else {
    return QueryVoteType.unVoted;
  }
};

export const highlightEntityNameAndDescription = (
  entity: SearchedDataProps['data'][number]['_source'],
  highlight: SearchedDataProps['data'][number]['highlight']
): SearchedDataProps['data'][number]['_source'] => {
  let entityDescription = entity.description ?? '';
  const descHighlights = highlight?.description ?? [];

  if (descHighlights.length > 0) {
    const matchTextArr = descHighlights.map((val: string) =>
      val.replace(/<\/?span(.*?)>/g, '')
    );

    matchTextArr.forEach((text: string, i: number) => {
      entityDescription = entityDescription.replace(text, descHighlights[i]);
    });
  }

  let entityDisplayName = getEntityName(entity);
  if (!isUndefined(highlight)) {
    entityDisplayName =
      highlight?.displayName?.join(' ') ||
      highlight?.name?.join(' ') ||
      entityDisplayName;
  }

  return {
    ...entity,
    displayName: entityDisplayName,
    description: entityDescription,
  };
};

export const columnSorter = (
  col1: { name: string; displayName?: string },
  col2: { name: string; displayName?: string }
) => {
  const name1 = getEntityName(col1);
  const name2 = getEntityName(col2);

  return name1.localeCompare(name2);
};

/**
 * Retrieves the column name from an entity link.
 * @param entityLink The entity link string.
 * @returns The column name extracted from the entity link.
 */
export const getColumnNameFromEntityLink = (entityLink: string) => {
  return EntityLink.getTableColumnName(entityLink);
};

export const getEntityNameLabel = (entityName?: string) => {
  const entityNameLabels = {
    table: t('label.table'),
    topic: t('label.topic'),
    pipeline: t('label.pipeline'),
    container: t('label.container'),
    dashboard: t('label.dashboard'),
    testCase: t('label.test-case'),
    testSuite: t('label.test-suite'),
    ingestionPipeline: t('label.ingestion-pipeline'),
    all: t('label.all'),
    announcement: t('label.announcement'),
    chart: t('label.chart'),
    conversation: t('label.conversation'),
    dashboardDataModel: t('label.data-model'),
    databaseSchema: t('label.database-schema'),
    databaseService: t('label.entity-service', {
      entity: t('label.database'),
    }),
    dashboardService: t('label.entity-service', {
      entity: t('label.dashboard'),
    }),
    messagingService: t('label.entity-service', {
      entity: t('label.messaging'),
    }),
    mlmodelService: t('label.entity-service', {
      entity: t('label.ml-model'),
    }),
    pipelineService: t('label.entity-service', {
      entity: t('label.pipeline'),
    }),
    storageService: t('label.entity-service', {
      entity: t('label.storage'),
    }),
    searchService: t('label.entity-service', { entity: t('label.search') }),
    metadataService: t('label.entity-service', {
      entity: t('label.metadata'),
    }),
    glossary: t('label.glossary'),
    glossaryTerm: t('label.glossary-term'),
    tag: t('label.tag'),
    tagCategory: t('label.classification'),
    user: t('label.user'),
    domain: t('label.domain'),
    dataProduct: t('label.data-product'),
    storedProcedure: t('label.stored-procedure'),
    searchIndex: t('label.search-index'),
    task: t('label.task'),
    mlmodel: t('label.ml-model'),
    location: t('label.location'),
    database: t('label.database'),
    alert: t('label.alert-plural'),
    query: t('label.query'),
    THREAD: t('label.thread'),
    app: t('label.application'),
  };

  return (
    entityNameLabels[entityName as keyof typeof entityNameLabels] ||
    startCase(entityName)
  );
};

export const getPluralizeEntityName = (entityType?: string) => {
  const entityNameLabels = {
    [EntityType.TABLE]: t('label.table-plural'),
    [EntityType.TOPIC]: t('label.topic-plural'),
    [EntityType.PIPELINE]: t('label.pipeline-plural'),
    [EntityType.CONTAINER]: t('label.container-plural'),
    [EntityType.DASHBOARD]: t('label.dashboard-plural'),
    [EntityType.STORED_PROCEDURE]: t('label.stored-procedure-plural'),
    [EntityType.MLMODEL]: t('label.ml-model-plural'),
    [EntityType.DASHBOARD_DATA_MODEL]: t('label.data-model-plural'),
    [EntityType.SEARCH_INDEX]: t('label.search-index-plural'),
    [EntityType.API_COLLECTION]: t('label.api-collection-plural'),
    [EntityType.API_ENDPOINT]: t('label.api-endpoint-plural'),
    [EntityType.METRIC]: t('label.metric-plural'),
  };

  return (
    entityNameLabels[entityType as keyof typeof entityNameLabels] ||
    getEntityNameLabel(entityType)
  );
};
