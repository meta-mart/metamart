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
import { JwtPayload } from 'jwt-decode';
import React, { createContext, ReactNode, useContext, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useHistory } from 'react-router-dom';
import {
  HTTP_STATUS_CODE,
  LOGIN_FAILED_ERROR,
} from '../../../constants/Auth.constants';
import { ROUTES } from '../../../constants/constants';
import { PasswordResetRequest } from '../../../generated/auth/passwordResetRequest';
import { RegistrationRequest } from '../../../generated/auth/registrationRequest';
import {
  basicAuthRegister,
  basicAuthSignIn,
  checkEmailInUse,
  generatePasswordResetLink,
  logoutUser,
  resetPassword,
} from '../../../rest/auth-API';
import { getBase64EncodedString } from '../../../utils/CommonUtils';
import {
  showErrorToast,
  showInfoToast,
  showSuccessToast,
} from '../../../utils/ToastUtils';
import { resetWebAnalyticSession } from '../../../utils/WebAnalyticsUtils';

import { toLower } from 'lodash';
import { useApplicationStore } from '../../../hooks/useApplicationStore';
import { OidcUser } from './AuthProvider.interface';

export interface BasicAuthJWTPayload extends JwtPayload {
  isBot?: false;
  email?: string;
}

interface BasicAuthProps {
  children: ReactNode;
  onLoginSuccess: (user: OidcUser) => void;
  onLoginFailure: () => void;
}

interface InitialContext {
  handleLogin: (email: string, password: string) => void;
  handleRegister: (payload: RegistrationRequest) => void;
  handleForgotPassword: (email: string) => Promise<void>;
  handleResetPassword: (payload: PasswordResetRequest) => Promise<void>;
  handleLogout: () => void;
  loginError?: string | null;
}

/**
 * @ignore
 */
const stub = (): never => {
  throw new Error('You forgot to wrap your component in <BasicAuthProvider>.');
};

const initialContext = {
  handleLogin: stub,
  handleRegister: stub,
  handleForgotPassword: stub,
  handleResetPassword: stub,
  handleLogout: stub,
  handleUserCreated: stub,
};

/**
 * The Basic Auth Context
 */
export const BasicAuthContext = createContext<InitialContext>(initialContext);

const BasicAuthProvider = ({
  children,
  onLoginSuccess,
  onLoginFailure,
}: BasicAuthProps) => {
  const { t } = useTranslation();
  const {
    setRefreshToken,
    setOidcToken,
    getOidcToken,
    removeOidcToken,
    getRefreshToken,
  } = useApplicationStore();
  const [loginError, setLoginError] = useState<string | null>(null);
  const history = useHistory();

  const handleLogin = async (email: string, password: string) => {
    try {
      setLoginError(null);
      try {
        const response = await basicAuthSignIn({
          email,
          password: getBase64EncodedString(password),
        });

        if (response.accessToken) {
          setRefreshToken(response.refreshToken);
          setOidcToken(response.accessToken);

          onLoginSuccess({
            id_token: response.accessToken,
            profile: {
              email: toLower(email),
              name: '',
              picture: '',
              sub: '',
            },
            scope: '',
          });
        }

        // reset web analytic session
        resetWebAnalyticSession();
      } catch (error) {
        const err = error as AxiosError<{ code: number; message: string }>;

        setLoginError(err.response?.data.message || LOGIN_FAILED_ERROR);
        onLoginFailure();
      }
    } catch (err) {
      showErrorToast(err as AxiosError, t('server.unauthorized-user'));
    }
  };

  const handleRegister = async (request: RegistrationRequest) => {
    try {
      const isEmailAlreadyExists = await checkEmailInUse(request.email);
      if (!isEmailAlreadyExists) {
        await basicAuthRegister(request);

        showSuccessToast(
          t('server.create-entity-success', { entity: t('label.user-account') })
        );
        showInfoToast(t('server.email-confirmation'));
        history.push(ROUTES.SIGNIN);
      } else {
        return showErrorToast(t('server.email-found'));
      }
    } catch (err) {
      if (
        (err as AxiosError).response?.status ===
        HTTP_STATUS_CODE.FAILED_DEPENDENCY
      ) {
        showSuccessToast(
          t('server.create-entity-success', { entity: t('label.user-account') })
        );
        showErrorToast(err as AxiosError, t('server.email-verification-error'));
        history.push(ROUTES.SIGNIN);
      } else {
        showErrorToast(err as AxiosError, t('server.unexpected-response'));
      }
    }
  };

  const handleForgotPassword = async (email: string) => {
    try {
      await generatePasswordResetLink(email);
    } catch (err) {
      if (
        (err as AxiosError).response?.status ===
        HTTP_STATUS_CODE.FAILED_DEPENDENCY
      ) {
        showErrorToast(t('server.forgot-password-email-error'));
      } else {
        showErrorToast(t('server.email-not-found'));
      }
    }
  };

  const handleResetPassword = async (payload: PasswordResetRequest) => {
    const response = await resetPassword(payload);
    if (response) {
      showSuccessToast(t('server.reset-password-success'));
    }
  };

  const handleLogout = async () => {
    const token = getOidcToken();
    const refreshToken = getRefreshToken();
    if (token) {
      try {
        await logoutUser({ token, refreshToken });
        removeOidcToken();
        history.push(ROUTES.SIGNIN);
      } catch (error) {
        showErrorToast(error as AxiosError);
      }
    }
  };

  const contextValue = {
    handleLogin,
    handleRegister,
    handleForgotPassword,
    handleResetPassword,
    handleLogout,
    loginError,
  };

  return (
    <BasicAuthContext.Provider value={contextValue}>
      {children}
    </BasicAuthContext.Provider>
  );
};

export const useBasicAuth = () => useContext(BasicAuthContext);

export default BasicAuthProvider;
