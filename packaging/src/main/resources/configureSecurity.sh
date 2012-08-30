#!/bin/bash

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`


# Clean iptables

iptables -F
iptables -t nat -F

# Create custom firewall rule

iptables -N 'E3-Firewall-INPUT'

iptables -A INPUT -j E3-Firewall-INPUT
iptables -A FORWARD -j E3-Firewall-INPUT

# Add standard rules

iptables -A E3-Firewall-INPUT -i lo -j ACCEPT
iptables -A E3-Firewall-INPUT -p tcp -m state --state NEW -m tcp --dport 22 -j ACCEPT
iptables -A E3-Firewall-INPUT -m state --state RELATED,ESTABLISHED -j ACCEPT

# SNMP
if [ -e $DIR/install_snmp.sh ] ; then
        iptables -A E3-Firewall-INPUT -p udp --dport 161 -j ACCEPT
fi

# NetBIOS
iptables -A E3-Firewall-INPUT -p udp --dport 137 -j ACCEPT

# Block all other requests
iptables -A E3-Firewall-INPUT -j REJECT --reject-with icmp-host-prohibited

# Manager/Gateway specific rules are added in "generateNature.sh"

service iptables save
service iptables restart

exit 0
