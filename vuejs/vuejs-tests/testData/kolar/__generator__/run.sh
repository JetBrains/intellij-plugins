#!/bin/bash
set -e

npm i
cd vue-transpiled-data-plugin/
npm run build
cd ../

node prepare.ts
cd ../__temp__
sh generate-all.sh

cd ../
rm -rf __temp__/