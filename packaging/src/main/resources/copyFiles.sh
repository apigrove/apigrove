#!/bin/bash

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

if [ ! -n "$BUNDLES" ] 
then
  echo "Unable to find the directory BUNDLES_SRC in zip " $BUNDLES_SRC 
exit 1
fi

if [ ! -n "$FEATURES" ]
then
  echo "Unable to find FEATURE_SRC in zip " $FEATURE_SRC
exit 1
fi

# replace PATH in the feature file
SEARCH_STRING="\\$\\\$PATH\\$\\$"

REPLACE_STRING=$DIR

FILE=$FEATURES

EXPR="s#${SEARCH_STRING}#${REPLACE_STRING}#g"

sed -i -e  $EXPR $FILE

# Default Key/Cert file installation ...
DEFAULT_KEY_CERT_LOCATION=$E3_HOME/.grove
echo "Installing default Key/Cert in $DEFAULT_KEY_CERT_LOCATION ..."

mkdir -p $DEFAULT_KEY_CERT_LOCATION
cp -f $DIR/../defaults/e3.default.crt $DIR/../defaults/e3.default.key $DEFAULT_KEY_CERT_LOCATION
# Let the owner be the root user
chown -R root:$USER $DEFAULT_KEY_CERT_LOCATION

# Root keep all access
# Grove user only have execute (intering the directory)
chmod 710 $DEFAULT_KEY_CERT_LOCATION
chmod 640 $DEFAULT_KEY_CERT_LOCATION/e3.default.crt $DEFAULT_KEY_CERT_LOCATION/e3.default.key

exit 0
