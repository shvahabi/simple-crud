#!/bin/bash

Timestamp=$(date '+%Y/%m/%d %T.%N %z')

Year=$(echo $Timestamp | cut -c1-4)
Month=$(echo $Timestamp | cut -c6-7)
Day=$(echo $Timestamp | cut -c9-10)
WikiPath=$(pwd)"/assets/wiki"

mkdir -p "$WikiPath/journal/$Year/$Month/$Day"

Name=$(uuidgen -r)

echo $Timestamp >> "$WikiPath/journal/$Year/$Month/$Day/$Name.md"

gapplication launch org.gnome.gedit "$WikiPath/journal/$Year/$Month/$Day/$Name.md"
