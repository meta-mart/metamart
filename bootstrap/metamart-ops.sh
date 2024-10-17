#!/usr/bin/env bash
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

# Resolve links - $0 may be a softlink
PRG="${0}"
while [ -h "${PRG}" ]; do
  ls=`ls -ld "${PRG}"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "${PRG}"`/"$link"
  fi
done

BOOTSTRAP_DIR=`dirname ${PRG}`
CONFIG_FILE_PATH=${BOOTSTRAP_DIR}/../conf/metamart.yaml

# Which java to use
if [ -z "${JAVA_HOME}" ]; then
  JAVA="java"
else
  JAVA="${JAVA_HOME}/bin/java"
fi

METAMART_SETUP_MAIN_CLASS=org.metamart.service.util.MetaMartOperations
LIBS_DIR="${BOOTSTRAP_DIR}"/../libs/
if  [ ${debug} ] ; then
  echo $LIBS_DIR
fi
if [ -d "${LIBS_DIR}" ]; then
  # First, add digitrans-service jar to the classpath.
  # This is required for cases where we override classes from dependencies.
  for file in "${LIBS_DIR}"digitrans-service-*.jar;
  do
      CLASSPATH="$CLASSPATH":"$file"
  done
  # Then, add the rest of the libraries
  for file in "${LIBS_DIR}"*.jar;
  do
      CLASSPATH="$CLASSPATH":"$file"
  done
else
  CLASSPATH=`mvn -pl metamart-service -q exec:exec -Dexec.executable=echo -Dexec.args="%classpath"`
fi

${JAVA} -Dbootstrap.dir=$BOOTSTRAP_DIR  -cp ${CLASSPATH} ${METAMART_SETUP_MAIN_CLASS} -c $CONFIG_FILE_PATH "$@"

