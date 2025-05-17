#!/bin/bash

# Define the source directory
SRC="TwentyFourGame/Client"

# Array of Java source files
files=(
    "${SRC}/AppPanel.java"
    "${SRC}/LoginManager.java"
    "${SRC}/Notification.java"
    "${SRC}/Main.java"
    "${SRC}/RegistrationManager.java"
)

# Set path to your specific Java 8 installation
JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home/"

# Compile the Java files
"$JAVA_HOME/bin/javac" -source 1.8 -target 1.8 "${files[@]}"

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    # Run the main class
    "$JAVA_HOME/bin/java" "${SRC}.Main"  $1
else
    echo "Compilation failed."
fi