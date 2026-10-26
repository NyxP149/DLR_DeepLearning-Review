@echo off
rem Lance DLR : double-clic = demarrer ; "DLR.cmd stop" = tout arreter ; "DLR.cmd status" = etat.
rem Options : -Mode local (site, API et base sur ce PC) ou -Mode hybrid (site Render), -Rebuild, -NoBrowser.
title DLR
cd /d "%~dp0"
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "infrastructure\scripts\dlr.ps1" %*
echo.
pause
