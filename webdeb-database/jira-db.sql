-- -----------------------------------------------------------------------------------------------
-- Copyright 2015 PReCISE - Girsef - CENTAL. This is part of WebDeb-web and is free
-- software: you can redistribute it and/or modify it under the terms of the GNU Lesser General
-- Public License version 3 as published by the Free Software Foundation. It is distributed in the
-- hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
-- 
-- See the GNU Lesser General Public License (LGPL) for more details.
-- 
-- You should have received a copy of the GNU Lesser General Public License along with
-- WebDeb-web. If not, see <http://www.gnu.org/licenses/>.
-- -----------------------------------------------------------------------------------------------

CREATE USER 'jira'@'localhost' IDENTIFIED BY '<ChangeThisPassword>';
CREATE DATABASE jira CHARACTER SET utf8 COLLATE utf8_bin;
GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,DROP,ALTER,INDEX on jira.* TO 'jirauser'@'localhost';
flush privileges;
