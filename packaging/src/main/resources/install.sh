#!/bin/sh

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

if [[ $# < 1 ]]
then
    echo "Missing input parameter : manager AND/OR gateway"
    exit 1
fi

# treat the manager ip parameter passed through the option -ip
if [ "$1" = "-ip" ]
then
	IP_MANAGER="$1 $2"
	echo "Capturing the manager ip and option $IP_MANAGER"
	# ignore the first two parameters (-ip option and ip itself)
	shift 2
fi

# create the user $USER
useradd $USER -d "$E3_HOME" -m -s "/bin/bash"
  
if [ -e $DIR/dependencies.sh ] ; then
	sh $DIR/dependencies.sh
	if [ $? != 0 ]
	then
		echo "Unable to assume the dependencies"
	    exit 1
	fi  
fi

sh $DIR/copyFiles.sh
if [ $? != 0 ]
then
	echo "Unable to copy files"
    exit 1
fi


sh $DIR/configureSecurity.sh $*
if [ $? != 0 ]
then
	echo "Unable to configure the security"
    exit 1
fi

echo "Calling generateNature.sh $IP_MANAGER $*"
sh $DIR/generateNature.sh $IP_MANAGER $*
if [ $? != 0 ]
then
	echo "Unable to generate the nature"
    exit 1
fi


sh $DIR/configSystem.sh $*
if [ $? != 0 ]
then
	echo "Unable to configure the system"
    exit 1
fi


sh $DIR/sanityCheck.sh $*
if [ $? != 0 ]
then
	echo "Unable to execute the sanity check"
    exit 1
fi

echo "Install success"
exit 0

 