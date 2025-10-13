#!/usr/bin/env bash
#
# Start the server and tail the log.
#
set -e
p4dctl start -o '-p 0.0.0.0:1666' main
exec tail -f /opt/perforce/servers/main/logs/log
