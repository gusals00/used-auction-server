#!/bin/bash
PROJECT_NAME="used-auction"
JAR_NAME=$(ls /home/ubuntu/$PROJECT_NAME/build/libs/ | grep '.jar' | tail -n 1)
JAR_PATH="/home/ubuntu/used-auction/build/libs/$JAR_NAME"
PROJECT_NAME_PATH=/home/ubuntu/$PROJECT_NAME/
DEPLOY_LOG_PATH="/home/ubuntu/$PROJECT_NAME/deploy.log"
DEPLOY_ERR_LOG_PATH="/home/ubuntu/$PROJECT_NAME/deploy_err.log"
APP_LOG_PATH="/home/ubuntu/$PROJECT_NAME/application.log"


echo "===== 배포 시작 : $(date +%c) =====" >> $DEPLOY_LOG_PATH

echo "JAR_PATH : $JAR_PATH" >> $DEPLOY_LOG_PATH
echo "JAR_NAME : $JAR_NAME" >> $DEPLOY_LOG_PATH
echo "PROJECT_NAME_PATH : $PROJECT_NAME_PATH" >> $DEPLOY_LOG_PATH

echo "> 현재 동작중인 pid 존재 여부 확인" >> $DEPLOY_LOG_PATH
CURRENT_PID=$(pgrep -f $JAR_NAME)

if [ -z $CURRENT_PID ]
then
  echo "> 기존 포트에 실행중인 프로세스 존재 X" >> $DEPLOY_LOG_PATH
else
  echo "> 기존 포트에 실행중인 프로세스 존재 O" >> $DEPLOY_LOG_PATH
  echo "> 기존 포트에 실행중인 프로세스 kill" >> $DEPLOY_LOG_PATH
  echo "> kill -9 $CURRENT_PID" >> $DEPLOY_LOG_PATH
  kill -9 $CURRENT_PID
fi

echo "===== JAR 파일 실행 =====" >> $DEPLOY_LOG_PATH
nohup java -jar $JAR_PATH > $APP_LOG_PATH 2> $DEPLOY_ERR_LOG_PATH &
echo -e "\n\n" >> $DEPLOY_LOG_PATH

echo "===== 배포 끝 : $(date +%c) =====" >> $DEPLOY_LOG_PATH