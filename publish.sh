#!/bin/bash

MC_SERVER=mc.gking2224.me
FILENAME=mc-mod-ctf-1.0.jar
BUILT_FILE=build/libs/$FILENAME
MSM_SERVER_NAME=CaptureTheFlag1
S3_PATH=s3://gk-minecraft/mods/ctf
S3_REGION=eu-west-2
S3_OPTS="--region $S3_REGION"
MSM_SERVER_MODS_DIR=/opt/msm/servers/$MSM_SERVER_NAME/mods

# CLEAN UP
if [[ -f $BUILT_FILE ]]
then
  echo "--- Remove old file"
  #rm -v $BUILT_FILE
fi

# BUILD
echo "--- Run gradle build"
#./gradlew build

if [[ ! -f $BUILT_FILE ]]
then
  echo "ERROR: No built file"
  exit 1
fi

# DEPLOY LOCALLY
echo "--- Deploying to local users"
for d in `find /Users -maxdepth 1 -type d`
do
  if sudo bash -c "[[ -d $d/Library/Application\ Support/minecraft/mods ]]"
  then
    echo "Deploy to $d"
    sudo cp -v $BUILT_FILE $d/Library/Application\ Support/minecraft/mods/$FILE_NAME
  fi
done

# PUBLISH
echo "--- Copying $BUILT_FILE to $S3_PATH/$FILENAME ($S3_REGION)"
aws s3 cp $BUILT_FILE $S3_PATH/$FILENAME $S3_OPTS

# STOP SERVER
echo "--- Stop server $MSM_SERVER_NAME on $MC_SERVER"
ssh -T -i ~/.awskey minecraft@$MC_SERVER "TERM=xterm msm $MSM_SERVER_NAME stop"

# DEPLOY
echo "--- Deploy to $MSM_SERVER_NAME on $MC_SERVER"
ssh -T -i ~/.awskey minecraft@$MC_SERVER "TERM=xterm; mkdir -vp $MSM_SERVER_MODS_DIR"
ssh -T -i ~/.awskey minecraft@$MC_SERVER "TERM=xterm; rm -v $MSM_SERVER_MODS_DIR/$FILENAME"
echo "--- Copy $S3_PATH/$FILENAME ($S3_REGION) to $MSM_SERVER_MODS_DIR/$FILENAME"
ssh -T -i ~/.awskey minecraft@$MC_SERVER "TERM=xterm; aws s3 cp $S3_PATH/$FILENAME $MSM_SERVER_MODS_DIR/$FILENAME $S3_OPTS"

# START SERVER
echo "--- Start server $MSM_SERVER_NAME on $MC_SERVER"
ssh -T -i ~/.awskey minecraft@$MC_SERVER "TERM=xterm msm $MSM_SERVER_NAME start"
