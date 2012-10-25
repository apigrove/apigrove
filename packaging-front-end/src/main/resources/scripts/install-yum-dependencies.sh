#!/bin/bash

# ** How to create a local repository yum from the packages of the Dead Start Dvd **

source variables.sh

echo "Dependencies install started"

#  1/ Create a folder which is going to be your local repository (ex "/mnt/fc_local_repo")
mkdir /mnt/fc_local_repo

# 2/ copy all the rpms in this folder
#tar xvjf allrpm.tar.bz2
unzip $E3_RPM_ZIP
mv $E3_RPM/*.rpm /mnt/fc_local_repo

# 3/ install yum if needed
which yum &>/dev/null
if ! [ $? = 0 ] ; then
	rpm -ivh /mnt/fc_local_repo/yum-3.2.22-37.el5.noarch.rpm
fi

# 4/ install create repo from the local repository
rpm -ivh /mnt/fc_local_repo/createrepo-0.4.11-3.el5.noarch.rpm

# 5/ create the repo
createrepo /mnt/fc_local_repo/

# remove previous repo
mkdir repobkp
mv /etc/yum.repos.d/* repobkp

# 6/ Add this repo in yum
cat > /etc/yum.repos.d/local-repo.repo <<'HEREDOC'
[fclocalrepo]
name=fc local repo
baseurl=file:///mnt/fc_local_repo
enabled=1
gpgcheck=0

HEREDOC

# /mnt/fc_local_repo/ is now a local repository which is used by yum

echo "Dependencies install ended"




