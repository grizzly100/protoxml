REM Example JAXB document generation batch file
SET PROJECT=C:\Dev\Projects\IDEA\protoxml

SET OUT=%PROJECT%\src\test\java
SET XSD=%PROJECT%\src\test\resources\schema

REM call xjcwrapper.bat %OUT% testdomain.employee %XSD%\generated\employee.xsd
REM call xjcwrapper.bat %OUT% testdomain.zoo %XSD%\generated\zoo.xsd
REM call xjcwrapwbind.bat %OUT% testdomain.company %XSD%\company.bindings.xml %XSD%\company.xsd
REM call xjcwrapper.bat %OUT% testdomain.music %XSD%\music.xsd
call xjcwrapwbind.bat %OUT% testdomain.music %XSD%\music.bindings.xml %XSD%\music.xsd



