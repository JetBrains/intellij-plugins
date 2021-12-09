#!/bin/bash
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";

mkdir -p "$DIR/work"

cd work

# Update or checkout repositories
if [ ! -d "vscSnippetsConverter" ]; then
  git clone https://github.com/denofevil/vscSnippetsConverter.git vscSnippetsConverter
else
  cd vscSnippetsConverter
  git pull
  cd ..
fi

if [ ! -d "vue-vscode-snippets" ]; then
  git clone https://github.com/sdras/vue-vscode-snippets.git vue-vscode-snippets
else
  cd vue-vscode-snippets
  git reset --hard
  git pull
  cd ..
fi

rm -f "$DIR/work/vue-vscode-snippets/snippets/nuxt-"*
rm -f "$DIR/work/vue-vscode-snippets/snippets/ignore.json"
rm -f "$DIR/work/vue-vscode-snippets/snippets/vue-pug.json"

sed -i "" "s/3:styleObjectB]}/3:styleObjectB}]/g" "$DIR/work/vue-vscode-snippets/snippets/vue-template.json"

node vscSnippetsConverter/index.js "$DIR/work/vue-vscode-snippets/snippets" "$DIR/liveTemplatesConfig.json" > "$DIR/gen/liveTemplates/Vue.xml"
