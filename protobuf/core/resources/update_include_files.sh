#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
mkdir -p "${DIR}/include/google/protobuf"
cd "${DIR}/include/google/protobuf"

for file in any api descriptor duration empty field_mask source_context struct timestamp type wrappers; do
  wget "https://raw.githubusercontent.com/protocolbuffers/protobuf/master/src/google/protobuf/${file}.proto" -O "${file}.proto"
done
