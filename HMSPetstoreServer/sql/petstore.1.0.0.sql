-- create user
CREATE USER 'pet'@'%' IDENTIFIED BY '@Pet2020$';
-- create database
CREATE DATABASE petstore;
-- add permission
GRANT SELECT,INSERT,UPDATE,DELETE ON petstore.* TO 'pet'@'%';

-- create table
DROP TABLE IF EXISTS `t_pet`;
CREATE TABLE `t_pet` (
  `id` bigint(20) NOT NULL COMMENT 'pet id',
  `name` varchar(64) NOT NULL COMMENT 'pet name',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `t_pet`(`id`, `name`) VALUES (1, 'Persian cat ');
INSERT INTO `t_pet`(`id`, `name`) VALUES (2, 'Golden hair');
