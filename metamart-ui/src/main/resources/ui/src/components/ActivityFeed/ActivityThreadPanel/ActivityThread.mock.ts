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

import {
  TaskType,
  ThreadTaskStatus,
} from '../../../generated/entity/feed/thread';

export const mockThreadData = [
  {
    id: '465b2dfb-300e-45f5-a1a6-e19c6225e9e7',
    href: 'http://localhost:8585/api/v1/feed/465b2dfb-300e-45f5-a1a6-e19c6225e9e7',
    threadTs: 1647434125848,
    about: '<#E::table::bigquery_gcp.shopify.raw_product_catalog::description>',
    entityId: 'f1ebcfdf-d4b8-43bd-add2-1789e25ddde3',
    createdBy: 'aaron_johnson0',
    updatedAt: 1647434125848,
    updatedBy: 'anonymous',
    resolved: false,
    message: 'New thread.',
    postsCount: 0,
    posts: [],
    relativeDay: 'Today',
    task: {
      id: 153,
      type: TaskType.RequestTag,
      assignees: [
        {
          id: 'ef1b9e8a-75ba-4c12-9874-034c135be925',
          type: 'team',
          name: 'Sales',
          fullyQualifiedName: 'Sales',
          displayName: 'Sales',
          deleted: false,
        },
        {
          id: '83bd6004-9237-47a1-b917-ef006b3192d7',
          type: 'user',
          name: 'aaron_johnson0',
          fullyQualifiedName: 'aaron_johnson0',
          displayName: 'Aaron Johnson',
          deleted: false,
        },
      ],
      status: ThreadTaskStatus.Open,
      oldValue: '[]',
      suggestion:
        '[{"tagFQN":"PII.None","source":"Classification","name":"None","description":"Non PII"}]',
    },
  },
  {
    id: '40c2faec-0159-4d86-9b15-c17f3e1c081b',
    href: 'http://localhost:8585/api/v1/feed/40c2faec-0159-4d86-9b15-c17f3e1c081b',
    threadTs: 1647411418056,
    about: '<#E::table::bigquery_gcp.shopify.raw_product_catalog::description>',
    entityId: 'f1ebcfdf-d4b8-43bd-add2-1789e25ddde3',
    createdBy: 'sachin.c',
    updatedAt: 1647434031435,
    updatedBy: 'anonymous',
    resolved: false,
    message: 'New thread.',
    postsCount: 3,
    posts: [
      {
        id: 'afc5648f-9f30-4588-bd26-319c66af7c46',
        message: 'reply2',
        postTs: 1647434021493,
        from: 'aaron_johnson0',
      },
      {
        id: '8ec9283f-a671-48d6-8328-f537dadd9fc7',
        message: 'reply3',
        postTs: 1647434025868,
        from: 'aaron_johnson0',
      },
      {
        id: 'a8559fd6-940c-4f14-9808-6c376b6f872c',
        message: 'reply4',
        postTs: 1647434031430,
        from: 'aaron_johnson0',
      },
    ],
    relativeDay: 'Yesterday',
  },
];
