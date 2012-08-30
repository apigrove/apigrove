#!/bin/sh


sh install_e3_aib.sh

if [ $? != 0 ]
then
    echo "Upgrade failed, exiting"
    exit 1
fi

