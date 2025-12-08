-- Skrypt inicjalizujący bazy danych dla architektury mikroserwisowej
-- Tworzy dwie bazy: auth_db (dla auth-service) i mafia (dla game-service)

-- Tworzenie bazy danych dla Auth Service
CREATE DATABASE auth_db;

-- Tworzenie bazy danych dla Game Service
CREATE DATABASE mafia;

-- Uprawnienia dla użytkownika postgres do obu baz
GRANT ALL PRIVILEGES ON DATABASE auth_db TO postgres;

GRANT ALL PRIVILEGES ON DATABASE mafia TO postgres;