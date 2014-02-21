#!/usr/bin/env bash

. ./compile.sh

HOST=localhost
LICENSE=$VOLTDB_HOME/voltdb/license.xml
DEPLOY=deployment.xml

# start VoltDB in background (4.0 syntax)
voltdb create ${CATALOG_NAME}.jar -H $HOST -d $DEPLOY -l $LICENSE --background

