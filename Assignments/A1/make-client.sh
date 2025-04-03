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

# Compile the Java files
javac -source 1.8 -target 1.8 "${files[@]}"

# Check if the compilation was successful
if [ $? -eq 0 ]; then
    # Run the main class
    java "${SRC}.Main"  $1
else
    echo "Compilation failed."
fi
