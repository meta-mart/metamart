#  Copyright 2021 DigiTrans
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

# Build stage
FROM alpine:3 AS build
ARG RI_VERSION="1.6.0-SNAPSHOT"
ENV RELEASE_URL="https://github.com/meta-mart/MetaMart/releases/download/${RI_VERSION}-release/metamart-${RI_VERSION}.tar.gz"

RUN mkdir -p /opt/metamart && \
    wget ${RELEASE_URL} && \
    tar zxvf metamart-*.tar.gz -C /opt/metamart --strip-components 1 && \
    rm metamart-*.tar.gz

# Final stage
FROM alpine:3
ARG RI_VERSION="1.6.0-SNAPSHOT"
ARG BUILD_DATE
ARG COMMIT_ID
LABEL maintainer="MetaMart"
LABEL org.meta-mart.image.authors="support@metamart.org"
LABEL org.meta-mart.vendor="MetaMart"
LABEL org.meta-mart.release-version="$RI_VERSION"
LABEL org.meta-mart.description="MetaMart is an open source platform for metadata management and discovery."
LABEL org.meta-mart.url="https://meta-mart.org/"
LABEL org.meta-mart.vcs-url="https://github.com/meta-mart/MetaMart"
LABEL org.meta-mart.build-date=$BUILD_DATE
LABEL org.meta-mart.commit-id=$COMMIT_ID

EXPOSE 8585 8586
RUN adduser -D metamart && apk update && \
    apk upgrade && \
    apk add --update --no-cache bash openjdk17-jre

COPY --chown=metamart:metamart --from=build /opt/metamart /opt/metamart
COPY --chmod=755 docker/metamart-start.sh ./
USER metamart

WORKDIR /opt/metamart
ENTRYPOINT [ "/bin/bash" ]
CMD ["/metamart-start.sh"]
