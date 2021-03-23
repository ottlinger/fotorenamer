#!/bin/bash
echo "Generating mvn site ..."
mvn clean site:site 
echo "DONE - ready to commit and push"
cp -rf target/site/* docs
git add -f docs
git commit -a -m "Update site via script"
git push
