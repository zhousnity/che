#!/bin/bash

function UpdateWorkspaceState() 
{
    currentState=$(curl -Ss  "http://localhost:8080/api/workspace/$1" | jq -r .status)
}

function StartWorkspace() 
{
    curl -Ss -X POST --output /dev/null  "http://localhost:8080/api/workspace/$1/runtime"
}

function StopWorkspace() 
{
    curl -Ss -X DELETE --output /dev/null "http://localhost:8080/api/workspace/$1/runtime"
}


currentState="UNKNOWN"
workspaceId=$1

while true
do
UpdateWorkspaceState $workspaceId
echo Current state of workspace $workspaceId is $currentState 
 case $currentState in
	"STOPPED")
    	echo starting worksapce $workspaceId
    	StartWorkspace $workspaceId
    ;;
    "RUNNING")
    	echo stopping worksapce $workspaceId
    	StopWorkspace $workspaceId
    ;;
	"2" | "3")
    	echo UNKNOWN
    ;;
	*)
    	echo $currentState
    ;;
esac
sleep 1

done
