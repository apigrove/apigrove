#!/bin/bash

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

serviceMixOk=0
retry=3
for n in `seq 1 $retry`
do
    $E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER ' '
    if [[ ${?} != 0 ]]
    then
       # unable to connect: wait and retry 
       echo "retrying in 3 seconds..."
       sleep 3
    else
       echo "OK, Karaf is started"
       serviceMixOk=1
       break
    fi
done

if [[ $serviceMixOk == 0 ]]
then
    exit 0
else
    exit 1
fi

