#!/bin/bash

echo "Start the E3 restore"

echo "Init local variables"

ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`
INSTALL_DIR=$DIR/..
ARCHIVE_DIR_NAME="backup"
ARCHIVE_NAME="backup-e3.tar.gz"

echo "Init global variables"

source $DIR/variables.sh

echo "E3 is located to $E3_HOME"

BACKUP_DIR=$INSTALL_DIR/$ARCHIVE_DIR_NAME/
BACKUP_FILE=$BACKUP_DIR$ARCHIVE_NAME

echo "Stopping E3 Manager"
service karaf-service stop

MANAGER_STATUS=`service karaf-service status`

if [ "$MANAGER_STATUS" == "karaf is not running." ]
then
    echo "The E3 Manager is stopped"
else
    echo "Unable to stop the E3 Manager: Error"
        exit 1
fi

echo "Check if backup file exist : $BACKUP_FILE"

if [ -f $BACKUP_FILE ]
then
    echo "Check Backup: ok"

        echo "Entering backup directory $BACKUP_DIR"
        cd $BACKUP_DIR

        echo "Clean useless files"
        rm $BACKUP_DIR*.properties
        rm $BACKUP_DIR*.xml

        echo "Unpack the backup archive $BACKUP_FILE"
        tar xvf $ARCHIVE_NAME

		echo "Adjust rights on files"
		chown $USER:$GROUP *.properties
		chown $USER:$GROUP *.xml
		chmod 664 *.xml
		chmod 664 *.properties
		
        echo "Restore files from the backup $BACKUP_DIR to $E3_HOME"
        cp -pf *.xml $E3_HOME
        cp -pf *.properties $E3_HOME
		
        echo "Restore done"

else
        echo "Backup file does not exsit: Error"
        exit 2
fi

echo "Start E3 Manager"
service karaf-service start

MANAGER_STATUS=`service karaf-service status`

if [ "$MANAGER_STATUS" != "karaf is not running." ]
then
    echo "The E3 Manager is started"
else
    echo "Unable to start the E3 Manager: Error"
        exit 3
fi

echo "Restore done with success"
exit 0
