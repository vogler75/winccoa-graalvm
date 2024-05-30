#!/bin/bash

# Define the directory path
directoryPath="libs"

# Get all file names in the directory with their full paths
filePaths=$(find "$directoryPath" -type f -print)

# Join all file paths into a single string, separated by semicolons
filePathsString=$(echo $filePaths | tr ' ' ';')

# Print the resulting string
echo $filePathsString

