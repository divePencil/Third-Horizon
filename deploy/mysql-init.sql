CREATE DATABASE IF NOT EXISTS freediving
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'freediving'@'localhost' IDENTIFIED BY 'change-me';
GRANT ALL PRIVILEGES ON freediving.* TO 'freediving'@'localhost';
FLUSH PRIVILEGES;
