#!/bin/bash

# First parameters should be the absolute path to the zip file
# Remove any previously created folders by this script.
# Zip file is expanded in it's parent directory.

# this is an absolutely horrible script to unpack crosswalk
# and to remove dependencies that cause all the problems
crosswalk_zip=$1

if [ ! -f "$crosswalk_zip" ]; then
	echo "File not found!";
	exit 1
fi

if [ ${crosswalk_zip: -4} != ".zip" ]; then
        echo "provide crosswalk zip file"
	exit 1
fi

echo "crosswalk zip: $crosswalk_zip"

crosswalk=${crosswalk_zip::-4}
echo "crosswalk path: $crosswalk"

if [ -z "$crosswalk" ]; then
	exit 2
fi

#remove old folder if it exists
if [ -f "$crosswalk" ]; then
	echo "crosswalk folder already exists"
	exit 1
fi

parent=`dirname $crosswalk_zip`

cd $parent

# Unzip crosswalk file
unzip $crosswalk_zip > unzip_dump

cd $crosswalk
echo "pwd: `pwd`"

# Create temp fodler
rm -rf libs/temp
mkdir -p libs/temp 
cd libs/temp

# Extract jar file
jar xf ../xwalk_core_library_java_library_part.jar

# Deletes ./javax and ./com folders
rm -r ./com
rm -r ./javax

# Creates new jar file
jar cf xwalk_core_library_java_library_part.jar *

# copy and replace
cp -Rf xwalk_core_library_java_library_part.jar ../xwalk_core_library_java_library_part.jar

# removes temp folder
cd ..
rm -r temp
