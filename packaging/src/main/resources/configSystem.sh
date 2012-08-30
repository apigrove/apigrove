#!/bin/bash

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh
export JAVA_HOME=/usr/java/default

# Non-Java (syslog) Logging:
# Setup initial syslog configuration, and give the e3 user permissions to change things
function e3_syslog_setup {

    E3_SYSLOG_FACILITY="local3"
    SYSLOG_FILE="/var/log/e3syslog.log"
    TMP_DIR="$E3_HOME/tmp"
    E3_SECTION_START_LINE="# E3 NON-JAVA LOGGING BEGIN"
    E3_SECTION_END_LINE="# E3 NON-JAVA LOGGING END"
    
    # Determine if syslog config is at /etc/syslog.conf or /etc/rsyslog.conf
    echo "Setting up E3-specific syslog logging"
    SYSLOG="UNDETERMINED"
    if [ -f /etc/rsyslog.conf ]
    then
        SYSLOG="rsyslog"
    elif [ -f /etc/syslog.conf ]
    then
        SYSLOG="syslog"
    else
        echo "Could not find syslog configuration!"
        return
    fi

    # Modify sudoers so e3 can adjust log level settings
    # If our changes from a previous install are present, delete them
    cp /etc/sudoers /etc/sudoers.e3save
    chown --reference=/etc/sudoers /etc/sudoers.e3save
    START_LINE_NO=`grep -n "$E3_SECTION_START_LINE" /etc/sudoers | cut -d ":" -f1`
    END_LINE_NO=`grep -n "$E3_SECTION_END_LINE" /etc/sudoers | cut -d ":" -f1`
    if [ ! -z $START_LINE_NO ] && [ ! -z $END_LINE_NO ] && [ $START_LINE_NO -lt $END_LINE_NO ]
    then
        echo "Deleting previous E3-specific rules from /etc/sudoers (lines $START_LINE_NO to $END_LINE_NO)"
        sed -i "/$E3_SECTION_START_LINE/,/$E3_SECTION_END_LINE/d" /etc/sudoers
    else
        # On a new install, make a little space 
        echo "" >> /etc/sudoers    
    fi
        
    echo "Writing changes to /etc/sudoers"
    cat >> /etc/sudoers <<-EOF    
	$E3_SECTION_START_LINE 
	Defaults:e3		!requiretty
	e3	ALL=(ALL)	NOPASSWD:/bin/cp $TMP_DIR/e3syslog.conf /etc/${SYSLOG}.conf
	e3	ALL=(ALL)	NOPASSWD:/etc/init.d/$SYSLOG restart
	e3	ALL=(ALL)	NOPASSWD:/bin/cp $TMP_DIR/e3logrotate.conf /etc/logrotate.d/e3
	$E3_SECTION_END_LINE
	EOF
	
    # Create an e3-specific logrotate file
    echo "Initializing logrotate config at /etc/logrotate.d/e3"
    cat > /etc/logrotate.d/e3 <<-EOF
	$SYSLOG_FILE {
	    missingok
	    notifempty
	    daily
	    create 0660 e3 root
	    dateext
	    dateformat .%Y%m%d
	}
	EOF
	
    # Make the logrotate file writable by user e3
    chown e3:root /etc/logrotate.d/e3
    chmod 0660 /etc/logrotate.d/e3

    # Modify syslog config to write to an e3-specific logfile
    # If our changes from a previous install are present, delete them
    cp /etc/${SYSLOG}.conf /etc/${SYSLOG}.conf.e3save
    chown --reference=/etc/${SYSLOG}.conf /etc/${SYSLOG}.conf.e3save
    START_LINE_NO=`grep -n "$E3_SECTION_START_LINE" /etc/${SYSLOG}.conf | cut -d ":" -f1`
    END_LINE_NO=`grep -n "$E3_SECTION_END_LINE" /etc/${SYSLOG}.conf | cut -d ":" -f1`
    if [ ! -z $START_LINE_NO ] && [ ! -z $END_LINE_NO ] && [ $START_LINE_NO -lt $END_LINE_NO ]
    then
        echo "Deleting previous E3-specific rules from /etc/${SYSLOG}.conf (lines $START_LINE_NO to $END_LINE_NO)"
        sed -i "/$E3_SECTION_START_LINE/,/$E3_SECTION_END_LINE/d" /etc/${SYSLOG}.conf
    else
        # On a new install, make a little space 
        echo "" >>/etc/${SYSLOG}.conf
    fi
    
    echo "Writing changes to /etc/${SYSLOG}.conf"    
    cat >> /etc/${SYSLOG}.conf <<-EOF    
	$E3_SECTION_START_LINE
	# E3-specific rule: do not change the facility ($E3_SYSLOG_FACILITY) or log-file path without reading logging documentation
	${E3_SYSLOG_FACILITY}.DEBUG						$SYSLOG_FILE
	$E3_SECTION_END_LINE
	EOF

    # Initialize syslog file so that the e3 user can read
    touch $SYSLOG_FILE
    chown e3:root $SYSLOG_FILE
    chmod 0660 $SYSLOG_FILE
    
    # Restart syslog daemon to activate new settings, and log one line to test the setup
    echo "Restarting syslog daemon"
	/etc/init.d/$SYSLOG restart
	sleep 2
	logger -p ${E3_SYSLOG_FACILITY}.debug -t "E3" "E3-specific syslog logging configured for facility: $E3_SYSLOG_FACILITY"

} # end e3_syslog_setup

# Set up E3 (com.alu.e3) Java logging
function e3_paxlogging_setup {

    # Modify servicemix's config to log E3 java output (from com.alu.e3)
    # to a specific location and with specific settings
    SMX_DIR="$E3_HOME/$SMX_LINK_DIRNAME"
    SMX_CFG="$SMX_DIR/etc/org.ops4j.pax.logging.cfg"
    E3_SECTION_START_LINE="# E3 appender start"
    E3_SECTION_END_LINE="# E3 appender end"
    
    if [ ! -f $SMX_CFG ]
    then
        echo "Could not find ServiceMix logging configuration where expected: $SMX_CFG"
        return
    fi

	# Save a version of config file before our modifications
    cp $SMX_CFG ${SMX_CFG}.e3save
    chown --reference=$SMX_CFG ${SMX_CFG}.e3save

    # If our changes from a previous install are present, delete them
    START_LINE_NO=`grep -n "$E3_SECTION_START_LINE" $SMX_CFG | cut -d ":" -f1`
    END_LINE_NO=`grep -n "$E3_SECTION_END_LINE" $SMX_CFG | cut -d ":" -f1`
    if [ ! -z $START_LINE_NO ] && [ ! -z $END_LINE_NO ] && [ $START_LINE_NO -lt $END_LINE_NO ]
    then
        echo "Deleting previous E3-specific rules from $SMX_CFG (lines $START_LINE_NO to $END_LINE_NO)"
        sed -i "/$E3_SECTION_START_LINE/,/$E3_SECTION_END_LINE/d" $SMX_CFG
    else
        # On a new install, make a little space 
        echo "" >>$SMX_CFG
    fi
    
    # Make sure SMX appender uses daily-rolling
    echo "Writing changes to $SMX_CFG" 
    sed -i "s/^log4j.appender.out=org.apache.log4j.RollingFileAppender/log4j.appender.out=org.apache.log4j.DailyRollingFileAppender\nlog4j.appender.out.DatePattern='.'yyyy-MM-dd-HH/
    	/^log4j.appender.out.maxFileSize=/d
    	/^log4j.appender.out.maxBackupIndex=/d" $SMX_CFG 
        
    # Add new E3Appender section 
    cat >> $SMX_CFG <<-EOF          
	$E3_SECTION_START_LINE
	log4j.appender.E3Appender=org.apache.log4j.DailyRollingFileAppender
	log4j.appender.E3Appender.layout=org.apache.log4j.PatternLayout
	log4j.appender.E3Appender.layout.ConversionPattern= %-5p %d %-20c [%L] - %m%n
	log4j.appender.E3Appender.file=$SMX_DIR/data/log/e3.log
	log4j.appender.E3Appender.append=true
	log4j.appender.E3Appender.DatePattern='.'yyyy-MM-dd-HH
	log4j.appender.E3Appender.threshold=INFO
	log4j.logger.com.alu.e3=INFO, E3Appender
	log4j.additivity.com.alu.e3=false
	$E3_SECTION_END_LINE
	EOF

} # end e3_paxlogging_setup


# Beginning of the script

if [[ $# < 1 ]]
then
    echo "Missing input parameter."  
    exit 1
fi

serviceMixOk=0
for n in `seq 1 2`
do
    $E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER " "
    if [[ ${?} != 0 ]]
    then
       # unable to connect: wait and retry
       sleep 3 
    else
       serviceMixOk=1
       break
    fi

done

if [[ $serviceMixOk == 0 ]]
then
    echo "Servicemix not started -- trying to start "
    service karaf-service start
    sleep 5
fi

eval '$E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER "features:addurl file:$FEATURES"' 2>&1
if [[ ${?} != 0 ]]
then
    echo "Unable to add the feature url"
    exit 1
fi

sleep 2
eval '$E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER "features:uninstall E3"' 2>&1
if [[ ${?} != 0 ]]
then
    echo "Unable to uninstall the feature: E3"
    exit 1
fi
sleep 2
eval '$E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER "features:install E3"' 2>&1
if [[ ${?} != 0 ]]
then
    echo "Unable to install the feature: E3"
    exit 1
fi

# refreshing fuse feature urls
eval '$E3_HOME/$SMX_LINK_DIRNAME/bin/client -p $SMX_PWD -u $SMX_USER "features:refreshurl"' 2>&1
if [[ ${?} != 0 ]]
then
        echo "Unable to fresh feature urls"
        exit 1
fi

# Affect hostname manually if respond on 127.0.0.1 for RMI connections
if  !  grep -lir "Binding external IP version 1.0" /etc/rc.local
then
	cat >> /etc/rc.local <<'HEREDOC'
# Affect hostname manually if respond on 127.0.0.1 for RMI connections
# Binding external IP version 1.0
if [ `hostname -i` = "127.0.0.1" ]
then
    if [ ! -f /root/e3ipaddresshistory ]
    then
        oldipaddress=`ifconfig eth0 | sed -n 's/.*inet *addr:\([0-9\.]*\).*/\1/p'`
    else
        oldipaddress=`cat /root/e3ipaddresshistory`
    fi
    
    echo `ifconfig eth0 | sed -n 's/.*inet *addr:\([0-9\.]*\).*/\1/p'` > /root/e3ipaddresshistory
    
    hostnameline=`sed -n "s/$oldipaddress\(.*\)/\1/p" /etc/hosts`
    hostnameline=`echo $hostnameline`
    if [ "$hostnameline" = "e3hostname" ]
    then
        sed -i '/e3hostname/d' /etc/hosts
    else
        sed -i 's/e3hostname//' /etc/hosts
    fi
    
    ipaddress=`ifconfig eth0 | sed -n 's/.*inet *addr:\([0-9\.]*\).*/\1/p'`
    
    ipline=`sed -n "s/\($ipaddress\)/\1/p" /etc/hosts`
    if [ "$ipline" = "" ]
    then
        echo "$ipaddress  e3hostname" >> /etc/hosts
    else
        sed -i "/$ipaddress/ s/\(.*[a-zA-Z0-9]\)\( *$\)/\1 e3hostname/g" /etc/hosts
    fi
    
    hostname e3hostname
    /etc/init.d/karaf-service restart
fi
HEREDOC
    /etc/rc.local
fi

# Logging setup for both E3-specific syslog and Java logging
e3_syslog_setup
e3_paxlogging_setup

exit 0
