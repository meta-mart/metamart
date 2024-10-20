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
import { SuggestionKeyDownProps } from '@tiptap/suggestion';

export interface SuggestionItem {
  id: string;
  name: string;
  fqn: string;
  label: string;
  type: string;
  href: string;
}

export interface ExtensionRef {
  onKeyDown: (props: SuggestionKeyDownProps) => boolean;
}

export interface EditorSlotsRef {
  onMouseDown: (e: React.MouseEvent<HTMLDivElement, MouseEvent>) => void;
}
