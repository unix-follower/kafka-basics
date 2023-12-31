#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
   CREATE ROLE $MESSENGER_SVC_USER WITH LOGIN NOCREATEROLE NOCREATEDB NOREPLICATION NOSUPERUSER PASSWORD '$MESSENGER_SVC_PASSWORD';

   CREATE DATABASE messenger OWNER $MESSENGER_SVC_USER;
EOSQL
