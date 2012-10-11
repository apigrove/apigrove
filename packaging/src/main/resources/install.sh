#!/bin/sh

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

if [[ $# < 1 ]]
then
    echo "Missing input parameter : manager AND/OR gateway"
    exit 1
fi

#checking the update mode activated
UPDATE=false
for i in $*
do
	if [ "$i" = "--update" ]
	then
		UPDATE=true
	fi
done

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

# moved up before dependencies.sh to create features.xml first
sh $DIR/copyFiles.sh
if [ $? != 0 ]
then
	echo "Unable to copy files"
    exit 1
fi
  
if [ -e $DIR/dependencies.sh ] ; then
	
	if [ "$UPDATE" = "true" ]
	then
		echo "install.sh running in udpdate mode"
		sh $DIR/dependencies.sh --update
	else
		echo "install.sh running in install mode"
		sh $DIR/dependencies.sh
	fi
	
	if [ $? != 0 ]
	then
		echo "Unable to assume the dependencies"
	    exit 1
	fi  
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

if [ -n "$GLOBAL_PROXY_HOST" -a -n "$GLOBAL_PROXY_PORT" -a -n "$GLOBAL_PROXY_USER" -a -n "$GLOBAL_PROXY_PASS" ]
then
	sed -i -e "s/^\(global_proxy.host=.*\)$/global_proxy.host=$GLOBAL_PROXY_HOST/" $MODE_CONF_PATH
	sed -i -e "s/^\(global_proxy.port=.*\)$/global_proxy.port=$GLOBAL_PROXY_PORT/" $MODE_CONF_PATH
	sed -i -e "s/^\(global_proxy.user=.*\)$/global_proxy.user=$GLOBAL_PROXY_USER/" $MODE_CONF_PATH
	sed -i -e "s/^\(global_proxy.pass=.*\)$/global_proxy.pass=$GLOBAL_PROXY_PASS/" $MODE_CONF_PATH
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

echo "Install success: $*" 
exit 0

 
