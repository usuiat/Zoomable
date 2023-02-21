#!/bin/bash

DOCS_ROOT=docs

[ -d $DOCS_ROOT ] && rm -r $DOCS_ROOT
mkdir $DOCS_ROOT
./gradlew dokkaHtml
