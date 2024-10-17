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

import { Image } from 'antd';
import React, { useEffect, useMemo, useState } from 'react';
import { getEntityName } from '../../../../../utils/EntityUtils';
import {
  getImageWithResolutionAndFallback,
  ImageQuality,
} from '../../../../../utils/ProfilerUtils';
import ProfilePicture from '../../../../common/ProfilePicture/ProfilePicture';
import { UserProfileImageProps } from './UserProfileImage.interface';

const UserProfileImage = ({ userData }: UserProfileImageProps) => {
  const [isImgUrlValid, setIsImgUrlValid] = useState<boolean>(true);

  const image = useMemo(
    () =>
      getImageWithResolutionAndFallback(ImageQuality['6x'], userData?.images),
    [userData?.images]
  );

  useEffect(() => {
    setIsImgUrlValid(Boolean(image));
  }, [image]);

  return (
    <div
      className="profile-image-container"
      data-testid="profile-image-container">
      {isImgUrlValid ? (
        <Image
          alt="profile"
          data-testid="user-profile-image"
          preview={false}
          referrerPolicy="no-referrer"
          src={image ?? ''}
          onError={() => {
            setIsImgUrlValid(false);
          }}
        />
      ) : (
        <ProfilePicture
          displayName={getEntityName(userData)}
          height="54"
          name={userData?.name ?? ''}
          width="54"
        />
      )}
    </div>
  );
};

export default UserProfileImage;
