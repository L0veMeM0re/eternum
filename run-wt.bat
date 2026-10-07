@echo off
REM Запуск в Windows Terminal — цвета работают без Jansi
where wt >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    wt -d "%~dp0" cmd /k "cd /d %~dp0 && run.bat"
    exit /b 0
)
echo Windows Terminal не установлен.
echo Скачайте: https://aka.ms/terminal
echo.
echo Запуск в обычной консоли...
call "%~dp0run.bat"
