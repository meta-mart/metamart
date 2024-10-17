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

import { AxiosError } from 'axios';
import { isEmpty } from 'lodash';
import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import RGL, { WidthProvider } from 'react-grid-layout';
import { useTranslation } from 'react-i18next';
import ActivityFeedProvider from '../../components/ActivityFeed/ActivityFeedProvider/ActivityFeedProvider';
import Loader from '../../components/common/Loader/Loader';
import WelcomeScreen from '../../components/MyData/WelcomeScreen/WelcomeScreen.component';
import PageLayoutV1 from '../../components/PageLayoutV1/PageLayoutV1';
import {
  KNOWLEDGE_LIST_LENGTH,
  LOGGED_IN_USER_STORAGE_KEY,
} from '../../constants/constants';
import { EntityType } from '../../enums/entity.enum';
import { SearchIndex } from '../../enums/search.enum';
import { Thread } from '../../generated/entity/feed/thread';
import { PageType } from '../../generated/system/ui/page';
import { EntityReference } from '../../generated/type/entityReference';
import LimitWrapper from '../../hoc/LimitWrapper';
import { useApplicationStore } from '../../hooks/useApplicationStore';
import { useGridLayoutDirection } from '../../hooks/useGridLayoutDirection';
import { getDocumentByFQN } from '../../rest/DocStoreAPI';
import { getActiveAnnouncement } from '../../rest/feedsAPI';
import { searchQuery } from '../../rest/searchAPI';
import { getWidgetFromKey } from '../../utils/CustomizableLandingPageUtils';
import customizePageClassBase from '../../utils/CustomizePageClassBase';
import { showErrorToast } from '../../utils/ToastUtils';
import { WidgetConfig } from '../CustomizablePage/CustomizablePage.interface';
import './my-data.less';

const ReactGridLayout = WidthProvider(RGL);

const MyDataPage = () => {
  const { t } = useTranslation();
  const { currentUser, selectedPersona } = useApplicationStore();
  const [followedData, setFollowedData] = useState<Array<EntityReference>>([]);
  const [followedDataCount, setFollowedDataCount] = useState(0);
  const [isLoadingOwnedData, setIsLoadingOwnedData] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState(true);
  const [layout, setLayout] = useState<Array<WidgetConfig>>([]);
  const isMounted = useRef(false);
  const [showWelcomeScreen, setShowWelcomeScreen] = useState(false);
  const [isAnnouncementLoading, setIsAnnouncementLoading] =
    useState<boolean>(true);
  const [announcements, setAnnouncements] = useState<Thread[]>([]);
  const storageData = localStorage.getItem(LOGGED_IN_USER_STORAGE_KEY);

  const loggedInUserName = useMemo(() => {
    return currentUser?.name ?? '';
  }, [currentUser]);

  const usernameExistsInCookie = useMemo(() => {
    return storageData
      ? storageData.split(',').includes(loggedInUserName)
      : false;
  }, [storageData, loggedInUserName]);

  const fetchDocument = async () => {
    try {
      setIsLoading(true);
      if (!isEmpty(selectedPersona)) {
        const pageFQN = `${EntityType.PERSONA}.${selectedPersona.fullyQualifiedName}.${EntityType.PAGE}.${PageType.LandingPage}`;
        const pageData = await getDocumentByFQN(pageFQN);
        setLayout(pageData.data.page.layout);
      } else {
        setLayout(customizePageClassBase.defaultLayout);
      }
    } catch {
      setLayout(customizePageClassBase.defaultLayout);
    } finally {
      setIsLoading(false);
    }
  };

  const updateWelcomeScreen = (show: boolean) => {
    if (loggedInUserName) {
      const arr = storageData ? storageData.split(',') : [];
      if (!arr.includes(loggedInUserName)) {
        arr.push(loggedInUserName);
        localStorage.setItem(LOGGED_IN_USER_STORAGE_KEY, arr.join(','));
      }
    }
    setShowWelcomeScreen(show);
  };

  useEffect(() => {
    fetchDocument();
  }, [selectedPersona]);

  useEffect(() => {
    isMounted.current = true;
    updateWelcomeScreen(!usernameExistsInCookie);

    return () => updateWelcomeScreen(false);
  }, []);

  const fetchUserFollowedData = async () => {
    if (!currentUser?.id) {
      return;
    }
    setIsLoadingOwnedData(true);
    try {
      const res = await searchQuery({
        pageSize: KNOWLEDGE_LIST_LENGTH,
        searchIndex: SearchIndex.ALL,
        query: '*',
        filters: `followers:${currentUser.id}`,
      });

      setFollowedDataCount(res?.hits?.total.value ?? 0);
      setFollowedData(res.hits.hits.map((hit) => hit._source));
    } catch (err) {
      showErrorToast(err as AxiosError);
    } finally {
      setIsLoadingOwnedData(false);
    }
  };

  useEffect(() => {
    if (currentUser) {
      fetchUserFollowedData();
    }
  }, [currentUser]);

  const widgets = useMemo(
    () =>
      // Adding announcement widget to the layout when announcements are present
      // Since the widget wont be in the layout config of the page
      // ok
      [
        ...(isEmpty(announcements)
          ? []
          : [customizePageClassBase.announcementWidget]),
        ...layout,
      ].map((widget) => (
        <div data-grid={widget} key={widget.i}>
          {getWidgetFromKey({
            announcements: announcements,
            followedData,
            followedDataCount,
            isLoadingOwnedData: isLoadingOwnedData,
            widgetConfig: widget,
          })}
        </div>
      )),
    [
      layout,
      isAnnouncementLoading,
      announcements,
      followedData,
      followedDataCount,
      isLoadingOwnedData,
    ]
  );

  const fetchAnnouncements = useCallback(async () => {
    try {
      setIsAnnouncementLoading(true);
      const response = await getActiveAnnouncement();

      setAnnouncements(response.data);
    } catch (error) {
      showErrorToast(error as AxiosError);
    } finally {
      setIsAnnouncementLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchAnnouncements();
  }, []);

  // call the hook to set the direction of the grid layout
  useGridLayoutDirection(isLoading);

  if (showWelcomeScreen) {
    return (
      <div className="bg-white full-height">
        <WelcomeScreen onClose={() => updateWelcomeScreen(false)} />
      </div>
    );
  }

  return (
    <ActivityFeedProvider>
      <PageLayoutV1
        mainContainerClassName="p-t-0"
        pageTitle={t('label.my-data')}>
        {isLoading ? (
          <div className="ant-layout-content flex-center">
            <Loader />
          </div>
        ) : (
          <>
            <ReactGridLayout
              className="bg-white"
              cols={4}
              isDraggable={false}
              isResizable={false}
              margin={[
                customizePageClassBase.landingPageWidgetMargin,
                customizePageClassBase.landingPageWidgetMargin,
              ]}
              rowHeight={100}>
              {widgets}
            </ReactGridLayout>
            <LimitWrapper resource="dataAssets">
              <br />
            </LimitWrapper>
          </>
        )}
      </PageLayoutV1>
    </ActivityFeedProvider>
  );
};

export default MyDataPage;
