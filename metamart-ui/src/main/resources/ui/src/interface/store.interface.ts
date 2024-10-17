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
import { ItemType } from 'antd/lib/menu/hooks/useItems';
import {
  AuthenticationConfigurationWithScope,
  IAuthContext,
  OidcUser,
} from '../components/Auth/AuthProviders/AuthProvider.interface';
import { InlineAlertProps } from '../components/common/InlineAlert/InlineAlert.interface';
import {
  EntityUnion,
  ExploreSearchIndex,
} from '../components/Explore/ExplorePage.interface';
import { AuthenticationConfiguration } from '../generated/configuration/authenticationConfiguration';
import { AuthorizerConfiguration } from '../generated/configuration/authorizerConfiguration';
import { LoginConfiguration } from '../generated/configuration/loginConfiguration';
import { LogoConfiguration } from '../generated/configuration/logoConfiguration';
import { UIThemePreference } from '../generated/configuration/uiThemePreference';
import { Domain } from '../generated/entity/domains/domain';
import { User } from '../generated/entity/teams/user';
import { EntityReference } from '../generated/entity/type';

export interface HelperFunctions {
  onLoginHandler: () => void;
  onLogoutHandler: () => void;
  handleSuccessfulLogin: (user: OidcUser) => Promise<void>;
  handleFailedLogin: () => void;
  updateAxiosInterceptors: () => void;
  trySilentSignIn: (forceLogout?: boolean) => Promise<void>;
}

export interface ApplicationStore
  extends IAuthContext,
    LogoConfiguration,
    LoginConfiguration {
  isApplicationLoading: boolean;
  setApplicationLoading: (loading: boolean) => void;
  userProfilePics: Record<string, User>;
  cachedEntityData: Record<string, EntityUnion>;
  selectedPersona: EntityReference;
  oidcIdToken: string;
  refreshTokenKey: string;
  authConfig?: AuthenticationConfigurationWithScope;
  applicationConfig?: UIThemePreference;
  searchCriteria: ExploreSearchIndex | '';
  theme: UIThemePreference['customTheme'];
  inlineAlertDetails?: InlineAlertProps;
  setInlineAlertDetails: (alertDetails?: InlineAlertProps) => void;
  setSelectedPersona: (persona: EntityReference) => void;
  setApplicationConfig: (config: UIThemePreference) => void;
  setCurrentUser: (user: User) => void;
  setAuthConfig: (authConfig: AuthenticationConfigurationWithScope) => void;
  setAuthorizerConfig: (authorizerConfig: AuthorizerConfiguration) => void;
  setJwtPrincipalClaims: (
    claims: AuthenticationConfiguration['jwtPrincipalClaims']
  ) => void;
  setJwtPrincipalClaimsMapping: (
    claimsMapping: AuthenticationConfiguration['jwtPrincipalClaimsMapping']
  ) => void;
  setHelperFunctionsRef: (helperFunctions: HelperFunctions) => void;
  updateUserProfilePics: (data: { id: string; user: User }) => void;
  updateCachedEntityData: (data: {
    id: string;
    entityDetails: EntityUnion;
  }) => void;

  getRefreshToken: () => string;
  setRefreshToken: (refreshToken: string) => void;
  getOidcToken: () => string;
  setOidcToken: (oidcToken: string) => void;
  removeOidcToken: () => void;
  removeRefreshToken: () => void;
  updateSearchCriteria: (criteria: ExploreSearchIndex | '') => void;
  trySilentSignIn: (forceLogout?: boolean) => void;
}

export interface DomainStore {
  domains: Domain[];
  userDomains: EntityReference[];
  domainLoading: boolean;
  activeDomain: string;
  activeDomainEntityRef?: EntityReference;
  domainOptions: ItemType[];
  updateDomains: (domainsArr: Domain[], selectDefault?: boolean) => void;
  updateActiveDomain: (activeDomainKey: string) => void;
  setDomains: (domains: Domain[]) => void;
  setUserDomains: (userDomainsArr: EntityReference[]) => void;
  updateDomainLoading: (loading: boolean) => void;
}
