@echo off
cd /d "%~dp0"
title Night Dragon - Beat 'Em Up
echo ========================================================
echo           NIGHT DRAGON - BEAT 'EM UP 2D
echo ========================================================
echo.

pushd "%~dp0.."
set "ROOT_DIR=%CD%"
popd

set "JAVA_BIN=%ROOT_DIR%\oracleJdk-27\bin\java.exe"
set "JAVAC_BIN=%ROOT_DIR%\oracleJdk-27\bin\javac.exe"
set "SRC_DIR=%~dp0src"
set "BIN_DIR=%~dp0bin"

if not exist "%BIN_DIR%" mkdir "%BIN_DIR%"

echo [1/2] Compilando arquivos Java...
dir /s /b "%SRC_DIR%\*.java" > "%~dp0sources.txt"
"%JAVAC_BIN%" -encoding UTF-8 -d "%BIN_DIR%" @"%~dp0sources.txt"
set COMPILE_ERR=%ERRORLEVEL%
if exist "%~dp0sources.txt" del "%~dp0sources.txt"

if %COMPILE_ERR% NEQ 0 (
    echo.
    echo [ERRO] Falha na compilacao! Verifique as mensagens acima.
    echo.
    pause
    exit /b %COMPILE_ERR%
)

echo [2/2] Iniciando o jogo...
echo.
"%JAVA_BIN%" -cp "%BIN_DIR%" main.ThanatosBeatEmUp

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [AVISO] O jogo encerrou com codigo: %ERRORLEVEL%
    pause
)
