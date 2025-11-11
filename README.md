## Quickstart (Docker)

Requisitos: Docker Desktop (con Compose). En WSL2, habilitar “WSL integration”.

```bash
git clone https://github.com/tuusuario/bitsealer.git
cd bitsealer

# 1) Crear el archivo de entorno a partir del ejemplo:
cp .env.example .env
# editar .env y poner:
# - JWT_SECRET con 32+ caracteres
# - (opcional) cambiar DB_USER/DB_PASSWORD si quieres

# 2) Arrancar
docker compose up --build -d

# 3) Ver logs de la app
docker compose logs -f app

# API en http://localhost:8080
