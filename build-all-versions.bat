@echo off
setlocal
powershell -ExecutionPolicy Bypass -File "%~dp0scripts\build-all-versions.ps1" %*
exit /b %ERRORLEVEL%
