#!/bin/bash
# #%L
#  wcm.io
#  %%
#  Copyright (C) 2017 - 2022 wcm.io
#  %%
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#  #L%


CONGA_ENVIRONMENT="local"
CONGA_NODE="aem-author"

if [[ $0 == *":\\"* ]]; then
  DISPLAY_PAUSE_MESSAGE=true
fi

mvn -Pfast -Dconga.environments=${CONGA_ENVIRONMENT} -Dconga.nodeDirectory=target/configuration/${CONGA_ENVIRONMENT}/${CONGA_NODE} clean install conga-aem:package-install

if [ "$DISPLAY_PAUSE_MESSAGE" = true ]; then
  echo ""
  read -n1 -r -p "Press any key to continue..."
fi
