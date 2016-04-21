#!/bin/bash
echo "Generating new fotorenamer 'mvn site' ..."
mvn clean site:site
echo "DONE - will commit and push now ..."
git checkout gh-pages && ./copy-site.sh 
