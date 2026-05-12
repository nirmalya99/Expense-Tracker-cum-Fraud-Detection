# Phase 5 Deployment

## Local Docker run

1. Create your `.env` file:
   ```bash
   cp .env.example .env
   ```

2. Build and start everything from the project root:
   ```bash
   docker compose up -d --build
   ```

3. Open:
   - Frontend: `http://localhost`
   - Backend: `http://localhost:8083`

## AWS EC2

Use an Ubuntu 22.04 or 24.04 EC2 instance, install Docker Engine and the Compose plugin, then run the same `docker compose up -d --build` command on the server.

Security group inbound rules:
- `22` from your IP only
- `80` from the internet
- do **not** expose `8083` publicly unless you really want extra trouble

## Notes

- Frontend traffic goes through nginx and proxies `/api` to the backend container.
- Backend database credentials and JWT secret come from environment variables.
- The MySQL data is stored in a Docker volume named `mysql_data`.
- Because your backend is now flattened, the backend Docker build context is `./backend`, not `./backend/backend`.
