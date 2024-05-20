# Define the directory path
$directoryPath = "C:\Workspace\winccoa-java\build\output\libs"

# Get all file names in the directory with their full paths
$filePaths = Get-ChildItem -Path $directoryPath | Select-Object -ExpandProperty FullName

# Join all file paths into a single string, separated by semicolons
$filePathsString = $filePaths -join ';'

# Print the resulting string
Write-Output $filePathsString
