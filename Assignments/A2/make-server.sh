#!/bin/bash

# Define the source directory
SRC="TwentyFourGame/Server"

# Array of Java source files
files=(
    "${SRC}/Authenticate.java"
    "${SRC}/AuthenticationManager.java"
)

### Export MySQL Plugin for JDBC
# Relative path to JDBC Driver, comment this and uncomment line below if there are issues
export CLASSPATH=$CLASSPATH:"$(dirname "$0")/mysql-connector-j-9.3.0/mysql-connector-j-9.3.0.jar"
# Uncomment and add path manually here
# export CLASSPATH=$CLASSPATH:"/path/to/mysql-connector-j-9.3.0.jar"

# Compile the Java files
javac -source 1.8 -target 1.8 "${files[@]}"

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    # Run the launch the server; Don't forget to launch rmiregistry
    java -Djava.security.policy=./TwentyFourGame/security.policy TwentyFourGame.Server.AuthenticationManager
else
    echo "Compilation failed."
fi
