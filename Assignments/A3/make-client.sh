#!/bin/bash

# Define the source directory
SRC="TwentyFourGame/Client"

# Set path to your specific Java 8 installation
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home/"

# Path to GlassFish JMS client JAR
GFJAR="./glassfish5/glassfish/lib/gf-client.jar"

# Compile all Java files in the client directory, including dependencies
"$JAVA_HOME/bin/javac" -source 1.8 -target 1.8 -cp ".:$GFJAR" ${SRC}/*.java # ../Common/*.java

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    # Run the main class with the correct classpath
    "$JAVA_HOME/bin/java" -cp ".:$GFJAR" ${SRC}.Main $1
else
    echo "Compilation failed."
fi