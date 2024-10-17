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

COPY metamart-dist/target/metamart-*.tar.gz /

RUN mkdir -p /opt/metamart && \
    tar zxvf metamart-*.tar.gz -C /opt/metamart --strip-components 1 && \
    rm metamart-*.tar.gz

# Final stage
FROM alpine:3

EXPOSE 8585

RUN adduser -D metamart && \
    apk update && \
    apk upgrade && \
    apk add --update --no-cache bash openjdk17-jre

COPY --chown=metamart:metamart --from=build /opt/metamart /opt/metamart
COPY --chmod=755 docker/metamart-start.sh /

USER metamart

WORKDIR /opt/metamart
ENTRYPOINT [ "/bin/bash" ]
CMD ["/metamart-start.sh"]
