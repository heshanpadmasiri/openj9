@echo off

rem
rem Copyright IBM Corp. and others 2001
rem
rem This program and the accompanying materials are made available under
rem the terms of the Eclipse Public License 2.0 which accompanies this
rem distribution and is available at https://www.eclipse.org/legal/epl-2.0/
rem or the Apache License, Version 2.0 which accompanies this distribution and
rem is available at https://www.apache.org/licenses/LICENSE-2.0.
rem
rem This Source Code may also be made available under the following
rem Secondary Licenses when the conditions for such availability set
rem forth in the Eclipse Public License, v. 2.0 are satisfied: GNU
rem General Public License, version 2 with the GNU Classpath
rem Exception [1] and GNU General Public License, version 2 with the
rem OpenJDK Assembly Exception [2].
rem
rem [1] https://www.gnu.org/software/classpath/license.html
rem [2] https://openjdk.org/legal/assembly-exception.html
rem
rem SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0-only WITH Classpath-exception-2.0 OR GPL-2.0-only WITH OpenJDK-assembly-exception-1.0
rem

set CLASSPATH=.
set CLASSPATH=%CLASSPATH%;C:\j9\201\lib\jclMax\classes.zip
set CLASSPATH=%CLASSPATH%;C:\j9\201\lib\jclMax\locale.zip
set CLASSPATH=%CLASSPATH%;C:\j9\201\lib\charconv.zip
set CLASSPATH=%CLASSPATH%;ZipTester.jar
set CLASSPATH="%CLASSPATH%"
set JRE_BIN=c:\j9\201\bin
set ZIP_FILE=ZipTester.jar

JarTester2 %JRE_BIN% %CLASSPATH% %ZIP_FILE%
@echo on
