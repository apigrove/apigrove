#!/bin/bash

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

serviceMixDown=0
retry=20
for n in `seq 1 $retry`
do
    $E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER ' '
    if [[ ${?} = 0 ]]
    then
       echo "Karaf is still up, retrying in 5 seconds..."
       sleep 5
    else
       echo "OK, Karaf is down"
       serviceMixDown=1
       break
    fi
done

if [[ $serviceMixDown == 1 ]]
then
    exit 0
else
	echo 'Failed to stop karaf'
    exit 1
fi
