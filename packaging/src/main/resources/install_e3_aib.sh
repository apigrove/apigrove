#!/bin/bash

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

export IS_E3_AIB=1

#checking if it's a update process or not
if [ "$1" = "--update" ]
then
	echo "install_e3_aib.sh running in update mode"
    sh $DIR/install.sh manager gateway --update
else
	echo "install_e3_aib.sh running in install mode"
	sh $DIR/install.sh manager gateway
fi

if [ $? != 0 ]
then
    echo "E3 manager/gateway installation failed, exiting"
    exit 1
fi

cp $DIR/aib_topology.xml $E3_HOME/topology.xml
if [ $? != 0 ]
then
    echo "unable to copy the topology, exiting"
    exit 1
fi

chown $USER:$GROUP $E3_HOME/topology.xml
if [ $? != 0 ]
then
    echo "unable to chowm the topology, exiting"
    exit 1
fi

chmod 400 $DIR/../system_topology.xml
if [ $? != 0 ]
then
    echo "unable to chmod the system topology."
    exit 1
fi

cp $DIR/../system_topology.xml $E3_HOME/system_topology.xml
if [ $? != 0 ]
then
    echo "unable to copy the system topology, you need to have a system_topology.xml file in $DIR/.."
    exit 1
fi

chown $USER:$GROUP $E3_HOME/system_topology.xml
if [ $? != 0 ]
then
    echo "unable to chowm the system topology, exiting"
    exit 1
fi

chmod 600 $E3_HOME/topology.xml
if [ $? != 0 ]
then
    echo "unable to chmod the topology, exiting"
    exit 1
fi

chmod 600 $E3_HOME/system_topology.xml
if [ $? != 0 ]
then
    echo "unable to chmod the system topology, exiting"
    exit 1
fi

echo "Install successful"
