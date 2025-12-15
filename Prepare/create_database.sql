-- Создание базы данных для проекта Prepare
-- Выполните этот скрипт в PostgreSQL (например, через pgAdmin или psql)

-- Создаем базу данных (если её нет)
CREATE DATABASE prepare_db
    WITH 
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Russian_Russia.1251'
    LC_CTYPE = 'Russian_Russia.1251'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

-- Подключаемся к базе данных (эту команду нужно выполнить отдельно)
-- \c prepare_db

-- Если нужно удалить существующие таблицы перед пересозданием:
-- DROP TABLE IF EXISTS orderservices CASCADE;
-- DROP TABLE IF EXISTS additionalservices CASCADE;
-- DROP TABLE IF EXISTS orders CASCADE;
-- DROP TABLE IF EXISTS prices CASCADE;
-- DROP TABLE IF EXISTS rooms CASCADE;
-- DROP TABLE IF EXISTS clients CASCADE;

