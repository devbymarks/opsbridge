@echo off
setlocal
cd /d "%~dp0"
where docker >nul 2>nul
if errorlevel 1 (
  echo ERRO: Docker nao encontrado. Instale o Docker Desktop e abra-o antes de continuar.
  pause
  exit /b 1
)
docker compose version >nul 2>nul
if errorlevel 1 (
  echo ERRO: Docker Compose v2 nao encontrado. Atualize o Docker Desktop.
  pause
  exit /b 1
)
echo Iniciando testes de build, banco PostgreSQL e aplicacao OpsBridge...
docker compose up --build
if errorlevel 1 (
  echo.
  echo O inicio falhou. Confira a mensagem acima ou execute: docker compose logs app
  pause
  exit /b 1
)
endlocal
