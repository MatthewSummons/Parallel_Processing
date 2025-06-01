#!/bin/bash

# Define the source directory
SRC="TwentyFourGame/Client"

# Path to GlassFish JMS client JAR
GFJAR="./glassfish5/glassfish/lib/gf-client.jar"

# Compile all Java files in the client directory, including dependencies
javac -cp ".:$GFJAR" ${SRC}/*.java

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    # Run the main class with the correct classpath
    java -cp ".:$GFJAR" ${SRC}.Main $1
else
    echo "Compilation failed."
fi