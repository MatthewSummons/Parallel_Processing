#!/bin/bash

# Define the source directory
SRC="TwentyFourGame/Server"

### Export MySQL Plugin for JDBC & GlassFish Installation
export CLASSPATH="$CLASSPATH:$(dirname "$0")/mysql-connector-j-9.3.0/mysql-connector-j-9.3.0.jar:$(dirname "$0")/glassfish5/glassfish/lib/gf-client.jar"

# Set path to your specific Java 8 installation
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home/"

# Compile the Java files
"$JAVA_HOME/bin/javac" -source 1.8 -target 1.8 "${SRC}/AuthenticationManager.java"

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    # Run the launch the server; Don't forget to launch rmiregistry
    "$JAVA_HOME/bin/java" -Djava.security.policy=./TwentyFourGame/security.policy TwentyFourGame.Server.AuthenticationManager
else
    echo "Compilation failed."
fi
