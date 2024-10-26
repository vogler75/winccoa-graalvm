@echo off
setlocal enabledelayedexpansion

set JAR_FILES=
for %%f in (libs\*.jar) do (
    if defined JAR_FILES (
        set JAR_FILES=!JAR_FILES!;.\%%f
    ) else (
        set JAR_FILES=.\%%f
    )
)

echo %JAR_FILES%
