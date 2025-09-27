## Integrantes
Marlon Estuardo Pappa Hernandez 2400156

## Docker Setup:
docker run --name postgres-db -e POSTGRES_PASSWORD=admin123 -e POSTGRES_USER=postgres -e POSTGRES_DB=postgres -p 5433:5432 -d postgres
