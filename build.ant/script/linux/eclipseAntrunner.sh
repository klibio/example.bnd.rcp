#!/bin/bash

echo \###########################################################################
echo \# shell script for launching an eclipse antrunner
echo \###########################################################################

pwd=$(pwd)
teamScmFetchDestination=${pwd}/../../..
timestamp=$(date +"%Y%m%d-%H%M%S")

echo \###########################################################################
echo \# parse property file into shell env variables
echo \###########################################################################
rm -rf /tmp/eclAntrunner >> /dev/null 2>&1

mkdir /tmp/eclAntrunner

source $pwd/../../config/localBuild_fw.properties_override

cat $pwd/../../config/localBuild_fw.properties_override | grep -v -e "^\s*#" | cut -d'=' -f1 > /tmp/eclAntrunner/keys.txt

cp -v $pwd/../../config/localBuild_fw.properties /tmp/eclAntrunner/localBuild_fw.properties

while read key; do
sed -i -e "/${key}=/d" /tmp/eclAntrunner/localBuild_fw.properties
done < /tmp/eclAntrunner/keys.txt

source /tmp/eclAntrunner/localBuild_fw.properties

#echo \### DEBUG - BEGIN ###
#set -o posix ; set | grep BUILD
#set -o posix ; set | grep JAVA
#set -o posix ; set | grep ENGINE
#echo \### DEBUG - END ###
#exit

echo \###########################################################################
echo \# script execution
echo \###########################################################################
SH_LOGFILE=${BUILD_WORK_DIR}/${timestamp}_sh.log
ECL_LOGFILE=${BUILD_WORK_DIR}/${timestamp}_eclipse.log
mkdir -p ${BUILD_WORK_DIR}

echo $SH_LOGFILE
echo $ECL_LOGFILE
echo 
echo ${JAVA8_DIR}/bin/java \\
echo -Xms40m -Xmx1024m \\
echo -jar ${ECLIPSE_BIN_DIR}/plugins/org.eclipse.equinox.launcher_${ECLIPSE_LAUNCHER_VERSION}.jar \\
echo -application org.eclipse.ant.core.antRunner \\
echo -configuration ${BUILD_WORK_DIR}/.ecl/cfg \\
echo -data          ${BUILD_WORK_DIR}/.ecl/data \\
echo -buildfile ${teamScmFetchDestination}/cec.build/STABLE/$2 \\
echo -Dbuilder=${teamScmFetchDestination}/cec.build/STABLE \\
echo -DENGINE_LIB_DIR=${ENGINE_LIB_DIR} \\
echo -DbuildLocal.properties=$1

${JAVA8_DIR}/bin/java \
-Xms40m -Xmx1024m \
-jar ${ECLIPSE_BIN_DIR}/plugins/org.eclipse.equinox.launcher_${ECLIPSE_LAUNCHER_VERSION}.jar \
-application org.eclipse.ant.core.antRunner \
-configuration ${BUILD_WORK_DIR}/.ecl/cfg \
-data          ${BUILD_WORK_DIR}/.ecl/data \
-buildfile ${teamScmFetchDestination}/cec.build/STABLE/$2 \
-Dbuilder=${teamScmFetchDestination}/cec.build/STABLE \
-DENGINE_LIB_DIR=${ENGINE_LIB_DIR} \
-DbuildLocal.properties=$1 > >(tee $ECL_LOGFILE) 2>&1

echo \###########################################################################
echo \# output ECL_LOGFILE
echo \###########################################################################
cat $ECL_LOGFILE

