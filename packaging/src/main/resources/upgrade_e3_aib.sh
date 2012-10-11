#!/bin/sh

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

cp $E3_HOME/system_topology.xml $DIR/../
if [ $? != 0 ]
then
    echo "unable to backup the system_topology, exiting"
    exit 1
fi

sh install_e3_aib.sh

if [ $? != 0 ]
then
    echo "Upgrade failed, exiting"
    exit 1
fi

