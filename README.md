# BT Revizer Backend

Spring Boot REST API for BT Revizer, a quiz and active-recall application. The backend provides authentication, quiz and category management, quiz attempts and grading, progress statistics, and optional automatic grading of text answers through Ollama.

The frontend is maintained in a separate repository: [bt-revizer-frontend](https://github.com/jakubp03/bt-revizer-frontend).

## Technology

- Java 21
- Spring Boot 3.5.8 and Maven (Maven Wrapper included)
- MySQL 8, Spring Data JPA, and Flyway database migrations
- Spring Security with JWT authentication
- Ollama with `llama3.1:8b` by default for automatic text-answer grading

## Prerequisites

For local backend development, install:

- JDK 21
- MySQL 8
- Ollama, if you want automatic grading of text answers

For the containerized setup, install Docker Engine and the Docker Compose plugin. The frontend must also be cloned separately as described in [Docker Compose](#docker-compose).

## Local Development

### 1. Configure MySQL

Create a database named `bt_revizer`. For example, connect to MySQL as an administrator and run:

```sql
CREATE DATABASE bt_revizer CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The default development JDBC URL is `jdbc:mysql://localhost:3306/bt_revizer`. Update it in `src/main/resources/application.properties` if your local MySQL host, port, or database name differs. The development profile also needs a MySQL username and password.

### 2. Configure the development profile

Copy [`src/main/resources/application-dev.properties.example`](src/main/resources/application-dev.properties.example) to `src/main/resources/application-dev.properties`, then set the database username and password and replace the JWT secret with a private Base64-encoded key. The example file is ignored by Git after copying so local credentials are not committed.

The JWT secret must decode to at least 32 bytes for HS256. Generate one with:

```sh
openssl rand -base64 32
```

The example profile's CORS origins allow the frontend's default Vite development origin, `http://localhost:5173`. Adjust `application.cors.allowed-origins` in your local properties if your frontend runs on another origin.

### 3. Install and start Ollama (optional)

Ollama is needed for automatic review of `TEXT_INPUT` questions configured for automatic review. Install Ollama using the instructions for your operating system at [ollama.com/download](https://ollama.com/download), then start its local service. Pull the default model:

```sh
ollama pull llama3.1:8b
```

The backend defaults to `http://localhost:11434` and model `llama3.1:8b`. To use a different model or Ollama host, set `OLLAMA_MODEL` or `OLLAMA_BASE_URL` in the backend process environment. Ollama is optional for other application features, but automatic LLM grading will not work without it.

### 4. Run the backend

From the backend repository root:

```sh
./mvnw spring-boot:run
```

The default active profile is `dev`; the API listens on port `8080`. Flyway applies the schema migration at startup. On Windows, use `mvnw.cmd spring-boot:run`.

To run the test suite:

```sh
./mvnw test
```

### 5. Run the frontend

Clone the [frontend repository](https://github.com/jakubp03/bt-revizer-frontend) separately and follow its README for installing dependencies and starting its development server. The backend development profile allows `http://localhost:5173` by default. The helper script `local_dev_util/start_fe.sh` starts the frontend's `npm run dev` command when the frontend has been cloned to the expected sibling directory.

## Docker Compose

Compose is defined in this backend repository, but it builds the frontend from a separate, sibling checkout. The directory layout must be:

```text
parent-directory/
├── bt-revizer-backend/
│   └── compose.yaml
└── bt-revizer-frontend/
	└── Dockerfile
```

From the parent directory, clone the frontend if it is not already present:

```sh
git clone https://github.com/jakubp03/bt-revizer-frontend.git bt-revizer-frontend
```

Then change to `bt-revizer-backend` for the remaining commands.

### 1. Configure the environment

Copy [`.env.example`](.env.example) to `.env` in the backend repository root. Set at least:

| Variable | Purpose |
| --- | --- |
| `DB_USERNAME` | Database username. The Compose MySQL service currently creates the root account only, so use `root`. |
| `DB_PASSWORD` | MySQL root password; also used as `MYSQL_ROOT_PASSWORD`. Choose a strong value. |
| `JWT_SECRET_KEY` | Private Base64-encoded JWT signing key. Generate with `openssl rand -base64 32`. |
| `JWT_EXPIRATION` | Access-token lifetime in milliseconds. |
| `JWT_REFRESH_EXPIRATION` | Refresh-token lifetime in milliseconds. |
| `DOMAIN_NAME` | Public DNS name used by the frontend and Let's Encrypt. |
| `CERTBOT_EMAIL` | Email address for Let's Encrypt certificate registration. |

The example values for token lifetimes are intentionally blank; set both to positive durations in milliseconds. For example, `600000` is 10 minutes and `604800000` is 7 days. `DB_URL` may be left blank to use the Compose default, `jdbc:mysql://db:3306/bt_revizer`. Set the port in this JDBC URL if you change the MySQL port; `DB_PORT` is present in the example template but is not currently used by `compose.yaml`. `MYSQL_DATABASE` defaults to `bt_revizer`.

`FRONTEND_PORT` and `FRONTEND_HTTPS_PORT` publish the frontend's HTTP and HTTPS ports on the host (defaults `80` and `443`). `CORS_ALLOWED_ORIGINS` should contain the frontend's public origin(s), comma-separated. `VITE_API_BASE_URL` defaults to `/api/v1`. `OLLAMA_MODEL` defaults to `llama3.1:8b`.

For Let's Encrypt, point `DOMAIN_NAME` at the host and allow inbound ports 80 and 443. The default domain `localhost` and an empty email are not suitable for issuing a public certificate. You can set `CERTBOT_STAGING=1` while testing certificate issuance; staging certificates are not browser-trusted. Set it to `0` for a production certificate. `CERT_WATCH_INTERVAL_SECONDS` controls how often the frontend checks for updated certificates.

`APP_UPLOAD_DIR` defaults to `/uploads`, which is bind-mounted to the backend repository's `uploads/` directory. `UID` and `GID` control the backend container's host-user mapping for bind-mounted `logs/` and `uploads/` directories. On Linux, set them to the output of `id -u` and `id -g` if needed to avoid file ownership issues.

### 2. Start the stack

From the backend repository root, with the frontend checkout beside it and `.env` configured:

```sh
docker compose up --build -d
```

Compose starts MySQL, Ollama, a one-shot Ollama model-pull service, the backend, the frontend, and Certbot. The backend waits for MySQL to become healthy and for the model pull to complete. The first model download can take time and requires sufficient disk space. The model is kept in the `ollama-data` volume for later starts.

The frontend is available on the configured host ports, normally `http://<your-domain>` and `https://<your-domain>`. The backend is exposed to the frontend on the Compose network at port `8080`; it is not directly published to the host by the supplied Compose file.

Useful commands, run from the backend directory:

```sh
docker compose logs -f backend
docker compose logs -f ollama-init
docker compose ps
docker compose down
```

`docker compose down` stops and removes the containers but preserves named data volumes. Avoid `docker compose down -v` unless you intend to delete the MySQL database, Ollama model cache, and certificate data.

## API

The API base path is `/api/v1`. Authentication and application endpoints are implemented under `/auth`, `/quiz`, `/category`, and `/attempt`. Most application endpoints require a valid JWT access token in the `Authorization: Bearer <token>` header.

## Configuration Reference

- [`.env.example`](.env.example): environment template for Docker Compose.
- [`src/main/resources/application-dev.properties.example`](src/main/resources/application-dev.properties.example): local Spring `dev` profile template. Copy it to `application-dev.properties` and fill in local values.
- [`compose.yaml`](compose.yaml): multi-container deployment definition.
- [`Dockerfile`](Dockerfile): backend container build.
