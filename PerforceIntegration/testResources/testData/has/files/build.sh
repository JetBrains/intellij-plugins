#!/usr/bin/env bash
#
# Configure the p4d instance during build time.
#
set -e

# would be nice to get the P4PORT passed to this script
export P4PORT=0.0.0.0:1666
export P4USER=super
P4PASSWD=Password

# start the server so we can populate it with data
p4dctl start -o '-p 0.0.0.0:1666' main
echo ${P4PASSWD} | p4 login

#
# Allow fallback to database password auth
#
p4 configure set auth.sso.allow.passwd=1

# disable the signed extensions requirement for testing
p4 configure set server.extensions.allow.unsigned=1

#
# install and configure the extension
#
p4 extension --package loginhook
p4 extension --install loginhook.p4-extension -y
rm -f loginhook.p4-extension
p4 extension --configure Auth::loginhook -o | ./ext_config.awk | p4 extension --configure Auth::loginhook -i
p4 extension --configure Auth::loginhook --name loginhook-all -o | ./ext_config.awk | p4 extension --configure Auth::loginhook --name loginhook-all -i

#
# populate p4d with test data
#
p4 user -f -i < user_jack.txt
yes 94f6ce8c-fbea-4fcd-b7d0-564de93beb1b | p4 passwd jack

p4 user -f -i < user_john.txt
yes 18873fa3-1918-43ca-a518-c706def5e07f | p4 passwd john

p4 user -f -i < user_swarm.txt
yes ${P4PASSWD} | p4 passwd swarm

#
# create a group with long lived tickets, log in again
#
p4 group -i < group_unlimited.txt
p4 logout
echo ${P4PASSWD} | p4 login

#
# give the swarm user admin and super protections
#
p4 protect -o > protects.txt
echo '	admin user swarm * //...' >> protects.txt
echo '	super user swarm * //...' >> protects.txt
p4 protect -i < protects.txt

#
# Unzip test project, create workspace 'john_ws' for user 'john', add and submit all files to Perforce
#
WS_ROOT=/perforce/ws
WS_ALT_ROOT='C:\perforce\ws'
ZIP_FILE=/perforce/ConsoleApp1.zip
JOHN_USER=john
JOHN_PASS=18873fa3-1918-43ca-a518-c706def5e07f
JOHN_CLIENT=john_ws

mkdir -p "${WS_ROOT}"
unzip -q "${ZIP_FILE}" -d "${WS_ROOT}"
echo "${JOHN_PASS}" | p4 -p "${P4PORT}" -u "${JOHN_USER}" login
printf "Client: %s\nOwner: %s\nHost: \nRoot: %s\nAltRoots:\n\t%s\nOptions: noallwrite noclobber nocompress unlocked nomodtime normdir\nLineEnd: local\nView:\n\t//depot/... //%s/...\n" "${JOHN_CLIENT}" "${JOHN_USER}" "${WS_ROOT}" "${WS_ALT_ROOT}" "${JOHN_CLIENT}" | p4 -p "${P4PORT}" -u "${JOHN_USER}" client -i
cd "${WS_ROOT}"
p4 -p "${P4PORT}" -u "${JOHN_USER}" -c "${JOHN_CLIENT}" add -f ...
p4 -p "${P4PORT}" -u "${JOHN_USER}" -c "${JOHN_CLIENT}" submit -d "Initial import of ConsoleApp1 test data"

#
# stop the server so that the run script can start it again,
# and the authentication changes will take effect
#
p4dctl stop main
