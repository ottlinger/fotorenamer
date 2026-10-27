#!/bin/bash
set -e
echo "Generating mvn site ..."
./mvnw clean site
echo "DONE - ready to commit and push"
cp -rf target/site/* docs
git add -f docs
git commit -a -m "Update site via script"
git push
