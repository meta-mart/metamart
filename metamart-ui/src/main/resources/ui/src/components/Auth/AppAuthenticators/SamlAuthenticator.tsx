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
/*
 *  Copyright 2021 DigiTrans
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

import React, {
  forwardRef,
  Fragment,
  ReactNode,
  useImperativeHandle,
} from 'react';
import { SamlSSOClientConfig } from '../../../generated/configuration/authenticationConfiguration';
import { postSamlLogout } from '../../../rest/miscAPI';
import { showErrorToast } from '../../../utils/ToastUtils';

import { ROUTES } from '../../../constants/constants';
import { useApplicationStore } from '../../../hooks/useApplicationStore';
import { AccessTokenResponse, refreshSAMLToken } from '../../../rest/auth-API';
import { AuthenticatorRef } from '../AuthProviders/AuthProvider.interface';

interface Props {
  children: ReactNode;
  onLogoutSuccess: () => void;
}

const SamlAuthenticator = forwardRef<AuthenticatorRef, Props>(
  ({ children, onLogoutSuccess }: Props, ref) => {
    const {
      setIsAuthenticated,
      authConfig,
      getOidcToken,
      getRefreshToken,
      setRefreshToken,
      setOidcToken,
    } = useApplicationStore();
    const config = authConfig?.samlConfiguration as SamlSSOClientConfig;

    const handleSilentSignIn = async (): Promise<AccessTokenResponse> => {
      const refreshToken = getRefreshToken();

      const response = await refreshSAMLToken({
        refreshToken: refreshToken as string,
      });

      setRefreshToken(response.refreshToken);
      setOidcToken(response.accessToken);

      return Promise.resolve(response);
    };

    const login = async () => {
      if (config.idp.authorityUrl) {
        const redirectUri = `${window.location.origin}${ROUTES.SAML_CALLBACK}`;
        window.location.href = `${config.idp.authorityUrl}?redirectUri=${redirectUri}`;
      } else {
        showErrorToast('SAML IDP Authority URL is not configured.');
      }
    };

    const logout = () => {
      const token = getOidcToken();
      if (token) {
        postSamlLogout()
          .then(() => {
            setIsAuthenticated(false);
            try {
              onLogoutSuccess();
            } catch (err) {
              // TODO: Handle error on logout failure
              // eslint-disable-next-line no-console
              console.log(err);
            }
          })
          .catch((err) => {
            // eslint-disable-next-line no-console
            console.log('Error while logging out', err);
          });
      }
    };

    useImperativeHandle(ref, () => ({
      invokeLogin: login,
      invokeLogout: logout,
      renewIdToken: handleSilentSignIn,
    }));

    return <Fragment>{children}</Fragment>;
  }
);

SamlAuthenticator.displayName = 'SamlAuthenticator';

export default SamlAuthenticator;
