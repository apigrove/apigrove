#!/bin/sh

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

PATH_TO_TAR_GZ=$1

# backup config files
cp $E3_HOME/topology.xml $DIR/../
if [ $? != 0 ]
then
    echo "unable to backup the topology, exiting"
    exit 1
fi

cp $E3_HOME/system_topology.xml $DIR/../
if [ $? != 0 ]
then
    echo "unable to backup the system_topology, exiting"
    exit 1
fi

# Call install
sh $DIR/install_e3_cluster.sh $PATH_TO_TAR_GZ

