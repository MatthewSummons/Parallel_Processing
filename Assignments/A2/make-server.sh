#!/bin/bash

# Define the source directory
SRC="TwentyFourGame/Server"

# Array of Java source files
files=(
    "${SRC}/Authenticate.java"
    "${SRC}/AuthenticationManager.java"
)

# Export MySQL Plugin for JDBC
# TODO: Modify and put in report & add Part about running mysql
export CLASSPATH=$CLASSPATH:"/Users/matthewsummons/Desktop/HKU Courses/2024-25/Y4S2/COMP3358/Assignments/A2/mysql-connector-j-9.3.0/mysql-connector-j-9.3.0.jar"

# Compile the Java files
javac -source 1.8 -target 1.8 "${files[@]}"

# Check if the compilation was successful
 if [ $? -eq 0 ]; then
    # TODO: Modify to Just Java (Mention in report)
    # Run the launch the server; Don't forget to launch rmiregistry
    /Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home/bin/java -Djava.security.policy=./TwentyFourGame/security.policy TwentyFourGame.Server.AuthenticationManager
 else
     echo "Compilation failed."
 fi
