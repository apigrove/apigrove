#!/bin/bash

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

#checking running mode
if [ "$1" = "--update" ]
then
	MODE=update
else
	MODE=install
fi

# this function installs java if java is not present in OS
function redhat_java {
    # Get input parameters
    JAVA_BIN="$1"
    JAVA_MD5="$2"

    # Check if java is already installed
    if which java ; then
        VER=`java -version 2>&1 | head -1 | awk '{print $3}' | tr -d \"`
        MAINVER="${VER%_*}"
        MAINVER="${MAINVER%.*}"
        MAINVER="${MAINVER#*.}"
        SUBVER="${VER#*_}"


        J_MAINVER="${JAVA_VER%_*}"
        J_MAINVER="${J_MAINVER%.*}"
        J_MAINVER="${J_MAINVER#*.}"
        J_SUBVER="${JAVA_VER#*_}"

        #        if [[ $MAINVER -gt $J_MAINVER ]] ; then return 0 ; fi
        #        if [ $MAINVER = $J_MAINVER ] ; then
        #          if [[ $SUBVER -ge $J_SUBVER ]] ; then return 0 ; fi
        #        fi

        if [ "$VER"x != "$JAVA_VER"x ] ; then
        Reinstall='yes'
        fi

        JDKVendor=`java -version 2>&1 | awk 'NR==2 {print $1}'`
        if [ "$JDKVendor"x != "Java(TM)"x ] ; then
        Reinstall='yes'
        fi
    else
        Reinstall='yes'
    fi

    if [ "$Reinstall" == "yes" ] ; then
        (
            # Compute and check MD5 sum
            #md5=`md5sum $JAVA_BIN`
            #md5check=`cat $JAVA_MD5`

            #set -- $md5

            #if [[ $1 != $md5check ]]; then
            #      echo " bad JAVA md5 checksum"
            #      exit 1
            #fi

            # Create directory if not exists
            if [ ! -e "/usr/java" ] ; then
               mkdir /usr/java
            fi

            # Copy the bin
            cp $JAVA_BIN /usr/java
            cd /usr/java
            JAVA_NAME=`basename $JAVA_BIN`
            chmod +x $JAVA_NAME
            test ! -z "$JAVA_NAME" || exit 1

            # do not remove any old java
            #rm -f /usr/bin/java

            # Install with auto answer
            echo "yes" | ./$JAVA_NAME &>/dev/null

            rm -f $JAVA_NAME

            # Set the environment variable
            JAVA_HOME=/usr/java/default
            export JAVA_HOME=/usr/java/default
            echo "export JAVA_HOME=/usr/java/default" > /etc/profile.d/java.sh
            export  PATH=$PATH:$JAVA_HOME/bin
            echo "export PATH=$JAVA_HOME/bin:$PATH" >> /etc/profile.d/java.sh
            . /etc/profile.d/java.sh
            source /etc/profile.d/java.sh
        )
        if ! [ $? = 0 ] ; then
            echo "Failed to install java" >&2
            exit 1
        fi
    fi
} # end redhat_java


function patch_karaf_wrapper {
    KARAF_WRAPPER_PATH=$E3_HOME/$SMX_LINK_DIRNAME/etc/karaf-wrapper.conf
    
    cp $KARAF_WRAPPER_PATH $KARAF_WRAPPER_PATH.bak
    cp $KARAF_WRAPPER_TEMPLATE $KARAF_WRAPPER_PATH
    
    EXPR="s#E3_HOME_DIR#$E3_HOME#g"
    sed -i -e $EXPR $KARAF_WRAPPER_PATH

} # end patch_karaf_wrapper

function patch_karaf_config {

	# patch karaf configuration file to use felix as osgi implementation
	KARAF_CONFIG_FILE_PATH=$E3_HOME/$SMX_LINK_DIRNAME/etc/config.properties
	
	echo "Overriding karaf config file $KARAF_CONFIG_FILE_PATH with custom version"
	cp $KARAF_CONFIG_FILE_PATH $KARAF_CONFIG_FILE_PATH.bak
    cp $KARAF_CONFIG_FILE $KARAF_CONFIG_FILE_PATH
}

function update_smx_ssh_pwd {
	smxPwdFile=$E3_HOME/$ESB_NAME/etc/users.properties
	echo "Updading servicemix password user:$SMX_USER $smxPwdFile"
	sed -i -e "s/^.*,admin/$SMX_USER=$SMX_PWD,admin/" $smxPwdFile
	echo "$smxPwdFile updated."
}

function universal_esb {
    export JAVA_HOME=/usr/java/default
    # Get input parameters
    FUSE_GZ="$1"
    FUSE_MD5="$2"
    
    ESB_DIR="${FUSE_GZ%.tar.gz}"
    ESB_NAME=`basename $ESB_DIR`
    
	# Update smx ssh pwd
	
	
    if [ -e "$E3_HOME/$ESB_NAME" ] ; then
        # Fuse already installed
		echo $E3_HOME/$ESB_NAME
        service karaf-service stop
		rm -rf $E3_HOME/$SMX_LINK_DIRNAME
        rm -rf $E3_HOME/$ESB_NAME
		#removing configuration file id update if option is activated
		if [ "$MODE" = "update" ]
		then
			sh $DIR/mergeProperties.sh $MODE_CONF_PATH $DIR/configuration.properties
			
			if [ $? != 0 ]
			then
				echo "error during merge process of configuration file"
				exit 1
			fi			
		else
			rm -f $MODE_CONF_PATH
		fi
        rm -f $E3_HOME/topology.xml
		rm -f $E3_HOME/system_topology.xml
		rm -f $E3_HOME/installer-config.xml
		rm -rf $E3_HOME/TDR_ProcessScript
		rm -f /etc/init.d/karaf-service
		rm -f /root/.ssh/known_hosts
    fi
    
    # Compute and check MD5 sum 
    #md5=`md5sum $FUSE_GZ`
    #md5check=`cat $FUSE_MD5`

    #set -- $md5
    
    #if [[ $1 != $md5check ]]; then
    #      echo " bad fuse md5 checksum"
    #      exit 1
    #fi

    # Install fuse
    tar xzf $FUSE_GZ -C $E3_HOME

    chown -R $USER:$GROUP $E3_HOME/$ESB_NAME
	
	su - $USER -c "cd $E3_HOME ; \
      ln -s $ESB_NAME $SMX_LINK_DIRNAME ; \
      exit 0 "
      
    if ! [ $? = 0 ] ; then
        echo "Failed to unpack apache-servicemix" >&2
        exit 1
    fi
	
	update_smx_ssh_pwd
	
	# Replacing variable org.ops4j.pax.url.mvn.repositories with empty value => removes any external maven repository servicemix might want to use
	# Have values for variable org.ops4j.pax.url.mvn.repositories be on one single line (remove new lines) and replace that line
	echo "Patching Servicemix external maven repo file ..."
    cd $E3_HOME
    cd $SMX_LINK_DIRNAME/etc
    cp $SERVICE_MIX_MVN_REPO_FILE $SERVICE_MIX_MVN_REPO_FILE.bak
    sed -i -e 's/^[ \t]*//' $SERVICE_MIX_MVN_REPO_FILE
    sed -i -e :a -e '/\\$/N; s/\\\n//; ta' $SERVICE_MIX_MVN_REPO_FILE
    sed -i -e 's/org.ops4j.pax.url.mvn.repositories=.*$/org.ops4j.pax.url.mvn.repositories=/' $SERVICE_MIX_MVN_REPO_FILE
    if ! [ $? = 0 ] ; then
    	echo "Failed to patch external maven repo file" >&2
        exit 1
    fi
    
    # patching karaf config file
    echo 'Patching karaf config.properties file to use Felix ...'
    patch_karaf_config
    
    if ! [ $? = 0 ] ; then
        echo "Failed to patch karaf config.properties file to use Felix" >&2
        exit 1
    fi
    
    # Unzip external dependencies into system folder (default karaf repository)
	# Allows to get rid of any internet repository dependecy
	echo "Adding external dependencies to default karaf repository ..."
	tar xvfz $DEP_ARCHIVE -C $E3_HOME/$ESB_NAME/system --strip-components=1
	chown -R $USER:$GROUP $E3_HOME/$ESB_NAME/system
    chmod a+rwx $E3_HOME/$ESB_NAME/system
    if ! [ $? = 0 ] ; then
        echo "Failed to add external maven dependencies for Servicemix" >&2
        exit 1
    fi
    
    # starting karaf under $USER user
    su - $USER -c "echo 'Starting karaf...' ; \
    $E3_HOME/$SMX_LINK_DIRNAME/bin/start ; \
    if ! [ $? = 0 ] ; then \
        echo 'Karaf startup seems to have failed' ; \
        exit 1 ; \
    fi ; \
    exit 0"
    
    # waiting for karaf
    echo "Waiting for karaf..."
    sh $DIR/wait-karaf.sh

    echo 'Wait for ServiceMix normal to properly init...'
	test_servicemix_ok
    
    # installing karaf-wrapper
    echo 'Installing wrapper feature...'
    $E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER 'features:install wrapper'
    if ! [ $? = 0 ] ; then
        echo 'Failed to install wrapper feature'
        exit 1
    fi
    
    # generating wrapper service stuff
    echo 'Generating wrapper service stuff...'
    $E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER 'wrapper:install -n karaf -d karaf -D KarafFuseESBService'
    if ! [ $? = 0 ] ; then
        echo 'Failed to generate wrapper service stuff'
        exit 1
    fi

    # install patched version of http feature   
    # add features url
    echo 'Adding patched features list...'
    $E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER "features:addurl file:$FEATURES"
    if ! [ $? = 0 ] ; then
        echo 'Failed to add patched features list'
        exit 1
    fi

    # refresh feature urls
    echo 'Refreshing feature urls...'
    $E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER 'features:refreshurl'
    if ! [ $? = 0 ] ; then
        echo 'Failed to refresh feature urls'
        exit 1
    fi

    # uninstall http feature
    echo 'Uninstalling http feature...'
    $E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER 'features:uninstall http'
    if ! [ $? = 0 ] ; then
        echo 'Failed to uninstall http feature'
        exit 1
    fi

    # install patched http feature
    echo 'Installing patched http feature...'
    $E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER 'features:install http-patched'
    if ! [ $? = 0 ] ; then
        echo 'Failed to install patched http feature'
        exit 1
    fi

    # stopping karaf
    echo 'Stopping karaf...'
    $E3_HOME/$SMX_LINK_DIRNAME/bin/stop
    echo "Waiting for karaf be down..."
    sh $DIR/wait-karaf-down.sh
    if [ $? != 0 ]
	then
		echo "Unable to stop karaf, kill all servicemix instances and try again, exiting..."
	    exit 1
	fi
    
    # patching karaf-wrapper.conf
    echo 'Patching karaf-wrapper.conf...'
    patch_karaf_wrapper
    
    if ! [ $? = 0 ] ; then
        echo "Failed to install and configure karaf-wrapper" >&2
        exit 1
    fi
    
    echo "Installing karaf as service"
    ln -s $E3_HOME/$SMX_LINK_DIRNAME/bin/karaf-service /etc/init.d/
    
    if grep -qE '#RUN_AS_USER=' $E3_HOME/$SMX_LINK_DIRNAME/bin/karaf-service ; then
        echo "Updating init.d script to set RUN_AS_USER=$USER..."
        EXPR="s/#RUN_AS_USER=/RUN_AS_USER=$USER/"
        sed -i -e $EXPR $E3_HOME/$SMX_LINK_DIRNAME/bin/karaf-service
        echo "Updating the start priority of the service"
        sed -i -e 's/# chkconfig: 2345 20 80/# chkconfig: 2345 99 80/' $E3_HOME/$SMX_LINK_DIRNAME/bin/karaf-service
    fi
} # end universal_esb

function redhat_initd {
    echo "Enabling karaf-service..."
    chkconfig karaf-service --add
    chkconfig karaf-service on
       
    echo "Starting karaf-service..."
    /etc/init.d/karaf-service start
    
} # end redhat_initd


function redhat_filelimits {
    if ! grep -qE 'fs.file-max' /etc/sysctl.conf ; then
        echo "Updating file-limits configuration..."
        echo "Patching /etc/sysctl.conf"
        echo "fs.file-max = $FILE_MAX" >> /etc/sysctl.conf
    fi
    
    if ! grep -qE "hard\s*nofile\s*$FILE_LIMITS" /etc/security/limits.conf ; then
        echo "Patching /etc/security/limits.conf (hard limit)"
        sed -i -e "/# End of file/ i \*        hard    nofile        $FILE_LIMITS" /etc/security/limits.conf
    fi
    
    if ! grep -qE "soft\s*nofile\s*$FILE_LIMITS" /etc/security/limits.conf ; then
        echo "Patching /etc/security/limits.conf (soft limit)"
        sed -i -e "/# End of file/ i \*        soft    nofile        $FILE_LIMITS" /etc/security/limits.conf
    fi
    
    # set file max
     if [ `cat /proc/sys/fs/file-max` -ne $FILE_MAX ] ; then 
        echo "Patching /proc/sys/fs/file-max"
        echo $FILE_MAX > /proc/sys/fs/file-max
    fi
    
} # end redhat_filelimits

function test_servicemix_ok {
	for n in `seq 1 20`
	do
	    result=`$E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER list | grep "camel" | grep Active`
	    if [[ $result = *[Active* ]]
	    then
           echo 'ServiceMix init done'
           break
	    else
           echo 'ServiceMix still initializing: wait 5s and retry ('$n' of 20)'
           sleep 5
	    fi
	done
} # end test_servicemix_ok

function disable_xinetd {
	echo "Disabling xinetd daemon ..."
	if [ -e /etc/init.d/xinetd ]
	then
		echo "  Stopping xinetd daemon ..."
		/etc/init.d/xinetd stop
		echo "  xinetd stopped."
		chkconfig --del xinetd
		# The following call to --list must fail
		# 'cause we have unregistered it
		chkconfig --list xinetd 2> /dev/null
		if [ $? = 0 ] ; then
			echo "  unabled to unregister xinetd daemon."
			exit 1
		else
			echo "  xinetd daemon unregistered."
		fi
	else
		echo "  xinetd does not appear to be present."
	fi
	
	echo "xinetd daemon disabled."
} # end disable_xinetd

function disable_xdmcp {
	echo "Disabling xdmcp protocol ..."
	if [ -e /etc/gdm/custom.conf ]
	then
		echo "  Stopping gdm daemon ..."
		gdm-stop > /dev/null 2>&1
		echo "  gdm stopped."
		
		echo "  Changing /etc/gdm/custom.conf if necessary ..."
		if [ -z "`grep -i 'Enable=true' /etc/gdm/custom.conf`" ]
		then
			echo "  No change to apply."
		else
			sed -i 's/enable=true/Enable=false/Ig' /etc/gdm/custom.conf
			echo "  /etc/gdm/custom.conf patched."
		fi
	else
		echo "  xdmcp does not appear to be present."
	fi
	
	echo "xdmcp protocol disabled."
} # end disable_xinetd

function check_tftp_daemon {
	echo "Checking tftp daemon configuration ..."
	if [ -e /etc/xinetd.d/tftp ]
	then
		if [ -z "`grep 'server_args' /etc/xinetd.d/tftp`" ]
		then
			echo "  Configuration file not conform."
			exit 1
		fi
		# Applying patch
		sed -i 's/\(server_args.*=\).*/\1 -s\ \/tftpboot/g' /etc/xinetd.d/tftp
		if [ $? = 0 ]
		then
			echo "  Configuration file patched."
		else
			echo "  Error while patching /etc/xinetd.d/tftp file."
			exit 1
		fi
	else
		echo "  tftp configuration does not appear to be present."
	fi
	
	echo "tftp daemon configuration checked."
} # end disable_xinetd

# Begginning of the script

if [ -e "/etc/redhat-release" ] ; then
    if [ "$(uname -m)" = "x86_64" ] ; then
        redhat_java $E3_JAVA_64_BIN $E3_JAVA_64_MD5 
    else
        redhat_java $E3_JAVA_32_BIN $E3_JAVA_32_MD5
    fi
    
    # file-limits patch
    redhat_filelimits

    if [ -e $DIR/install_snmp.sh ] ; then
        sh $DIR/install_snmp.sh
        if [ $? != 0 ]
        then
            exit 1
        fi
    fi
 
    # installing fuse esb
    universal_esb $E3_FUSE_GZ $E3_FUSE_MD5
	
	# set fuse esb as service
    redhat_initd
	
	#
	# Applying TMO Security items:
	# 4- checking tftp daemon configuration
	check_tftp_daemon
	
    # 6- xinetd daemon disabling
    disable_xinetd
	
	# 8- xdmcp protocol disabling
	disable_xdmcp
    
    
	
    
elif [ -e "/etc/debian_version" ] ; then
    echo "Debian is not yet supported!" >&2
    exit 1
else
    echo "Unknown Linux Distro" >&2
    exit 1
fi
