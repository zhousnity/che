#!/bin/bash
cd  wsmaster/che-core-api-workspace
mvn clean install  -Dmaven.test.skip=true  -Dskip-validate-sources  -Dmdep.analyze.skip=true
cd ../..
cd assembly
mvn clean install -pl  '!:assembly-ide-war'
#./che restart --fast --debug