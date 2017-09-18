set OUT=%1
set PKG=%2
set SCHEMA=%3
set PKGPATH=%OUT%\%PKG:.=\%

REM clean output directory
rmdir /S /Q %PKGPATH%

REM generate JAXB classes
"%JAVA_HOME%"\bin\xjc -d %OUT% -p %PKG% %SCHEMA%
