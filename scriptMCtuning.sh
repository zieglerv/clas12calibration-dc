#!/bin/sh -f

OS=$(uname)

# set up environment
classPath="$PWD/target/*:$PWD/target/classes/*"

# run 
java -Xmx1536m -Xms1024m -cp $classPath org.clas.detector.clas12calibration.dc.mctuning.viewer.Driver
