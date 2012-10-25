#!/bin/bash

# dependencies
E3_RPM=e3rpm
E3_RPM_ZIP=../dependencies/$E3_RPM.zip
E3_ZEND=ZendFramework-1.11.13-minimal
E3_ZEND_GZ=../dependencies/$E3_ZEND.tar.gz
E3_ZEND_FW_PATH=$E3_ZEND/library/Zend
E3_LIBCRYPT_RPM=../dependencies/libmcrypt-2.5.7-5.el5.x86_64.rpm
E3_PHPCRYPT_RPM=../dependencies/php53-mcrypt-5.3.3-1.el5.x86_64.rpm

# e3 data
E3_PHP_APP=../php/front-end*.zip
E3_PHP_APP_FOLDER=${E3_PHP_APP%.zip}

# config
E3_ZEND_INSTALL_PATH=/usr/share/php
E3_WWW_PATH=/var/www/front-end
E3_WWW_DATA_PATH=$E3_WWW_PATH/application/data
APACHE_USER=apache
APACHE_GROUP=$APACHE_USER
E3_ZEND_PORT_NUMBER=24100
