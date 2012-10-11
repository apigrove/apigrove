#!/bin/bash

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`

source $DIR/variables.sh

if [[ $# < 1 ]]
then
    echo "Missing input parameter."
    exit 1
fi

# build firewall restriction if manager ip is passed
if [ "$1" = "-ip" ]
then
	SOURCE_RESTICT="-s $2"
	echo "Will apply source restriction $SOURCE_RESTICT on all rules concerning manager-gateway communication"
	# ignore the first two parameters (-ip option and ip itself) for the rest of the script
	shift 2
fi

# create configuration file indicating mode (manager, gateway or both)
if [ ! -f $MODE_CONF_PATH ]
then
	echo "E3 mode configuration file does not exist. Creating it in $MODE_CONF_PATH"
	
	if [ -f $DIR/configuration-defaults.properties ]
	then
	    cp $DIR/configuration-defaults.properties $MODE_CONF_PATH
	fi
	
	if [ -f $DIR/configuration.properties ]
	then
	    cp $DIR/configuration.properties $MODE_CONF_PATH
	fi
	
    if [ ! -f $MODE_CONF_PATH ]
	then
		echo "Unable to create $MODE_CONF_PATH. Exiting ..."
	    exit 1
	fi
fi
chown $USER:$GROUP $MODE_CONF_PATH
chmod 600 $MODE_CONF_PATH

# change configuration file values according to parameters
for ARG in "$@"
do
	if [ "$ARG" == "gateway" ]
	then
		sed -i -e 's/^\(e3.gateway=.*\)$/e3.gateway=true/' $MODE_CONF_PATH
	fi
	
	if [ "$ARG" == "manager" ]
	then
		sed -i -e 's/^\(e3.manager=.*\)$/e3.manager=true/' $MODE_CONF_PATH
		if [ $PROV_REST_API_BASICAUTH_ENABLE = 1 ] 
		then
			sed -i -e 's/^\(e3.provisioning.restapi.security.basicauth.enable=.*\)$/e3.provisioning.restapi.security.basicauth.enable=true/' $MODE_CONF_PATH
			sed -i -e "s/^\(e3.provisioning.restapi.security.basicauth.username=.*\)$/e3.provisioning.restapi.security.basicauth.username=$PROV_REST_API_BASICAUTH_USERNAME/" $MODE_CONF_PATH
			sed -i -e "s/^\(e3.provisioning.restapi.security.basicauth.password=.*\)$/e3.provisioning.restapi.security.basicauth.password=$PROV_REST_API_BASICAUTH_PASSWORD/" $MODE_CONF_PATH
		else		
			sed -i -e 's/^\(e3.provisioning.restapi.security.basicauth.enable=.*\)$/e3.provisioning.restapi.security.basicauth.enable=false/' $MODE_CONF_PATH
			sed -i -e "s/^\(e3.provisioning.restapi.security.basicauth.username=.*\)$/e3.provisioning.restapi.security.basicauth.username=/" $MODE_CONF_PATH
			sed -i -e "s/^\(e3.provisioning.restapi.security.basicauth.password=.*\)$/e3.provisioning.restapi.security.basicauth.password=/" $MODE_CONF_PATH
		fi
		
	fi
done

# Install the transfer of the TDRs for gateway
if [ -e $DIR/installTdrTransfer.sh ] ; then
	for ARG in "$@"
	do
		if [ "$ARG" == "gateway" ]
		then
			sh $DIR/installTdrTransfer.sh
			if [ $? != 0 ]
			then
				echo "Unable to install the TDR transfer"
				exit 1
			fi
		fi
	done
fi


# Set up pax-web settings
function e3_paxweb_setup {


    SMX_DIR="$E3_HOME/$SMX_LINK_DIRNAME"
    SMX_CFG="$SMX_DIR/etc/$SERVICE_MIX_PAX_WEB_CFG_FILE"
    
	E3_SECTION_START_LINE="# E3 setup start"
    E3_SECTION_END_LINE="# E3 setup end"
	
    if [ ! -f $SMX_CFG ]
    then
        echo "Could not find ServiceMix logging configuration where expected: $SMX_CFG"
        return
    fi
	
	
    # If our changes from a previous install are present, delete them
    cp $SMX_CFG ${SMX_CFG}.bak
    chown --reference=$SMX_CFG ${SMX_CFG}.bak
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
    
    echo "Writing changes to $SMX_CFG" 
	if [ $PROV_REST_API_HTTP_ENABLE = 1 ]  && [ $PROV_REST_API_HTTPS_ENABLE = 1 ] 
	then
		cat >> $SMX_CFG <<-EOF          
	$E3_SECTION_START_LINE
    org.osgi.service.http.enabled=true
	org.osgi.service.http.secure.enabled=true
	org.ops4j.pax.web.ssl.keystore=$PROV_REST_API_KEYSTORE_PATH
	org.ops4j.pax.web.ssl.password=$PROV_REST_API_KEYSTORE_PASSWORD
	org.ops4j.pax.web.ssl.keypassword=$PROV_REST_API_KEYSTORE_KEYPASSWORD
	org.ops4j.pax.web.ssl.clientauthwanted=false
	org.ops4j.pax.web.ssl.clientauthneeded=false
	$E3_SECTION_END_LINE
	EOF
	fi
	if [ $PROV_REST_API_HTTP_ENABLE = 0 ]  && [ $PROV_REST_API_HTTPS_ENABLE = 1 ] 
	then
		cat >> $SMX_CFG <<-EOF          
	$E3_SECTION_START_LINE
	org.osgi.service.http.enabled=false
	org.osgi.service.http.secure.enabled=true
	org.ops4j.pax.web.ssl.keystore=$PROV_REST_API_KEYSTORE_PATH
	org.ops4j.pax.web.ssl.password=$PROV_REST_API_KEYSTORE_PASSWORD
	org.ops4j.pax.web.ssl.keypassword=$PROV_REST_API_KEYSTORE_KEYPASSWORD
	org.ops4j.pax.web.ssl.clientauthwanted=false
	org.ops4j.pax.web.ssl.clientauthneeded=false
	$E3_SECTION_END_LINE
	EOF
	fi 
	if [ $PROV_REST_API_HTTP_ENABLE = 1 ]  && [ $PROV_REST_API_HTTPS_ENABLE = 0 ] 
	then
		cat >> $SMX_CFG <<-EOF          
	$E3_SECTION_START_LINE
	org.osgi.service.http.enabled=true
	org.osgi.service.http.secure.enabled=false
	$E3_SECTION_END_LINE
	EOF
	fi  
	if [ $PROV_REST_API_HTTP_ENABLE = 0 ]  && [ $PROV_REST_API_HTTPS_ENABLE = 0 ] 
	then
		echo "WARNING: both http and https are disabled to access REST API !!!"
	fi    

} # end e3_paxweb_setup

IS_GATEWAY=0
IS_MANAGER=0
IS_SPEAKER=0

# change configuration file values according to parameters
for ARG in "$@"
do
	if [ "$ARG" = "gateway" ]
	then
		IS_GATEWAY=1
		IS_SPEAKER=1
	fi
	
	if [ "$ARG" = "manager" ]
	then
		IS_MANAGER=1
	fi
done

iptables -D E3-Firewall-INPUT -j REJECT --reject-with icmp-host-prohibited

if [ $IS_GATEWAY = 1 ] ; then
	if [ ! $IS_E3_AIB ] ; then
		iptables -A E3-Firewall-INPUT -p tcp --dport 15701 -j ACCEPT $SOURCE_RESTICT
	fi

	# Block 25100 and 25101 in nat table here because they must be accepted
	# further by firewall rules after redirection
	iptables -t nat -A PREROUTING -p tcp --dport 25100 -j REJECT --reject-with icmp-host-prohibited
	iptables -t nat -A PREROUTING -p tcp --dport 25101 -j REJECT --reject-with icmp-host-prohibited
	iptables -t nat -A PREROUTING -p tcp --dport 80 -j REDIRECT --to-port 25100
	iptables -t nat -A PREROUTING -p tcp --dport 443 -j REDIRECT --to-port 25101

	iptables -A E3-Firewall-INPUT -p tcp --dport 25100 -j ACCEPT
	iptables -A E3-Firewall-INPUT -p tcp --dport 25101 -j ACCEPT
	iptables -A OUTPUT -t nat -p TCP --dport 25100 -j REDIRECT --to-ports 80
	iptables -A OUTPUT -t nat -p TCP --dport 25101 -j REDIRECT --to-ports 443
fi

if [ $IS_SPEAKER = 1 ] ; then
	if [ ! $IS_E3_AIB ] ; then
		iptables -A E3-Firewall-INPUT -p tcp --dport 15701 -j ACCEPT $SOURCE_RESTICT
	fi
fi

ipList=
nbElem=0
if [ $PROV_REST_API_IPWHITELIST_ENABLE = 1 ] ; then
	echo "ip whitelist for $PROV_REST_API_IPWHITELIST"
	ipList=($PROV_REST_API_IPWHITELIST)
	nbElem=${#ipList[*]}

	if [ $nbElem = 0 ] ; then	
		echo "ERROR: PROV_REST_API_IPWHITELIST_ENABLE=1 but no ip list provided in PROV_REST_API_IPWHITELIST see varibales.sh."
		exit 1
	fi
fi
	
if [ $IS_MANAGER = 1 ] ; then
	e3_paxweb_setup
	if [ $PROV_REST_API_HTTP_ENABLE = 1 ] ; then
		echo "HTTP port 8181 enabled"		
		if [ $nbElem = 0 ] 
		then
			iptables -A E3-Firewall-INPUT -p tcp --dport 8181 -j ACCEPT
		else
			for i in `seq 0 $(($nbElem - 1))`;
			do 		
				echo "HTTP port 8181 enabled for ip ${ipList[$i]}"		
				iptables -A E3-Firewall-INPUT -p tcp --dport 8181 -j ACCEPT -s ${ipList[$i]}
			done
		fi	
	fi
	if [ $PROV_REST_API_HTTPS_ENABLE = 1 ] ; then
		echo "HTTPS port 8443 enabled"
		if [ $nbElem = 0 ] 
		then
			iptables -A E3-Firewall-INPUT -p tcp --dport 8443 -j ACCEPT
		else
			for i in `seq 0 $(($nbElem - 1))`;
			do 		
				echo "HTTP port 8443 enabled for ip ${ipList[$i]}"		
				iptables -A E3-Firewall-INPUT -p tcp --dport 8443 -j ACCEPT -s ${ipList[$i]}
			done
		fi
	fi	
fi

if [ ! $IS_E3_AIB ] ; then
	iptables -A E3-Firewall-INPUT -p tcp --dport 8888 -j ACCEPT
	iptables -A E3-Firewall-INPUT -p tcp --dport 8889 -j ACCEPT
	iptables -A E3-Firewall-INPUT -p tcp --dport 8988 -j ACCEPT

	iptables -A E3-Firewall-INPUT -p tcp --dport 8082 -j ACCEPT $SOURCE_RESTICT
    
	if [ $IS_GATEWAY = 1 ] ; then
		iptables -A E3-Firewall-INPUT -p tcp --dport 8083 -j ACCEPT $SOURCE_RESTICT
	fi

	if [ $IS_SPEAKER = 1 ] ; then
		iptables -A E3-Firewall-INPUT -p tcp --dport 8084 -j ACCEPT $SOURCE_RESTICT
	fi
	
	if [ $IS_MANAGER = 1 ] ; then
		iptables -A E3-Firewall-INPUT -p tcp --dport 8085 -j ACCEPT $SOURCE_RESTICT
	fi
fi

# Block all other requests
iptables -A E3-Firewall-INPUT -j REJECT --reject-with icmp-host-prohibited

service iptables save
service iptables restart

exit 0
