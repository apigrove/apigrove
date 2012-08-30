#!/bin/bash
echo "Test if components are up and running..."

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

JAVA_HOME=/usr/java/default
export JAVA_HOME=/usr/java/default

$JAVA_HOME/bin/java -version 2> /tmp/tmp.ver
VERSION=`cat /tmp/tmp.ver | grep "java version" | awk '{ print substr($3, 2, length($3)-2); }'`
rm -f /tmp/tmp.ver

if [[ $VERSION != $JAVA_VER ]]
then
echo "warning: java version doesn't match: $VERSION != $JAVA_VER"
fi

# check property file exists
if [ ! -f $MODE_CONF_PATH ]
then
	echo "error: no property file found at $MODE_CONF_PATH"
	exit 1
fi

nbWait=24
numero=0
while [ $numero != $nbWait ]
do
    # Check if component is running
    result=`$E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER list | grep "E3 Bundle" | grep Active`
    if [[ $result = *Failed* ]]; then
        echo "E3 Bundle component failed start"
        exit 1
    fi
    if [[ $result = *Waiting* ]]; then
        result="In Wait"
    fi
    if [[ $result = *Active* ]]; then
        echo "E3 Bundle component up and running"
        exit 0
    else
        numero=`expr $numero + 1`
        if [ $numero = $nbWait ] ; then
            echo "E3 Bundle component not started"
            exit 1
        fi
        sleep 5
    fi
done

echo "Sanity Check script error"
exit 1
