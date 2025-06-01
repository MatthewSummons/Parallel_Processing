#!/bin/bash

# Set path to your GlassFish installation
GLASSFISH_HOME="$HOME/Desktop/HKU Courses/2024-25/Y4S2/COMP3358/Assignments/A3/glassfish5"

# Set path to your specific Java 8 installation
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home/"

# Compile the files first using specific Java 8 compiler
"$JAVA_HOME/bin/javac" -source 1.8 -target 1.8 -cp "$GLASSFISH_HOME/glassfish/lib/gf-client.jar" QueueSenderExample.java QueueReceiverExample.java

# Check if sender or receiver is specified
if [ "$1" == "sender" ]; then
    echo "Running Queue Sender..."
    # Use the appclient with specified Java version
    JAVA_HOME="$JAVA_HOME" "$GLASSFISH_HOME/glassfish/bin/appclient" QueueSenderExample
elif [ "$1" == "receiver" ]; then
    echo "Running Queue Receiver..."
    # Use the appclient with specified Java version
    JAVA_HOME="$JAVA_HOME" "$GLASSFISH_HOME/glassfish/bin/appclient" QueueReceiverExample
else
    echo "Usage: ./run.sh [sender|receiver]"
    exit 1
fi