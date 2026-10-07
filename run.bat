@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ==============================
echo   Idle RPG - запуск...
echo ==============================
echo.

where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ОШИБКА] Java не найдена!
    echo Установите JDK: https://adoptium.net/
    echo.
    pause
    exit /b 1
)

if not exist out mkdir out
if not exist lib mkdir lib

echo Компиляция...
javac -encoding UTF-8 -d out -cp "lib\jansi-2.4.1.jar" src/game/*.java src/game/prologue/*.java src/player/*.java src/party/*.java src/enemy/*.java src/item/*.java src/inventory/*.java src/combat/*.java src/progression/*.java src/progression/skill/*.java src/save/*.java src/util/*.java src/town/*.java src/forge/*.java src/quest/*.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ОШИБКА] Ошибка компиляции!
    echo.
    pause
    exit /b 1
)

REM Библиотека Jansi — цвета в обычном cmd Windows
if not exist lib\jansi-2.4.1.jar (
    echo Загрузка Jansi для цветов в консоли...
    powershell -NoProfile -Command "try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/fusesource/jansi/jansi/2.4.1/jansi-2.4.1.jar' -OutFile 'lib\jansi-2.4.1.jar' -UseBasicParsing } catch { Write-Host 'Не удалось скачать Jansi' }"
    echo.
)

set "CP=out"
if exist lib\jansi-2.4.1.jar (
    set "CP=out;lib\jansi-2.4.1.jar"
    echo Цвета: Jansi ^(обычный cmd^)
) else (
    echo Цвета: без Jansi — чистый текст в cmd
    echo Скачайте jansi в lib\ или запустите run-wt.bat в Windows Terminal
)
echo.

echo Запуск игры...
echo.
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp "%CP%" game.Main

echo.
echo Игра завершена.
pause
