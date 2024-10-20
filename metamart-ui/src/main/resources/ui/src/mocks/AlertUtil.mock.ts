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

import {
  SubscriptionCategory,
  SubscriptionType,
} from '../generated/events/eventSubscription';

export const mockExternalDestinationOptions = Object.values(
  SubscriptionType
).filter((value) => value !== SubscriptionType.ActivityFeed);

export const mockTaskInternalDestinationOptions = [
  SubscriptionCategory.Owners,
  SubscriptionCategory.Assignees,
];

export const mockNonTaskInternalDestinationOptions = Object.values(
  SubscriptionCategory
).filter(
  (value) =>
    value !== SubscriptionCategory.External &&
    value !== SubscriptionCategory.Assignees
);
