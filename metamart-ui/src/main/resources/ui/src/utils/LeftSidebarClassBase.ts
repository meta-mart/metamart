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
import { LeftSidebarItem } from '../components/MyData/LeftSidebar/LeftSidebar.interface';
import { SIDEBAR_LIST } from '../constants/LeftSidebar.constants';

class LeftSidebarClassBase {
  /**
   * getSidebarItems
   */
  public getSidebarItems(): Array<LeftSidebarItem> {
    return SIDEBAR_LIST;
  }
}

const leftSidebarClassBase = new LeftSidebarClassBase();

export default leftSidebarClassBase;

export { LeftSidebarClassBase };
