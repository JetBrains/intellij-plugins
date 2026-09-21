#!/bin/sh
# Regenerates the Python code in gen/ from the .proto files in proto/.
cd "$(dirname "$0")" || exit 1
protoc --proto_path=./proto --python_out=./gen --pyi_out=./gen ./proto/*.proto
