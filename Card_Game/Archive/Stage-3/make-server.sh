#!/bin/bash

# Define the source directory
SRC="TwentyFourGame/Server"

### Export MySQL Plugin for JDBC & GlassFish Installation
export CLASSPATH="$CLASSPATH:$(dirname "$0")/mysql-connector-j-9.3.0/mysql-connector-j-9.3.0.jar:$(dirname "$0")/glassfish5/glassfish/lib/gf-client.jar"

# Compile the Java files
javac "${SRC}/AuthenticationManager.java"

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    # Run the launch the server; Don't forget to launch rmiregistry
    java -Djava.security.policy=./TwentyFourGame/security.policy TwentyFourGame.Server.AuthenticationManager
else
    echo "Compilation failed."
fi