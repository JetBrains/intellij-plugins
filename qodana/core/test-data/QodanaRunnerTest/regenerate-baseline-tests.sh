#! /bin/bash
set -eu

read -r -p "Modify *.baseline to your liking, then press Enter"

for dir in ./baseline*; do
  for file in "$dir"/*.baseline "$dir"/*/*.baseline; do
    [ -f "$file" ] && cp "$file" "${file%.baseline}"
  done
  rm "$dir"/baseline-results.sarif.json "$dir"/expected.sarif.json
done

read -r -p "Run QodanaRunnerTest, it will fail, then press Enter"

for dir in ./baseline*; do
  mv "$dir"/expected.sarif.json "$dir"/baseline-results.sarif.json
  for file in "$dir"/*.baseline "$dir"/*/*.baseline; do
    [ -f "$file" ] && git restore "${file%.baseline}"
  done
done

read -r -p "Modify non-baseline files to your liking, then press Enter"

read -r -p "Run QodanaRunnerTest, it will fail, then press Enter"

read -r -p "Run QodanaRunnerTest again, then press Enter"
