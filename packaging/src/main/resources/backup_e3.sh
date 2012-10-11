#!/bin/bash

echo "Start Backup e3 process"

# get the current folder

echo "Init local variables"



ABS_PATH_NAME=$(readlink -f $0)
DIR=`dirname $ABS_PATH_NAME`
INSTALL_DIR=$DIR/..
ARCHIVE_DIR_NAME="backup"
ARCHIVE_NAME="backup-e3.tar.gz"

# init common vars like the e3 home directory

echo "Init global variables"

source $DIR/variables.sh

echo "E3 is located to $E3_HOME"

# create a clean directory backup
BACKUP_DIR=$INSTALL_DIR/$ARCHIVE_DIR_NAME/

echo "Create and clean directory backup $BACKUP_DIR"

rm -rf $BACKUP_DIR
mkdir -p $BACKUP_DIR

# go to e3 backup directory

echo "Entering $BACKUP_DIR"

cd $BACKUP_DIR

# backup a copy of files

CURRENT_DIR=`pwd`

echo "Copy configuration files from $E3_HOME to $CURRENT_DIR"

cp $E3_HOME/topology.xml .
cp $E3_HOME/system_topology.xml .
cp $E3_HOME/configuration.properties .

# create archive

ARCHIVE_NAME="backup-e3.tar.gz"

echo "Create backup archive $CURRENT_DIR/$ARCHIVE_NAME"

tar zcf backup-e3.tar.gz *

echo "Backup done with success"
exit 0
