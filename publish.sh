#!/bin/bash

MC_SERVER=mc.gking2224.me
FILENAME=mc-mod-ctf-1.0.jar
MSM_SERVER_NAME=CaptureTheFlag1
S3_PATH=s3://gk-minecraft/mods/ctf
S3_OPTS="--region eu-west-2"
MSM_SERVER_MODS_DIR=/opt/msm/servers/$MSM_SERVER_NAME/mods

# BUILD
./gradlew build

# DEPLOY LOCALLY
cp -v build/libs/$FILENAME /Users/gk/Library/Application\ Support/minecraft/mods/
sudo cp -v build/libs/$FILENAME /Users/edward/Library/Application\ Support/minecraft/mods/
sudo cp -v build/libs/$FILENAME /Users/thomas/Library/Application\ Support/minecraft/mods/

# PUBLISH
aws s3 cp build/libs/$FILENAME $S3_PATH/$FILENAME $S3_OPTS

# STOP SERVER
ssh -i ~/.awskey minecraft@$MC_SERVER msm $MSM_SERVER_NAME stop

# DEPLOY
ssh -i ~/.awskey minecraft@$MC_SERVER "mkdir -p $MSM_SERVER_MODS_DIR"
ssh -i ~/.awskey minecraft@$MC_SERVER rm $MSM_SERVER_MODS_DIR/$FILENAME
ssh -i ~/.awskey minecraft@$MC_SERVER aws s3 cp $S3_PATH/$FILENAME $MSM_SERVER_MODS_DIR/$FILENAME $S3_OPTS

# START SERVER
ssh -i ~/.awskey minecraft@$MC_SERVER msm $MSM_SERVER_NAME start
