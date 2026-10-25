-- PostgreSQL initialization script

-- Create additional databases
CREATE DATABASE sonar;
CREATE DATABASE jenkins;

-- Create additional users
CREATE USER sonar WITH PASSWORD 'sonar123';
CREATE USER jenkins WITH PASSWORD 'jenkins123';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE sonar TO sonar;
GRANT ALL PRIVILEGES ON DATABASE jenkins TO jenkins;

-- Create extensions in banking_system
\c banking_system;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
CREATE EXTENSION IF NOT EXISTS "btree_gin";