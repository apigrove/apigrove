#!/bin/sh

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

if [[ $# < 1 ]]
then
    echo "Missing input parameter : Absolute path to installer.tar.zg"
    exit 1
fi

PATH_TO_TAR_GZ=$1


#checking the update mode activated
UPDATE=false
for i in $*
do
	if [ "$i" = "--update" ]
	then
		UPDATE=true
	fi
done


if [[ $PATH_TO_TAR_GZ = /* ]]
then
	if [ ! -e $PATH_TO_TAR_GZ ]
	then
		echo "File $PATH_TO_TAR_GZ does not exist"
		exit 1
	fi
else
        echo "Error: Please provide the absolute path to the archive"
        exit 1
fi


# Install manager
if [ "$UPDATE" = "true" ]
then
	echo "install.sh running in udpdate mode"
	sh $DIR/install.sh manager --update
else
	echo "install.sh running in install mode"
	sh $DIR/install.sh manager
fi

if [ $? != 0 ]
then
    echo "E3 manager installation failed, exiting"
    exit 1
fi

# copy the config files 
chown $USER:$GROUP $DIR/../topology.xml
if [ $? != 0 ]
then
    echo "unable to chown the topology, you need to have a topology.xml file in $DIR/.."
    exit 1
fi

chmod 600 $DIR/../topology.xml
if [ $? != 0 ]
then
    echo "unable to chmod the topology."
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
    echo "unable to chown the system topology, exiting"
    exit 1
fi

if [ ! -e $DIR/../installer-config.xml ]
then
    cp $DIR/../templates/template-installer-config.xml $DIR/../installer-config.xml 
    if [ $? != 0 ]
    then
        echo "unable to copy the installer-config, exiting"
        exit 1
    fi
fi

chown $USER:$GROUP $DIR/../installer-config.xml
if [ $? != 0 ]
then
    echo "unable to chown the installer-config, exiting"
    exit 1
fi

echo "Calling system manager"
HTTP_CODE_RETURN=0
rm -f result.log

# replace / with a space
TAR_MODULE_PATH=`dirname $PATH_TO_TAR_GZ`
TAR_MODULE_PATH=$(echo $TAR_MODULE_PATH|sed 's/\//%2F/g')

HTTP_SCHEME="http"
PORT="8181"
if [ $PROV_REST_API_HTTPS_ENABLE = 1 ] ; then
	HTTP_SCHEME="https"
	PORT="8443"
fi

credentials="$PROV_REST_API_BASICAUTH_USERNAME:$PROV_REST_API_BASICAUTH_PASSWORD"
credentials64=`printf "$credentials"|base64`

curl -k --header "Authorization: Basic $credentials64" -s -o result.log -w "%{http_code}" $HTTP_SCHEME://localhost:$PORT/cxf/e3/system-manager/install/$TAR_MODULE_PATH > log.log

result=`cat log.log`
if [[ "$result" != *200* ]]; then
  HTTP_CODE_RETURN=1
fi

echo " WebService response begin "
cat result.log
echo " WebService response end "

if ! [ $HTTP_CODE_RETURN = 0 ] ; then
        echo "Install failed"
        exit $HTTP_CODE_RETURN
fi


chmod 600 $E3_HOME/topology.xml
if [ $? != 0 ]
then
    echo "unable to chmod the topology in E3 home, exiting"
    exit 1
fi

echo "Install successful"

