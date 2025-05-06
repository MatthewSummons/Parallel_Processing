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
    # TODO: Modify to Just Java (Mention in report)
    # Run the main class
    /Library/Java/JavaVirtualMachines/jdk-1.8.jdk/Contents/Home/bin/java "${SRC}.Main"  $1
else
    echo "Compilation failed."
fi