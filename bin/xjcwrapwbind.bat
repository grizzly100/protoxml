set OUT=%1
set PKG=%2
set BIND=%3
set SCHEMA=%4

set PKGPATH=%OUT%\%PKG:.=\%

REM clean output directory
rmdir /S /Q %PKGPATH%

REM generate JAXB classes
"%JAVA_HOME%"\bin\xjc -d %OUT% -b %BIND% -p %PKG% %SCHEMA%
