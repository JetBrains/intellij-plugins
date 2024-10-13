#! /bin/sh
set -eu

xmllint --quiet --nonet --noout --schema inspection-profile.xsd .idea/inspectionProfiles/*.xml
