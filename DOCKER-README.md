# 🐳 Guía de Uso de Docker - Inventario CourierSync

## 📋 Requisitos Previos

- Docker Desktop instalado y en ejecución
- Java 21 instalado (para compilación local)
- Maven 3.9+ (incluido en la imagen Docker)

---

## 🚀 Construcción de la Imagen Docker

### Opción 1: Build manual

```bash
# Desde la raíz del proyecto
docker build -t fabricaescuela-inventario:latest .
```

### Opción 2: Usando Docker Compose

```bash
docker-compose build
```

---

## ▶️ Ejecutar la Aplicación

### Opción 1: Docker Run con valores por defecto

```bash
docker run -d \
  -p 8080:8080 \
  --name inventario-app \
  fabricaescuela-inventario:latest
```

### Opción 2: Docker Run con variables de entorno personalizadas

```bash
docker run -d \
  -p 8080:8080 \
  --name inventario-app \
  -e SERVER_PORT=8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mi_db \
  -e SPRING_DATASOURCE_USERNAME=mi_usuario \
  -e SPRING_DATASOURCE_PASSWORD=mi_password \
  -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \
  fabricaescuela-inventario:latest
```

### Opción 3: Docker Compose (Recomendado)

```bash
# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

---

## 🔍 Comandos Útiles

### Ver logs del contenedor

```bash
docker logs inventario-app
docker logs -f inventario-app  # Seguir logs en tiempo real
```

### Ver contenedores en ejecución

```bash
docker ps
```

### Detener el contenedor

```bash
docker stop inventario-app
```

### Eliminar el contenedor

```bash
docker rm inventario-app
```

### Entrar al contenedor (debugging)

```bash
docker exec -it inventario-app sh
```

### Ver imágenes Docker

```bash
docker images
```

### Eliminar imagen

```bash
docker rmi fabricaescuela-inventario:latest
```

---

## 🌐 Acceder a la Aplicación

Una vez el contenedor esté corriendo:

- **API Base URL**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

### Endpoints de ejemplo:

```bash
# Estados
curl http://localhost:8080/api/estados

# Paquetes
curl http://localhost:8080/api/paquetes

# Estado por ID (con links HATEOAS)
curl http://localhost:8080/api/estados/1
```

---

## ⚙️ Configuración con Variables de Entorno

### Variables disponibles:

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `SERVER_PORT` | Puerto del servidor | `8080` |
| `JWT_SECRET` | Secret para firmar tokens JWT | (ver .env.example) |
| `JWT_EXPIRATION` | Tiempo de expiración JWT (ms) | `86400000` (24h) |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos CORS | `*` |
| `SPRING_DATASOURCE_URL` | URL de PostgreSQL | (ver application.properties) |
| `SPRING_DATASOURCE_USERNAME` | Usuario DB | `neondb_owner` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña DB | (ver application.properties) |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia DDL Hibernate | `update` |
| `HIKARI_MAX_POOL_SIZE` | Tamaño máximo pool | `10` |

### Usar archivo .env con Docker Compose:

1. Copia `.env.example` a `.env`
2. Edita los valores en `.env`
3. Ejecuta: `docker-compose up -d`

---

## 🐛 Troubleshooting

### El contenedor no inicia

```bash
# Ver logs detallados
docker logs inventario-app

# Verificar que el puerto no esté en uso
netstat -an | findstr :8080  # Windows
lsof -i :8080                # Linux/Mac
```

### Error de conexión a base de datos

- Verifica que la URL de la base de datos sea accesible desde el contenedor
- Si usas `localhost`, cambia a la IP de tu máquina o usa Docker networks
- Verifica credenciales y permisos de la base de datos

### Imagen muy grande

La imagen usa `eclipse-temurin:21-jre-alpine` que es muy liviana (~200MB).
Para reducir más:
- Usa multi-stage build (ya implementado)
- Limpia cache de Maven en el Dockerfile

---

## 📊 Optimizaciones Implementadas

✅ Multi-stage build (BUILD + RUNTIME separados)  
✅ Imagen base ligera Alpine Linux  
✅ Solo JRE en runtime (no JDK completo)  
✅ Usuario no-root para seguridad  
✅ Variables de entorno configurables  
✅ Cache de dependencias Maven optimizado  
✅ .dockerignore para contexto limpio  

---

## 📝 Notas de Seguridad

- **Nunca** subas el archivo `.env` a Git
- Cambia `JWT_SECRET` en producción
- No uses `CORS_ALLOWED_ORIGINS=*` en producción
- Usa secrets de Docker/Kubernetes para credenciales sensibles
- El contenedor corre con usuario no-root (`spring:spring`)

---

## 🔗 Enlaces Útiles

- [Documentación oficial de Docker](https://docs.docker.com/)
- [Spring Boot con Docker](https://spring.io/guides/gs/spring-boot-docker/)
- [Best practices Dockerfile](https://docs.docker.com/develop/dev-best-practices/)
