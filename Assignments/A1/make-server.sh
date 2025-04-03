#!/bin/bash

# Define the source directory
SRC="TwentyFourGame/Server"

# Array of Java source files
files=(
    "${SRC}/Authenticate.java"
    "${SRC}/AuthenticationManager.java"
)

# Compile the Java files
javac -source 1.8 -target 1.8 "${files[@]}"

# Check if the compilation was successful
 if [ $? -eq 0 ]; then
     # Run the launch the server; Don't forget to launch rmiregistry
    java -Djava.security.policy=./TwentyFourGame/security.policy TwentyFourGame.Server.AuthenticationManager
 else
     echo "Compilation failed."
 fi
