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
import { MentionBlot, MentionBlotData } from 'quill-mention';
import { Quill } from 'react-quill-new';

type RenderType = (
  data: MentionBlotData & { link: string; id: string }
) => HTMLAnchorElement;

type LinkBlotType = typeof MentionBlot & {
  render: RenderType;
};

const LinkBlot = Quill.import('blots/mention') as LinkBlotType;

LinkBlot.render = function (
  data: MentionBlotData & { link: string; id: string }
) {
  const element = document.createElement('a');
  element.innerText = data.value;
  element.href = data.link;
  element.id = data.id;

  return element;
};

LinkBlot.blotName = 'link-mention';

export { LinkBlot };
