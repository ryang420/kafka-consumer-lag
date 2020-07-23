#!/bin/sh

################################################################
# Description: consumer lag startup script
# Author: David Yang
# Date: 2020-07-22
################################################################

# jdk path
JAVAHOME=$JAVA_HOME

RUNNING_USER=$USER

# project path
SCRIPT_DIR=$(cd `dirname $0`; pwd)
BASEPATH=$(dirname $SCRIPT_DIR)

# jar package path
APP_HOME=$BASEPATH/lib/kafka-consumer-lag-1.0-SNAPSHOT-jar-with-dependencies.jar

# main method class
MAIN_CLASS=com.active.prometheus.ConsumerLagExporter


CLASSPATH=$APP_HOME/classes
for i in $APP_HOME/lib/*.jar; do
    CLASSPATH="$CLASSPATH":"$i"
done



psid=0
initPsid(){
    javaps=`$JAVAHOME/bin/jps -l | grep $APP_HOME`

    if [ -n "$javaps" ]; then
        psid=`echo $javaps | awk '{ print $1}'`
    else
        psid=0
    fi
}


start(){
    initPsid
    if [ ! -n "$1" ]; then
        configPath=$BASEPATH/config/conf.properties
    else
        echo "use custom configuration file: $1"
        configPath=$1
    fi

    if [ $psid -ne 0 ]; then
        echo "=================================================="
        echo "|         server has already started          |"
        echo "=================================================="
    else
        echo "starting $MAIN_CLASS ..."
        JAVA_CMD="nohup $JAVAHOME/bin/java -jar $APP_HOME $configPath >/dev/null 2>&1 &"
        eval "$JAVA_CMD"
        initPsid
        if [ $psid -ne 0 ]; then
            echo "start [OK] pid=$psid"
        else
            echo "start [FAILED], $?"
        fi
    fi
}


stop(){
    initPsid

    if [ $psid -ne 0 ]; then
        echo -n "Stopping $MAIN_CLASS pid=$psid ..."
        eval "kill $psid"
        if [ $? -eq 0 ]; then
            echo "Stop [OK]"
        else
            echo "Stop [FAILED]"
        fi
    else
        echo "=================================================="
        echo "|      WARN: $MAIN_CLASS is not running!      |"
        echo "=================================================="
    fi
}


status(){
    initPsid
    if [ $psid -ne 0 ]; then
        echo "Running"
    else
        echo "not Running"
    fi
}


info(){
    echo "********* System Information ***********"
    echo `head -n 1 /etc/issue`
    echo `uname -a`
    echo
    echo "JAVAHOME=$JAVAHOME"
    echo `$JAVAHOME/bin/java -version`
    echo
    echo "USER=$RUNNING_USER"
    echo "BASEPATH=$BASEPATH"
    echo "APP_HOME=$APP_HOME"
    echo "MAIN_CLASS=$MAIN_CLASS"
    echo "*****************************************"
}


case "$1" in
    'start')
        start $2
        ;;
    'stop')
        stop
        ;;
    'restart')
        stop
        start $2
        ;;
    'status')
        status
        ;;
    'info')
        info
        ;;
    *)

    echo "Usage $0 { start | stop | status | restart | info }"
    exit
esac
exit 0