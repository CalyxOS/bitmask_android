#!/bin/bash

SWAGGER_MENSHEN_COMMIT=d50eef262620f58c9e3b39a4e5f0cdb7797357a1
SCRIPT_DIR=$(dirname "$0")
BASE_DIR="$SCRIPT_DIR/.."

GREEN='\033[0;32m'
NC='\033[0m'

echo -e "${GREEN}Creating swagger build directory${NC}"
mkdir -p ${BASE_DIR}/app/build/swagger

echo -e "${GREEN}Fetching swagger specification for menshen commit ${SWAGGER_MENSHEN_COMMIT}${NC}"
curl https://0xacab.org/leap/menshen/-/raw/${SWAGGER_MENSHEN_COMMIT}/api/swagger.yaml > ${BASE_DIR}/app/build/swagger/swagger.yaml

echo -e "${GREEN}Pulling swagger-codegen docker image${NC}"
docker pull swaggerapi/swagger-codegen-cli
cd $BASE_DIR

echo -e "${GREEN}Running swagger-codegen-cli in docker${NC}"
docker run --rm --user $(id -u):$(id -g) -v ${PWD}:/bitmask_android swaggerapi/swagger-codegen-cli generate \
    -i /bitmask_android/app/build/swagger/swagger.yaml \
    -l java \
    -o /bitmask_android/app/build/swagger \
    -DuseAndroidMavenGradlePlugin=true \
    -DhideGenerationTimestamp=true

echo -e "${GREEN}Copying generated model classes${NC}"
cp -r app/build/swagger/src/main/java/io/swagger/client/model/* app/src/main/java/io/swagger/client/model/.
cp app/build/swagger/src/main/java/io/swagger/client/JSON.java app/src/main/java/io/swagger/client/.

echo -e "${GREEN}Cleanup${NC}"
#rm -r app/build/swagger/