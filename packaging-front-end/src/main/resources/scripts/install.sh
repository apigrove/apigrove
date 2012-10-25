#!/bin/bash
source variables.sh

echo "Zend install started"

# Manage scripts params
DEV=0

for para in $*
do
    case $para in
        "DEV" )         DEV=1
                        DEVIP=$2;;
    esac
    shift
done


# Check if SELinux is installed on the system
#SELINUX=$(cat /selinux/enforce) &>/dev/null
if [ -d /selinux ] ; then
	SELINUX=1
	# Check if SELinux is activate on the system
	SELINUXENABLED=$(cat /selinux/enforce) &>/dev/null
	if [ -z $SELINUXENABLED ] ; then
		SELINUXENABLED=0
	fi
else
	SELINUX=0
fi

# make SH files executables
chmod 755 ./*


# Check for E3 local repo installed
CHECK_YUM_LOCAL=`ls /etc/yum.repos.d/ | grep local-repo.repo`
if [ -z $CHECK_YUM_LOCAL ] ; then
	echo "$CHECK_YUM_LOCAL"
	echo "Installing yum..."
	./install-yum-dependencies.sh
    if ! [ $? = 0 ] ; then
        echo "Yum installation failed, exiting"
        exit 1
    fi
fi

# Check for httpd installed
rpm -q httpd &>/dev/null
if ! [ $? = 0 ] ; then

	# install httpd
	yum -y install httpd
	if ! [ $? = 0 ] ; then
		echo "httpd installation failed, exiting"
		exit 1
	fi
fi

HTTPD_ON=`chkconfig --list httpd | grep 3:on`
if [ -n HTTPD_ON ] ; then
	# init http deamon
	chkconfig httpd on
	if ! [ $? = 0 ] ; then
		echo "chkconfig httpd on failed, exiting"
		exit 1
	fi
fi

# Check for php53 installed
rpm -q php53 &>/dev/null
if ! [ $? = 0 ] ; then
	# install php53
	yum -y install php53
	if ! [ $? = 0 ] ; then
		echo "php53 installation failed, exiting"
		exit 1
	fi
fi

# Check for php53-mbstring installed
rpm -q php53-mbstring &>/dev/null
if ! [ $? = 0 ] ; then
	# install php53-mbstring
	yum -y install php53-mbstring
	if ! [ $? = 0 ] ; then
		echo "php53-mbstring installation failed, exiting"
		exit 1
	fi
fi

# Check for php53-gd installed
rpm -q php53-gd &>/dev/null
if ! [ $? = 0 ] ; then
	# install php53-gd
	yum -y install php53-gd
	if ! [ $? = 0 ] ; then
		echo "php53-gd installation failed, exiting"
		exit 1
	fi
fi

# Check for libmcrypt installed
rpm -q libmcrypt &>/dev/null
if ! [ $? = 0 ] ; then
	# install libmcrypt
	rpm -Uvh $E3_LIBCRYPT_RPM
	if ! [ $? = 0 ] ; then
		echo "libmcrypt installation failed, exiting"
		exit 1
	fi
fi

# Check for php53-mcrypt installed
rpm -q php53-mcrypt &>/dev/null
if ! [ $? = 0 ] ; then
	# install php53-mcrypt
	rpm -Uvh $E3_PHPCRYPT_RPM
	if ! [ $? = 0 ] ; then
		echo "php53-mcrypt installation failed, exiting"
		exit 1
	fi
fi

# Check for php53-pdo installed
rpm -q php53-pdo &>/dev/null
if ! [ $? = 0 ] ; then
    # install php53-pdo
    yum -y install php53-pdo
    if ! [ $? = 0 ] ; then
        echo "php53-pdo installation failed, exiting"
        exit 1
    fi
fi

# Check that php.ini is installed
if ! [ -s /etc/php.ini ] ; then
    echo "php.ini does not exist, exiting"
    exit 1
fi

# Check for ZEND installed
if [ -d "$E3_ZEND_INSTALL_PATH/Zend" ] ; then
    echo "Zend already installed in $E3_ZEND_INSTALL_PATH, skipping"
else
    echo "unpacking Zend framework to $E3_ZEND_INSTALL_PATH"

    tar xzf $E3_ZEND_GZ
    if ! [ $? = 0 ] ; then
        echo "tar $E3_ZEND_GZ failed, exiting"
        exit 1
    fi

    mv $E3_ZEND_FW_PATH $E3_ZEND_INSTALL_PATH
    if ! [ $? = 0 ] ; then
        echo "mv $E3_ZEND_FW_PATH failed, exiting"
        exit 1
    fi

    find $E3_ZEND_INSTALL_PATH/Zend -type f -print0 | xargs -0 chmod -R 644 \
        && \
    find $E3_ZEND_INSTALL_PATH/Zend -type d -print0 | xargs -0 chmod -R 755 
    if ! [ $? = 0 ] ; then
        echo "chmod $E3_ZEND_INSTALL_PATH failed, exiting"
        exit 1
    fi

fi

# Check for php include_path
if grep -s -e '^[ \t]*include_path[ \t]*=' /etc/php.ini | grep -qs "$E3_ZEND_INSTALL_PATH" ; then
    echo "php.ini already has \"$E3_ZEND_INSTALL_PATH\" in its include_path, skipping"
else
    echo "installing Zend and adding it to the include_path in php.ini"
    if ! grep -qs -e '^[ \t]*include_path[ \t]*=[ \t]*' /etc/php.ini ; then
        echo "include_path not found in php.ini. Adding include_path."
        if grep -qs -e '^[; \t]*include_path[ \t]*=' /etc/php.ini ; then
            echo "adding include_path after an example include_path"
            LINENR=`grep -n -e '^[; \t]*include_path[ \t]*=' /etc/php.ini | head -1`
        elif grep -qs -e '^[ \t]*\[PHP\]' /etc/php.ini ; then
            echo "adding include_path after the [PHP] section"
            LINENR=`grep -n -e '^[ \t]*\[PHP\]' /etc/php.ini | head -1`
        else
            echo "Failed to parse the php.ini file, exiting"
            exit 1
        fi
        LINENR=${LINENR%%:*}

        echo "inserting line on linenumber $LINENR"
        ed /etc/php.ini <<HEREDOC
$LINENR + 1 i
include_path = ".:$E3_ZEND_INSTALL_PATH"
.
w
q
HEREDOC

    else
        echo "include_path exists, modifying it to add \"$E3_ZEND_INSTALL_PATH\""
        sed -i.bak1 -e 's#^[ \t]*include_path[ \t]*=[ \t]*\(['\''"]\)\?\(.*\)#include_path=\1'$E3_ZEND_INSTALL_PATH':\2#' /etc/php.ini
    fi
    
fi

# Check for short_open_tag in php.ini
sed -i.bak2 -e 's/^[ \t]*short_open_tag[ \t]*=.*$/short_open_tag = On/' /etc/php.ini

echo "Zend install ended"

# Checking for httpd/conf.d and /var/www
CHECK_WWW=`ls /etc/httpd/conf.d/ | grep front-end.conf`
if [ -z $CHECK_WWW ] ; then

	cat > /etc/httpd/conf.d/front-end.conf <<HEREDOC

Listen $E3_ZEND_PORT_NUMBER

<VirtualHost *:$E3_ZEND_PORT_NUMBER>
    DocumentRoot $E3_WWW_PATH/public
    <Directory "$E3_WWW_PATH/public">
		Options FollowSymLinks
		AllowOverride All
		Order allow,deny
		Allow from all
    </Directory>
</VirtualHost>

HEREDOC
	if ! [ $? = 0 ] ; then
		echo "front-end.conf failed, exiting"
		exit 1
	fi
	
	if [ $SELINUX = 1 ] ; then
            if semanage port -l | grep $E3_ZEND_PORT_NUMBER | grep -q http_port_t ; then
                # already set, return
                echo "port $E3_ZEND_PORT_NUMBER already configured in selinux"
            else
                semanage port -a -t http_port_t -p tcp $E3_ZEND_PORT_NUMBER
                if ! [ $? = 0 ] ; then
                    echo "semanage failed"
                    exit 1
                fi
            fi
	fi
	
	echo "Http install ended"
fi

if ! [ -d $E3_WWW_PATH ] ; then
    mkdir -p $E3_WWW_PATH
    if ! [ $? = 0 ] ; then
        echo "Create www folder failed, exiting"
        exit 1
    fi
fi

chown -R $APACHE_USER:$APACHE_GROUP $E3_WWW_PATH
if ! [ $? = 0 ] ; then
echo "chown www failed, exiting"
exit 1
fi

# Debug php
if [ $DEV = 1 ] ; then
    # Check if gcc installed
    rpm -q gcc &>/dev/null
    if ! [ $? = 0 ] ; then
        yum -y install gcc
        if ! [ $? = 0 ] ; then
            echo "install gcc failed, exiting"
            exit 1
        fi
    fi
    
    # Check if php-pear installed
    rpm -q php-pear &>/dev/null
    if ! [ $? = 0 ] ; then
        yum -y install php-pear
        if ! [ $? = 0 ] ; then
            echo "install php-pear failed, exiting"
            exit 1
        fi
    fi
    
    # Check for XDEBUG installed
    CHECK_XDEBUG=`ls /etc/php.d/ | grep xdebug.ini`
    if [ -z $CHECK_XDEBUG ] ; then
        pecl install xdebug
        if ! [ $? = 0 ] ; then
            echo "install xdebug failed, exiting"
            exit 1
        fi

        cat > /etc/php.d/xdebug.ini <<HEREDOC
xdebug.remote_enable=1
xdebug.remote_host=$DEVIP
xdebug.remote_port=9000
xdebug.remote_handler=dbgp
zend_extension="/usr/lib64/php/modules/xdebug.so"
HEREDOC
        if ! [ $? = 0 ] ; then
            echo "xdebug.ini failed, exiting"
            exit 1
        fi
    
        echo "XDEBUG install ended"
    fi
    echo "XDEBUG CONFIGURATION READY"
fi

echo "LAMP CONFIGURATION READY"

# Install E3 modules to the WWW location

cd ../php

if ! [ $? = 0 ] ; then
	echo "No php directory found"
	exit 1
fi

# Unzip php files
unzip -u $E3_PHP_APP

if ! [ $? = 0 ] ; then
	echo "No module archive file found"
	exit 1
fi

rm -Rf $E3_WWW_PATH/*
cp -Rf $E3_PHP_APP_FOLDER/* $E3_WWW_PATH

chown -R $APACHE_USER:$APACHE_GROUP $E3_WWW_PATH
if ! [ $? = 0 ] ; then
	echo "chown www path failed, exiting"
	exit 1
fi	

if [ $SELINUX = 1 ] ; then

        semanage fcontext -a -t httpd_sys_content_t "/usr/share/php/Zend(/.*)?" 
        restorecon -R /usr/share/php/Zend

	# Add httpd SELinux right to WWW Folder
	restorecon -R $E3_WWW_PATH
	if ! [ $? = 0 ] ; then
		echo "restorecon www path failed"
		exit 1
	fi
	
	if [ $SELINUXENABLED = 1 ] ; then
		# Authorize Httpd to connect network
		setsebool -P httpd_can_network_connect=1
		if ! [ $? = 0 ] ; then
			echo "HTTPD authorization to network failed"
			exit 1
		fi
	fi
fi


# Open E3_ZEND_PORT_NUMBER port for FE
if !  grep -lir "dport $E3_ZEND_PORT_NUMBER" /etc/sysconfig/iptables
then
    if ! iptables -L E3-Firewall-INPUT >/dev/null ; then
        #install the E3-firewall chain
        iptables -N E3-Firewall-INPUT && iptables -I INPUT 1 -g E3-Firewall-INPUT
        if ! [ $? = 0 ] ; then
            echo "Failed to add E3 chian to iptables"
            exit 1
        fi
    fi
    iptables -D E3-Firewall-INPUT -j REJECT --reject-with icmp-host-prohibited
    PROHIBITED=$?
    iptables -A E3-Firewall-INPUT -p tcp --dport $E3_ZEND_PORT_NUMBER -j ACCEPT
    if [ $PROHIBITED = 0 ] ; then
        iptables -A E3-Firewall-INPUT -j REJECT --reject-with icmp-host-prohibited
    fi
    service iptables save
fi

/etc/init.d/httpd restart
if ! [ $? = 0 ] ; then
    echo "httpd restart failed, exiting"
    exit 1
fi

echo "E3 PHP App installed/updated"
echo "FE installation success"
