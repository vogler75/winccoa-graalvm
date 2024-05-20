# Define the directory path
$directoryPath = "libs"

# Get all file names in the directory with their full paths
$filePaths = Get-ChildItem -Path $directoryPath | Resolve-Path -Relative

# Join all file paths into a single string, separated by semicolons
$filePathsString = $filePaths -join ';'

# Print the resulting string
Write-Output $filePathsString
