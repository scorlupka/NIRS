-- Исправление checksum в Liquibase после изменения миграций
-- Выполните этот скрипт в базе данных prepare_db через pgAdmin или psql

-- ВАРИАНТ 1 (РЕКОМЕНДУЕТСЯ): Если база данных пустая или можно пересоздать таблицы
-- Удаляем все записи из таблицы databasechangelog и пересоздаем таблицы
TRUNCATE TABLE databasechangelog CASCADE;

-- Затем удаляем все таблицы (если они уже созданы):
DROP TABLE IF EXISTS orderservices CASCADE;
DROP TABLE IF EXISTS additionalservices CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS prices CASCADE;
DROP TABLE IF EXISTS rooms CASCADE;
DROP TABLE IF EXISTS clients CASCADE;

-- После этого перезапустите приложение - миграции выполнятся заново с новыми checksum


-- ВАРИАНТ 2: Обновляем checksum для конкретных changesets (если нужно сохранить данные)
-- Раскомментируйте следующие строки, если используете этот вариант:

-- UPDATE databasechangelog 
-- SET md5sum = '8:2f93a82af0734619115e04d5b9fec07c'
-- WHERE id = '3' AND author = 'student' AND filename = 'db/changelog/changes/01-create-users-table.yaml';

-- UPDATE databasechangelog 
-- SET md5sum = '8:9b1ef1a3d938afea4b8e1b565a40ed9f'
-- WHERE id = '4' AND author = 'student' AND filename = 'db/changelog/changes/01-create-users-table.yaml';

-- UPDATE databasechangelog 
-- SET md5sum = '8:6d826f8cdb635463b9a994df3f4fc101'
-- WHERE id = '6' AND author = 'student' AND filename = 'db/changelog/changes/01-create-users-table.yaml';

