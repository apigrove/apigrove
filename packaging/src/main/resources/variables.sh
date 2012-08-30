#!/bin/bash

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`


JAVA_VER=1.6.0_32

E3_JAVA_64_BIN=$DIR/../tools/x64-6u32-linux-x64-rpm.bin
E3_JAVA_32_BIN=$DIR/../tools/i586-6u32-linux-i586-rpm.bin
E3_FUSE_NAME=apache-servicemix-4.3.1-fuse-01-09
E3_FUSE_GZ=$DIR/../tools/$E3_FUSE_NAME.tar.gz

INSTALLER_HOME=$DIR/..

BUNDLES=$DIR/../bundles/

FEATURES=$DIR/../features/features.xml

JCE_PROVIDER_LIB=$BUNDLES/bcprov-jdk16-1.46.jar

# dependency archive path
DEP_ARCHIVE=$DIR/../thirdparty-dependencies/thirdparty-dependencies.tar.gz

# file containing service mix external maven repositories
SERVICE_MIX_MVN_REPO_FILE=org.ops4j.pax.url.mvn.cfg

# username/group, owner of E3
USER=e3
GROUP=e3

# installation directories
# base directory where fuse will be installed
E3_HOME=/home/$USER
# name of the symbolic link to fuse installation, located in E3_HOME directory
SMX_LINK_DIRNAME=apache-servicemix

# ssh fuse user/pwd
SMX_USER=smx
SMX_PWD=smx

# ESB full path (a link will be created in $E3_HOME/$SMX_LINK_DIRNAME
ESB_FULL_PATH=$E3_HOME/apache-servicemix-4.3.1-fuse-01-09

# configuration parameters
# template file use for karaf wrapper configuration (update java memory params in this file)
KARAF_WRAPPER_TEMPLATE=$DIR/karaf-wrapper.conf

# karaf main configuration file: configured to use felix by default
KARAF_CONFIG_FILE=$DIR/config.properties

# file max/limits configuration
FILE_MAX=1048576
FILE_LIMITS=1048500

# path to configuration file indicating mode (manager or gateway)
MODE_CONF_PATH=$E3_HOME/configuration.properties

# file containing jetty http/https config through pax-web
SERVICE_MIX_PAX_WEB_CFG_FILE=org.ops4j.pax.web.cfg

# enable/disable https access to provisioning rest api
# possible values are 1 or 0. Default port is 8443 
PROV_REST_API_HTTPS_ENABLE=0
# Path to E3 Rest server keystore. ex: $E3_HOME/rest-keystore.jks
PROV_REST_API_KEYSTORE_PATH=$E3_HOME/rest-keystore.jks
# E3 Rest server keystore password
PROV_REST_API_KEYSTORE_PASSWORD=
# E3 Rest server key password
PROV_REST_API_KEYSTORE_KEYPASSWORD=

# enable/disable http access to provisioning rest api
# possible values are 1 or 0. Default port is 8181.
PROV_REST_API_HTTP_ENABLE=1

# enable/disable basic authentication to access provisioning rest api
# possible values are 1 or 0 
PROV_REST_API_BASICAUTH_ENABLE=0
PROV_REST_API_BASICAUTH_USERNAME=changeit
PROV_REST_API_BASICAUTH_PASSWORD=changeit

# Activate IP whitelist. 
PROV_REST_API_IPWHITELIST_ENABLE=0
# ip list 192.168.84.135 192.168.84.135 192.168.84.135 
PROV_REST_API_IPWHITELIST=

