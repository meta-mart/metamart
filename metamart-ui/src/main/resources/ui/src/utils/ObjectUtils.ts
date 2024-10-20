/*
 *  Copyright 2023 DigiTrans.
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
import { has } from 'lodash';

/**
 * Checks if the given object has the specified key(s).
 *
 * @template T - The type of the object.
 * @param {T} data - The object to check.
 * @param {string[]} keys - The key(s) to check for.
 * @param {boolean} checkAllKey - If true, checks if all keys are present; if false, checks if any key is present.
 * @returns {boolean} True if the object has the key(s), false otherwise.
 */
export const isHasKey = <T>(
  data: T,
  keys: string[],
  checkAllKey = false
): boolean => {
  const func = (item: string) => has(data, item);

  return checkAllKey ? keys.every(func) : keys.some(func);
};
