#!/bin/bash
java -Xmx2G -XX:+UseParallelGC -XX:MaxGCPauseMillis=3 -jar LEDCubeManager.jar --showfps
