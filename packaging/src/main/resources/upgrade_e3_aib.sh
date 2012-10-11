 #!/bin/sh

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

cp -f $E3_HOME/system_topology.xml $DIR/../
if [ $? != 0 ]
then
    echo "unable to backup the system_topology, exiting"
    exit 1
fi

cp -f $E3_HOME/topology.xml $DIR/../
if [ $? != 0 ]
then
    echo "unable to backup the topology, exiting"
    exit 1
fi

#checking the parameter
if [ "$1" = "--force" ] || [ "$1" = "-f" ]
then
    echo "update process started with '--force' option"
	sh install_e3_aib.sh
else
	sh install_e3_aib.sh --update
fi

if [ $? != 0 ]
then
    echo "Upgrade failed, exiting"
	
	# restore backup files for upgrade retry
	echo "restoring system_topology.xml and topology.xml from $DIR/../ to $E3_HOME/"
	cp -f $DIR/../system_topology.xml $E3_HOME/
	cp -f $DIR/../topology.xml $E3_HOME/
    exit 1
fi

