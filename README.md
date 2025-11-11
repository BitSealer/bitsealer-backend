
## 🚀 Cómo ejecutar este proyecto

### 🧱 1️⃣ Requisitos previos

- [Docker Desktop](https://www.docker.com/products/docker-desktop) (Windows/Mac)  


### 📥 2️⃣ Clonar el repositorio

Abre una terminal y ejecuta:

```bash
git clone https://github.com/BitSealer/bitsealer-backend
cd bitsealer
```

### ⚙️ 3️⃣ Crear el archivo `.env`

Copia el ejemplo incluido:

```bash
cp .env.example .env
```

### 🧩 4️⃣ Construir las imágenes Docker

```bash
docker compose build --no-cache
docker compose up -d
```

### Otros comandos

Verificar estado:
```bash
docker compose ps
```

Ver logs de aplicacion:
```bash
docker compose logs -f app
```


