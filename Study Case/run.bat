@echo off
title Kopma Mart POS - Kasir Cepat Minimarket Kampus
echo ====================================================
echo   KOPMA MART POS - KOPERASI MAHASISWA KAMPUS
echo   Menjalankan Aplikasi Kasir Java Swing...
echo ====================================================
echo.

javac -encoding UTF-8 *.java
if %errorlevel% neq 0 (
    echo [ERROR] Kompilasi Java gagal! Pastikan JDK terpasang.
    pause
    exit /b %errorlevel%
)

start javaw -cp . KopmaMartApp
echo Aplikasi berhasil dijalankan di background!
exit
