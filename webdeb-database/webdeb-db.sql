drop schema IF EXISTS `webdeb`;
CREATE DATABASE  IF NOT EXISTS `webdeb` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `webdeb`;

-- MySQL dump 10.13  Distrib 8.0.16, for Win64 (x86_64)
--
-- Host: localhost    Database: webdeb
-- ------------------------------------------------------
-- Server version	5.7.26-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
 SET NAMES utf8 ;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `actor`
--

DROP TABLE IF EXISTS `actor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `actor` (
  `id_contribution` bigint(20) NOT NULL,
  `crossref` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `id_type` int(11) DEFAULT NULL COMMENT 'actortype  = 0 	--> person\nactortype = 1 	--> organization',
  `id_picture` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `k_actor_t_actor_type1` (`id_type`),
  KEY `fk_actor_picture1` (`id_picture`),
  CONSTRAINT `fk_actor_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_actor_picture1` FOREIGN KEY (`id_picture`) REFERENCES `contributor_picture` (`id_picture`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_actor_t_actor_type1` FOREIGN KEY (`id_type`) REFERENCES `t_actor_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='actortype = 0 for person and 1 for organization';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `actor_has_affiliation`
--

DROP TABLE IF EXISTS `actor_has_affiliation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `actor_has_affiliation` (
  `id_aha` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_actor` bigint(20) NOT NULL,
  `id_actor_as_affiliation` bigint(20) DEFAULT NULL COMMENT 'may be null (at least function or affiliation must exist)',
  `start_date` varchar(9) COLLATE utf8_unicode_ci DEFAULT NULL,
  `end_date` varchar(9) COLLATE utf8_unicode_ci DEFAULT NULL,
  `start_date_type` int(11) DEFAULT NULL,
  `end_date_type` int(11) DEFAULT NULL,
  `function` int(11) DEFAULT NULL,
  `type` int(11) DEFAULT NULL,
  `version` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_aha`),
  KEY `k_actor_has_actor_actor2_idx` (`id_actor_as_affiliation`),
  KEY `k_actor_has_actor_actor1_idx` (`id_actor`),
  KEY `k_actor_has_affiliation_profession1_idx` (`function`),
  KEY `k_actor_has_affiliation_t_affiliation_type1` (`type`),
  KEY `k_actor_has_affiliation_start_date_type1` (`start_date_type`),
  KEY `k_actor_has_affiliation_end_date_type1` (`end_date_type`),
  CONSTRAINT `fk_actor_has_actor_actor1` FOREIGN KEY (`id_actor`) REFERENCES `actor` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_actor_has_actor_actor2` FOREIGN KEY (`id_actor_as_affiliation`) REFERENCES `actor` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_actor_has_affiliation_end_date_type1` FOREIGN KEY (`end_date_type`) REFERENCES `t_precision_date_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_actor_has_affiliation_profession1` FOREIGN KEY (`function`) REFERENCES `profession` (`id_profession`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_actor_has_affiliation_start_date_type1` FOREIGN KEY (`start_date_type`) REFERENCES `t_precision_date_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_actor_has_affiliation_t_affiliation_type1` FOREIGN KEY (`type`) REFERENCES `t_affiliation_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=40350 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `actor_i18names`
--

DROP TABLE IF EXISTS `actor_i18names`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `actor_i18names` (
  `id_contribution` bigint(20) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `first_or_acro` varchar(60) COLLATE utf8_unicode_ci DEFAULT NULL,
  `pseudo` varchar(60) COLLATE utf8_unicode_ci DEFAULT NULL,
  `lang` varchar(2) COLLATE utf8_unicode_ci NOT NULL,
  `is_old` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_contribution`,`lang`,`is_old`),
  FULLTEXT KEY `FULLNAME` (`first_or_acro`,`name`,`pseudo`),
  CONSTRAINT `fk_actor_i18names_actor1` FOREIGN KEY (`id_contribution`) REFERENCES `actor` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `advice`
--

DROP TABLE IF EXISTS `advice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `advice` (
  `id_advice` int(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id_advice`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `advice_i18names`
--

DROP TABLE IF EXISTS `advice_i18names`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `advice_i18names` (
  `id_advice` int(11) NOT NULL,
  `title` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `lang` varchar(2) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_advice`,`lang`),
  CONSTRAINT `fk_advice_i18names_advice1` FOREIGN KEY (`id_advice`) REFERENCES `advice` (`id_advice`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `argument`
--

DROP TABLE IF EXISTS `argument`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `argument` (
  `id_contribution` bigint(20) NOT NULL,
  `id_dictionary` bigint(20) NOT NULL,
  `id_type` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `fk_argument_argument_dictionary` (`id_dictionary`),
  CONSTRAINT `fk_argument_argument_dictionary` FOREIGN KEY (`id_dictionary`) REFERENCES `argument_dictionary` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_argument_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `argument_dictionary`
--

DROP TABLE IF EXISTS `argument_dictionary`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `argument_dictionary` (
  `id_contribution` bigint(20) NOT NULL,
  `title` varchar(512) COLLATE utf8_unicode_ci NOT NULL,
  `id_language` char(2) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_contribution`),
  UNIQUE KEY `uq_argument_dictionary_unique_shade_language_title_idx` (`id_language`,`title`),
  KEY `k_argument_dictionary_t_language` (`id_language`),
  FULLTEXT KEY `argument_dictionary_idx` (`title`),
  CONSTRAINT `fk_argument_dictionary_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_argument_dictionary_t_language1` FOREIGN KEY (`id_language`) REFERENCES `t_language` (`code`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `argument_justification_link`
--

DROP TABLE IF EXISTS `argument_justification_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `argument_justification_link` (
  `id_contribution` bigint(20) NOT NULL,
  `id_context` bigint(20) NOT NULL,
  `id_sub_context` bigint(20) DEFAULT NULL,
  `id_category` bigint(20) DEFAULT NULL,
  `id_super_argument` bigint(20) DEFAULT NULL,
  `id_argument` bigint(20) NOT NULL,
  `id_shade` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `k_ajl_argument1_idx` (`id_argument`),
  KEY `k_ajl_t_link_shade_type1_idx` (`id_shade`),
  KEY `fk_ajl_category1` (`id_category`),
  KEY `fk_ajl_super_argument1` (`id_super_argument`),
  KEY `fk_ajl_context_contribution1` (`id_context`),
  KEY `fk_ajl_sub_context_contribution1_idx` (`id_sub_context`),
  CONSTRAINT `fk_ajl_argument1` FOREIGN KEY (`id_argument`) REFERENCES `argument` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ajl_category1` FOREIGN KEY (`id_category`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ajl_context_contribution1` FOREIGN KEY (`id_context`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ajl_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ajl_sub_context_contribution1` FOREIGN KEY (`id_sub_context`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ajl_super_argument1` FOREIGN KEY (`id_super_argument`) REFERENCES `argument` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ajl_t_link_shade_type1` FOREIGN KEY (`id_shade`) REFERENCES `t_justification_link_shade_type` (`id_shade`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `argument_shaded`
--

DROP TABLE IF EXISTS `argument_shaded`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `argument_shaded` (
  `id_contribution` bigint(20) NOT NULL,
  `id_shade` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `k_argument_shaded_t_argument_shade_type1` (`id_shade`),
  CONSTRAINT `fk_argument_shaded_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_argument_shaded_t_argument_shade_type1` FOREIGN KEY (`id_shade`) REFERENCES `t_argument_shade_type` (`id_shade`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `argument_similarity_link`
--

DROP TABLE IF EXISTS `argument_similarity_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `argument_similarity_link` (
  `id_contribution` bigint(20) NOT NULL,
  `id_argument_from` bigint(20) NOT NULL,
  `id_argument_to` bigint(20) NOT NULL,
  `id_shade` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  UNIQUE KEY `uq_asl_argument` (`id_argument_from`,`id_argument_to`),
  KEY `k_asl_argument_from1_idx` (`id_argument_from`),
  KEY `k_asl_argument_to1_idx` (`id_argument_to`),
  KEY `k_asl_t_link_shade_type1_idx` (`id_shade`),
  CONSTRAINT `fk_asl_argument1` FOREIGN KEY (`id_argument_from`) REFERENCES `argument` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_asl_argument2` FOREIGN KEY (`id_argument_to`) REFERENCES `argument` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_asl_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_asl_t_link_shade_type1` FOREIGN KEY (`id_shade`) REFERENCES `t_similarity_link_shade_type` (`id_shade`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `citation`
--

DROP TABLE IF EXISTS `citation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `citation` (
  `id_contribution` bigint(20) NOT NULL,
  `original_excerpt` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `working_excerpt` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  `id_language` char(2) COLLATE utf8_unicode_ci DEFAULT NULL,
  `id_text` bigint(20) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `k_citation_t_language` (`id_language`),
  KEY `k_citation_text` (`id_text`),
  FULLTEXT KEY `citation_idx` (`original_excerpt`),
  CONSTRAINT `fk_citation_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_citation_t_language1` FOREIGN KEY (`id_language`) REFERENCES `t_language` (`code`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_citation_text` FOREIGN KEY (`id_text`) REFERENCES `text` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `citation_justification_link`
--

DROP TABLE IF EXISTS `citation_justification_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `citation_justification_link` (
  `id_contribution` bigint(20) NOT NULL,
  `id_context` bigint(20) NOT NULL,
  `id_sub_context` bigint(20) DEFAULT NULL,
  `id_category` bigint(20) DEFAULT NULL,
  `id_argument` bigint(20) DEFAULT NULL,
  `id_citation` bigint(20) NOT NULL,
  `id_shade` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `k_ejl_argument1_idx` (`id_argument`),
  KEY `k_ejl_t_link_shade_type1_idx` (`id_shade`),
  KEY `fk_ejl_category1` (`id_category`),
  KEY `fk_ejl_citation1` (`id_citation`),
  KEY `fk_ejl_sub_context_contribution1_idx` (`id_sub_context`),
  KEY `fk_ejl_super_context_contribution1` (`id_context`),
  CONSTRAINT `fk_ejl_argument1` FOREIGN KEY (`id_argument`) REFERENCES `argument` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ejl_category1` FOREIGN KEY (`id_category`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ejl_citation1` FOREIGN KEY (`id_citation`) REFERENCES `citation` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ejl_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ejl_sub_context_contribution1` FOREIGN KEY (`id_sub_context`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ejl_super_context_contribution1` FOREIGN KEY (`id_context`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_ejl_t_link_shade_type1` FOREIGN KEY (`id_shade`) REFERENCES `t_justification_link_shade_type` (`id_shade`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `citation_position_link`
--

DROP TABLE IF EXISTS `citation_position_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `citation_position_link` (
  `id_contribution` bigint(20) NOT NULL,
  `id_citation` bigint(20) NOT NULL,
  `id_debate` bigint(20) NOT NULL,
  `id_sub_debate` bigint(20) DEFAULT NULL,
  `id_shade` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  UNIQUE KEY `uq_cpl_citation_debate_subdebate` (`id_citation`,`id_debate`,`id_sub_debate`),
  KEY `k_cpl_debate1` (`id_debate`,`id_sub_debate`),
  KEY `k_cpl_shade1` (`id_shade`),
  KEY `fk_cpl_sub_debate1` (`id_sub_debate`),
  CONSTRAINT `fk_cpl_citation1` FOREIGN KEY (`id_citation`) REFERENCES `citation` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_cpl_debate1` FOREIGN KEY (`id_debate`) REFERENCES `debate` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_cpl_debate_shade1` FOREIGN KEY (`id_shade`) REFERENCES `t_position_link_shade_type` (`id_shade`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_cpl_sub_debate1` FOREIGN KEY (`id_sub_debate`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `context_has_category`
--

DROP TABLE IF EXISTS `context_has_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `context_has_category` (
  `id_contribution` bigint(20) NOT NULL,
  `id_context` bigint(20) NOT NULL,
  `id_category` bigint(20) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  UNIQUE KEY `uq_dhc_context_category` (`id_context`,`id_category`),
  KEY `k_chc_context_contribution1_idx` (`id_context`),
  KEY `k_chc_category1_idx` (`id_category`),
  CONSTRAINT `fk_chc_category1` FOREIGN KEY (`id_category`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_chc_context1` FOREIGN KEY (`id_context`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_chc_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contribution`
--

DROP TABLE IF EXISTS `contribution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contribution` (
  `id_contribution` bigint(20) NOT NULL AUTO_INCREMENT,
  `contribution_type` int(11) DEFAULT NULL,
  `hit` bigint(20) unsigned DEFAULT '0',
  `validated` int(11) NOT NULL,
  `locked` tinyint(1) DEFAULT '0',
  `sortkey` varchar(700) COLLATE utf8_unicode_ci DEFAULT NULL,
  `version` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint(1) NOT NULL DEFAULT '0',
  `hidden` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_contribution`),
  KEY `k_contribution_t_contribution_type1` (`contribution_type`),
  KEY `k_contribution_t_validation_state` (`validated`),
  FULLTEXT KEY `quicksearch` (`sortkey`),
  CONSTRAINT `fk_contribution_t_contribution_type1` FOREIGN KEY (`contribution_type`) REFERENCES `t_contribution_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contribution_t_validation_state` FOREIGN KEY (`validated`) REFERENCES `t_validation_state` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=180571 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contribution_has_actor`
--

DROP TABLE IF EXISTS `contribution_has_actor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contribution_has_actor` (
  `id_cha` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_contribution` bigint(20) NOT NULL,
  `id_actor` bigint(20) NOT NULL,
  `is_author` tinyint(1) DEFAULT '0' COMMENT 'Author (writter) of the contribution',
  `is_speaker` tinyint(1) DEFAULT '0' COMMENT 'Author (reported interview) of the contribution',
  `is_about` tinyint(1) DEFAULT '0',
  `actor_id_aha` bigint(20) DEFAULT NULL COMMENT 'technical ID of one affiliation/function (actor may have many and we trace a particular one)',
  `version` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_cha`),
  KEY `k_contribution_has_actor_actor1_idx` (`id_actor`),
  KEY `k_contribution_has_actor_contribution1_idx` (`id_contribution`),
  KEY `k_actor_has_affiliation` (`actor_id_aha`),
  CONSTRAINT `fk_actor_has_affiliation` FOREIGN KEY (`actor_id_aha`) REFERENCES `actor_has_affiliation` (`id_aha`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contribution_has_actor_actor1` FOREIGN KEY (`id_actor`) REFERENCES `actor` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contribution_has_actor_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2166 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contribution_has_contributor`
--

DROP TABLE IF EXISTS `contribution_has_contributor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contribution_has_contributor` (
  `id_contribution` bigint(20) NOT NULL,
  `id_contributor` bigint(20) NOT NULL,
  `version` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  `status` int(11) NOT NULL,
  `serialization` mediumtext COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_contribution`,`id_contributor`,`version`,`status`),
  KEY `k_contribution_has_contributor_contributor1_idx` (`id_contributor`),
  KEY `k_contribution_has_contributor_contribution1_idx` (`id_contribution`),
  KEY `k_contribution_has_contributor_t_modification_status1` (`status`),
  CONSTRAINT `fk_contribution_has_contributor_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contribution_has_contributor_contributor1` FOREIGN KEY (`id_contributor`) REFERENCES `contributor` (`id_contributor`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contribution_has_contributor_t_modification_status1` FOREIGN KEY (`status`) REFERENCES `t_modification_status` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contribution_has_place`
--

DROP TABLE IF EXISTS `contribution_has_place`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contribution_has_place` (
  `id_contribution` bigint(20) NOT NULL,
  `id_place` bigint(20) NOT NULL,
  PRIMARY KEY (`id_contribution`,`id_place`),
  KEY `k_contribution_has_place_place1_idx` (`id_place`),
  KEY `k_contribution_has_place_contribution1_idx` (`id_contribution`),
  CONSTRAINT `fk_contribution_has_place_contribution` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contribution_has_place_place` FOREIGN KEY (`id_place`) REFERENCES `place` (`id_place`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contribution_has_tag`
--

DROP TABLE IF EXISTS `contribution_has_tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contribution_has_tag` (
  `id_contribution` bigint(20) NOT NULL,
  `id_tag` bigint(20) NOT NULL,
  `version` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_contribution`,`id_tag`),
  KEY `fk_contribution_has_tag_tag1` (`id_tag`),
  CONSTRAINT `fk_contribution_has_tag_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contribution_has_tag_tag1` FOREIGN KEY (`id_tag`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contribution_in_group`
--

DROP TABLE IF EXISTS `contribution_in_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contribution_in_group` (
  `id_contribution` bigint(20) NOT NULL,
  `id_group` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution`,`id_group`),
  KEY `k_contribution_has_contributor_group_contributor_group1_idx` (`id_group`),
  KEY `k_contribution_has_contributor_group_contribution1_idx` (`id_contribution`),
  CONSTRAINT `fk_contribution_has_contributor_group_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contribution_has_contributor_group_contributor_group1` FOREIGN KEY (`id_group`) REFERENCES `contributor_group` (`id_group`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contribution_to_explore`
--

DROP TABLE IF EXISTS `contribution_to_explore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contribution_to_explore` (
  `id_contribution_to_explore` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_contribution` bigint(20) NOT NULL,
  `id_contributor_group` int(11) NOT NULL,
  `num_order` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution_to_explore`),
  UNIQUE KEY `uq_contribution_to_explore1` (`id_contribution`,`id_contributor_group`),
  KEY `k_contribution_to_explore_group1` (`id_contributor_group`),
  CONSTRAINT `fk_contribution_to_explore_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contribution_to_explore_group1` FOREIGN KEY (`id_contributor_group`) REFERENCES `contributor_group` (`id_group`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contributor`
--

DROP TABLE IF EXISTS `contributor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contributor` (
  `id_contributor` bigint(20) NOT NULL AUTO_INCREMENT,
  `firstname` varchar(60) COLLATE utf8_unicode_ci DEFAULT NULL,
  `lastname` varchar(60) COLLATE utf8_unicode_ci DEFAULT NULL,
  `email` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `password_hash` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `registration_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `gender` char(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `birth_year` int(4) DEFAULT NULL,
  `residence` char(2) COLLATE utf8_unicode_ci DEFAULT NULL,
  `auth_token` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `confirmation_token` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `newsletter_token` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `validated` tinyint(1) DEFAULT '0',
  `default_group` int(11) NOT NULL DEFAULT '0',
  `is_banned` tinyint(1) NOT NULL DEFAULT '0',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `pedagogic` tinyint(1) DEFAULT '0',
  `newsletter` tinyint(1) DEFAULT '1',
  `browser_warned` tinyint(1) DEFAULT '0' COMMENT 'True if the user want to use old browser',
  `id_picture` bigint(20) DEFAULT NULL,
  `version` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `auth_token_expiration_date` timestamp NULL DEFAULT NULL,
  `newsletter_token_expiration_date` timestamp NULL DEFAULT NULL,
  `pseudo` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `tmp_contributor` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_contributor`),
  UNIQUE KEY `uq_user_pseudo` (`pseudo`),
  UNIQUE KEY `uq_user_email` (`email`),
  KEY `k_contributor_t_gender1` (`gender`),
  KEY `k_contributor_contributor_group1` (`default_group`),
  KEY `k_contributor_t_country1` (`residence`),
  KEY `k_contributor_tmp_contributor1` (`tmp_contributor`),
  KEY `fk_contributor_picture1` (`id_picture`),
  FULLTEXT KEY `contributor_search` (`firstname`,`lastname`,`email`),
  CONSTRAINT `fk_contributor_contributor_group1` FOREIGN KEY (`default_group`) REFERENCES `contributor_group` (`id_group`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_picture1` FOREIGN KEY (`id_picture`) REFERENCES `contributor_picture` (`id_picture`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_t_country1` FOREIGN KEY (`residence`) REFERENCES `t_country` (`id_country`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_t_gender1` FOREIGN KEY (`gender`) REFERENCES `t_gender` (`id_gender`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_tmp_contributor1` FOREIGN KEY (`tmp_contributor`) REFERENCES `tmp_contributor` (`id_tmp_contributor`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2066 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contributor_group`
--

DROP TABLE IF EXISTS `contributor_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contributor_group` (
  `id_group` int(11) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(140) COLLATE utf8_unicode_ci NOT NULL,
  `group_description` varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL,
  `is_open` tinyint(1) NOT NULL DEFAULT '0',
  `member_visibility` int(11) NOT NULL,
  `contribution_visibility` int(11) NOT NULL,
  `group_color` varchar(6) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'E97451',
  `version` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_group`),
  UNIQUE KEY `group_name_UNIQUE` (`group_name`),
  KEY `k_contributor_group_t_member_visibility1_idx` (`member_visibility`),
  KEY `k_contributor_group_t_contribution_visibility1_idx` (`contribution_visibility`),
  KEY `k_contributor_group_t_group_color_idx` (`group_color`),
  CONSTRAINT `fk_contributor_group_t_contribution_visibility1` FOREIGN KEY (`contribution_visibility`) REFERENCES `t_contribution_visibility` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_group_t_member_visibility1` FOREIGN KEY (`member_visibility`) REFERENCES `t_member_visibility` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=143 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contributor_has_affiliation`
--

DROP TABLE IF EXISTS `contributor_has_affiliation`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contributor_has_affiliation` (
  `id_cha` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_contributor` bigint(20) NOT NULL,
  `id_actor` bigint(20) DEFAULT '-1',
  `start_date` varchar(9) COLLATE utf8_unicode_ci DEFAULT NULL,
  `end_date` varchar(9) COLLATE utf8_unicode_ci DEFAULT NULL,
  `start_date_type` int(11) DEFAULT NULL,
  `end_date_type` int(11) DEFAULT NULL,
  `function` int(11) DEFAULT NULL,
  `version` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_cha`),
  KEY `k_contributor_has_actor_actor1_idx` (`id_actor`),
  KEY `k_contributor_has_actor_contributor1_idx` (`id_contributor`),
  KEY `k_contributor_has_affiliation_profession1_idx` (`function`),
  KEY `k_contributor_has_affiliation_start_date_type1` (`start_date_type`),
  KEY `k_contributor_has_affiliation_end_date_type1` (`end_date_type`),
  CONSTRAINT `fk_contributor_has_actor_actor1` FOREIGN KEY (`id_actor`) REFERENCES `actor` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_has_actor_contributor1` FOREIGN KEY (`id_contributor`) REFERENCES `contributor` (`id_contributor`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_has_affiliation_end_date_type1` FOREIGN KEY (`end_date_type`) REFERENCES `t_precision_date_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_has_affiliation_profession1` FOREIGN KEY (`function`) REFERENCES `profession` (`id_profession`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_has_affiliation_start_date_type1` FOREIGN KEY (`start_date_type`) REFERENCES `t_precision_date_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contributor_has_group`
--

DROP TABLE IF EXISTS `contributor_has_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contributor_has_group` (
  `id_group` int(11) NOT NULL,
  `id_contributor` bigint(20) NOT NULL,
  `id_role` int(11) NOT NULL,
  `is_banned` tinyint(1) NOT NULL DEFAULT '0',
  `is_followed` tinyint(1) NOT NULL DEFAULT '1',
  `invitation` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL,
  `version` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id_group`,`id_contributor`,`id_role`),
  KEY `k_group_has_contributor_contributor1_idx` (`id_contributor`),
  KEY `k_group_has_contributor_group1_idx` (`id_group`),
  KEY `k_contributor_has_group_contributor1_idx` (`id_contributor`),
  KEY `k_contributor_has_group_group1_idx` (`id_group`),
  KEY `k_contributor_has_group_role1_idx` (`id_role`),
  CONSTRAINT `fk_contributor_has_group_role1` FOREIGN KEY (`id_role`) REFERENCES `role` (`id_role`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_group_has_contributor_contributor1` FOREIGN KEY (`id_contributor`) REFERENCES `contributor` (`id_contributor`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_group_has_contributor_group1` FOREIGN KEY (`id_group`) REFERENCES `contributor_group` (`id_group`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contributor_in_project_subgroup`
--

DROP TABLE IF EXISTS `contributor_in_project_subgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contributor_in_project_subgroup` (
  `id_contributor` bigint(20) NOT NULL,
  `id_project_subgroup` int(11) NOT NULL,
  PRIMARY KEY (`id_contributor`,`id_project_subgroup`),
  KEY `k_contributor_in_project_subgroup_subgroup1_idx` (`id_project_subgroup`),
  KEY `k_contributor_in_project_subgroup_contributor1_idx` (`id_contributor`),
  CONSTRAINT `fk_contributor_in_project_subgroup_contributor1` FOREIGN KEY (`id_contributor`) REFERENCES `contributor` (`id_contributor`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_in_project_subgroup_subgroup1` FOREIGN KEY (`id_project_subgroup`) REFERENCES `project_subgroup` (`id_project_subgroup`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `contributor_picture`
--

DROP TABLE IF EXISTS `contributor_picture`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `contributor_picture` (
  `id_picture` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_contributor` bigint(20) DEFAULT NULL,
  `url` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `author` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `extension` varchar(8) COLLATE utf8_unicode_ci NOT NULL,
  `id_source` int(11) NOT NULL,
  `id_licence_type` int(11) NOT NULL,
  PRIMARY KEY (`id_picture`),
  KEY `fk_contributor_picture_contributor1` (`id_contributor`),
  KEY `fk_contributor_picture_source1` (`id_source`),
  KEY `fk_contributor_picture_licence_type1` (`id_licence_type`),
  CONSTRAINT `fk_contributor_picture_contributor1` FOREIGN KEY (`id_contributor`) REFERENCES `contributor` (`id_contributor`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_picture_licence_type1` FOREIGN KEY (`id_licence_type`) REFERENCES `t_picture_licence_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_contributor_picture_source1` FOREIGN KEY (`id_source`) REFERENCES `t_contributor_picture_source` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=180561 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `debate`
--

DROP TABLE IF EXISTS `debate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `debate` (
  `id_contribution` bigint(20) NOT NULL,
  `id_argument` bigint(20) NOT NULL,
  `description` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `id_picture` bigint(20) DEFAULT NULL,
  `id_shade` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `fk_debate_picture1` (`id_picture`),
  KEY `fk_debate_argument1` (`id_argument`),
  KEY `fk_debate_t_debate_shade_type1` (`id_shade`),
  CONSTRAINT `fk_debate_argument1` FOREIGN KEY (`id_argument`) REFERENCES `argument_shaded` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_debate_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_debate_picture1` FOREIGN KEY (`id_picture`) REFERENCES `contributor_picture` (`id_picture`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_debate_t_debate_shade_type1` FOREIGN KEY (`id_shade`) REFERENCES `t_debate_shade_type` (`id_shade`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `debate_external_url`
--

DROP TABLE IF EXISTS `debate_external_url`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `debate_external_url` (
  `id_url` bigint(20) NOT NULL AUTO_INCREMENT,
  `url` varchar(2048) COLLATE utf8_unicode_ci NOT NULL,
  `alias` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `id_debate` bigint(20) NOT NULL,
  PRIMARY KEY (`id_url`),
  KEY `k_debate_type1` (`id_debate`),
  CONSTRAINT `debate_external_url_debate` FOREIGN KEY (`id_debate`) REFERENCES `debate` (`id_contribution`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `debate_has_tag_debate`
--

DROP TABLE IF EXISTS `debate_has_tag_debate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `debate_has_tag_debate` (
  `id_contribution` bigint(20) NOT NULL,
  `id_debate` bigint(20) NOT NULL,
  `id_tag` bigint(20) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  UNIQUE KEY `k_debate_link_unique_link_idx` (`id_debate`,`id_tag`),
  KEY `k_debate_link_debate_idx` (`id_debate`),
  KEY `k_debate_link_tag_idx` (`id_tag`),
  CONSTRAINT `fk_debate_link_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_debate_link_debate1` FOREIGN KEY (`id_debate`) REFERENCES `debate` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_debate_link_tag1` FOREIGN KEY (`id_tag`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `debate_multiple`
--

DROP TABLE IF EXISTS `debate_multiple`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `debate_multiple` (
  `id_contribution` bigint(20) NOT NULL,
  `id_argument` bigint(20) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `fk_debate_multiple_argument1` (`id_argument`),
  CONSTRAINT `fk_debate_multiple_argument1` FOREIGN KEY (`id_argument`) REFERENCES `argument` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_debate_multiple_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `debate` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `debate_similarity_link`
--

DROP TABLE IF EXISTS `debate_similarity_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `debate_similarity_link` (
  `id_contribution` bigint(20) NOT NULL,
  `id_debate_from` bigint(20) NOT NULL,
  `id_debate_to` bigint(20) NOT NULL,
  `id_shade` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  UNIQUE KEY `uq_dsl_debate` (`id_debate_from`,`id_debate_to`),
  KEY `k_dsl_debate_from1_idx` (`id_debate_from`),
  KEY `k_dsl_debate_to1_idx` (`id_debate_to`),
  KEY `k_dsl_t_link_shade_type1_idx` (`id_shade`),
  CONSTRAINT `fk_dsl_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_dsl_debate1` FOREIGN KEY (`id_debate_from`) REFERENCES `debate` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_dsl_debate2` FOREIGN KEY (`id_debate_to`) REFERENCES `debate` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_dsl_t_link_shade_type1` FOREIGN KEY (`id_shade`) REFERENCES `t_similarity_link_shade_type` (`id_shade`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `external_contribution`
--

DROP TABLE IF EXISTS `external_contribution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `external_contribution` (
  `id_contribution` bigint(20) NOT NULL,
  `id_internal_contribution` bigint(20) DEFAULT NULL,
  `source_url` varchar(2048) COLLATE utf8_unicode_ci NOT NULL,
  `external_source` int(11) NOT NULL,
  `language` char(2) COLLATE utf8_unicode_ci DEFAULT NULL,
  `title` varchar(1024) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `k_external_contribution_internal_contribution` (`id_internal_contribution`),
  KEY `k_external_contribution_external_source` (`external_source`),
  KEY `k_external_contribution_external_source_t_language1` (`language`),
  CONSTRAINT `fk_external_contribution_external_contribution` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_external_contribution_external_source` FOREIGN KEY (`external_source`) REFERENCES `t_external_source_name` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_external_contribution_external_source_t_language1` FOREIGN KEY (`language`) REFERENCES `t_language` (`code`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_external_contribution_internal_contribution` FOREIGN KEY (`id_internal_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_has_permission`
--

DROP TABLE IF EXISTS `group_has_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `group_has_permission` (
  `id_group` int(11) NOT NULL,
  `id_permission` int(11) NOT NULL,
  PRIMARY KEY (`id_group`,`id_permission`),
  KEY `k_group_has_permission_permission1_idx` (`id_permission`),
  KEY `k_group_has_permission_group1_idx` (`id_group`),
  CONSTRAINT `fk_group_has_permission_group1` FOREIGN KEY (`id_group`) REFERENCES `contributor_group` (`id_group`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_group_has_permission_permission1` FOREIGN KEY (`id_permission`) REFERENCES `permission` (`id_permission`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organization`
--

DROP TABLE IF EXISTS `organization`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `organization` (
  `id_contribution` bigint(20) NOT NULL,
  `legal_status` int(11) DEFAULT NULL,
  `creation_date` varchar(9) COLLATE utf8_unicode_ci DEFAULT NULL,
  `termination_date` varchar(9) COLLATE utf8_unicode_ci DEFAULT NULL,
  `official_number` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `k_organization_t_legal_status1` (`legal_status`),
  CONSTRAINT `fk_organization_actor1` FOREIGN KEY (`id_contribution`) REFERENCES `actor` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_organization_t_legal_status1` FOREIGN KEY (`legal_status`) REFERENCES `t_legal_status` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `organization_has_sector`
--

DROP TABLE IF EXISTS `organization_has_sector`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `organization_has_sector` (
  `id_contribution` bigint(20) NOT NULL,
  `business_sector` int(11) NOT NULL,
  PRIMARY KEY (`id_contribution`,`business_sector`),
  KEY `k_table1_organization1_idx` (`id_contribution`),
  KEY `k_organization_has_t_business_sector_t_business_sector1` (`business_sector`),
  CONSTRAINT `fk_organization_has_t_business_sector_t_business_sector1` FOREIGN KEY (`business_sector`) REFERENCES `t_business_sector` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_table1_organization1` FOREIGN KEY (`id_contribution`) REFERENCES `organization` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `permission`
--

DROP TABLE IF EXISTS `permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `permission` (
  `id_permission` int(11) NOT NULL,
  `name` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `description` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_permission`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `person`
--

DROP TABLE IF EXISTS `person`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `person` (
  `id_contribution` bigint(20) NOT NULL,
  `birthdate` varchar(9) COLLATE utf8_unicode_ci DEFAULT NULL,
  `deathdate` varchar(9) COLLATE utf8_unicode_ci DEFAULT NULL,
  `gender` char(1) COLLATE utf8_unicode_ci DEFAULT NULL,
  `residence` char(2) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `k_Person_actor1_idx` (`id_contribution`),
  KEY `k_person_t_gender1` (`gender`),
  KEY `k_person_t_country1` (`residence`),
  CONSTRAINT `fk_Person_actor1` FOREIGN KEY (`id_contribution`) REFERENCES `actor` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_person_t_country1` FOREIGN KEY (`residence`) REFERENCES `t_country` (`id_country`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_person_t_gender1` FOREIGN KEY (`gender`) REFERENCES `t_gender` (`id_gender`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `place`
--

DROP TABLE IF EXISTS `place`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `place` (
  `id_place` bigint(20) NOT NULL AUTO_INCREMENT,
  `geoname_id` bigint(20) DEFAULT NULL,
  `code` char(3) COLLATE utf8_unicode_ci DEFAULT NULL,
  `latitude` varchar(25) COLLATE utf8_unicode_ci DEFAULT NULL,
  `longitude` varchar(25) COLLATE utf8_unicode_ci DEFAULT NULL,
  `placetype` int(11) NOT NULL,
  `id_continent` bigint(20) DEFAULT NULL,
  `id_country` bigint(20) DEFAULT NULL,
  `id_region` bigint(20) DEFAULT NULL,
  `id_subregion` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id_place`),
  UNIQUE KEY `geoname_id` (`geoname_id`),
  KEY `k_t_place_type` (`placetype`),
  KEY `k_place_continent` (`id_continent`),
  KEY `k_place_country` (`id_country`),
  KEY `k_place_region` (`id_region`),
  KEY `k_place_subregion` (`id_subregion`),
  CONSTRAINT `fk_place_continent` FOREIGN KEY (`id_continent`) REFERENCES `place` (`id_place`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_place_country` FOREIGN KEY (`id_country`) REFERENCES `place` (`id_place`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_place_region` FOREIGN KEY (`id_region`) REFERENCES `place` (`id_place`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_place_subregion` FOREIGN KEY (`id_subregion`) REFERENCES `place` (`id_place`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_t_place_type` FOREIGN KEY (`placetype`) REFERENCES `t_place_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=146 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `place_i18names`
--

DROP TABLE IF EXISTS `place_i18names`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `place_i18names` (
  `id_place` bigint(20) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `lang` varchar(2) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_place`,`lang`),
  CONSTRAINT `fk_place_i18names_place` FOREIGN KEY (`id_place`) REFERENCES `place` (`id_place`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `profession`
--

DROP TABLE IF EXISTS `profession`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `profession` (
  `id_profession` int(11) NOT NULL AUTO_INCREMENT,
  `id_type` int(11) NOT NULL,
  `display_hierarchy` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_profession`),
  KEY `profession_t_profession_type1` (`id_type`),
  CONSTRAINT `profession_t_profession_type1` FOREIGN KEY (`id_type`) REFERENCES `t_profession_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15319 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `profession_has_link`
--

DROP TABLE IF EXISTS `profession_has_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `profession_has_link` (
  `id_profession_from` int(11) NOT NULL,
  `id_profession_to` int(11) NOT NULL,
  `link_type` int(11) NOT NULL,
  PRIMARY KEY (`id_profession_from`,`id_profession_to`,`link_type`),
  KEY `k_profession_has_profession_profession2_idx` (`id_profession_to`),
  KEY `k_profession_has_profession_profession1_idx` (`id_profession_from`),
  KEY `k_profession_link_profession_link1_idx` (`link_type`),
  CONSTRAINT `fk_profession_has_profession_profession1` FOREIGN KEY (`id_profession_from`) REFERENCES `profession` (`id_profession`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_profession_has_profession_profession2` FOREIGN KEY (`id_profession_to`) REFERENCES `profession` (`id_profession`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_profession_link_profession_link1` FOREIGN KEY (`link_type`) REFERENCES `profession_link` (`id_link`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `profession_i18names`
--

DROP TABLE IF EXISTS `profession_i18names`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `profession_i18names` (
  `profession` int(11) NOT NULL,
  `lang` char(2) COLLATE utf8_unicode_ci NOT NULL,
  `spelling` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `gender` char(1) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`profession`,`lang`,`gender`),
  KEY `k_profession_i18names_t_language1_idx` (`lang`),
  KEY `k_profession_i18names_t_gender` (`gender`),
  FULLTEXT KEY `profession_idx` (`spelling`),
  CONSTRAINT `fk_profession_i18names_profession1` FOREIGN KEY (`profession`) REFERENCES `profession` (`id_profession`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_profession_i18names_t_gender` FOREIGN KEY (`gender`) REFERENCES `t_word_gender` (`id_word_gender`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_profession_i18names_t_language1` FOREIGN KEY (`lang`) REFERENCES `t_language` (`code`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `profession_link`
--

DROP TABLE IF EXISTS `profession_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `profession_link` (
  `id_link` int(11) NOT NULL,
  `description` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_link`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project`
--

DROP TABLE IF EXISTS `project`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `project` (
  `id_project` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `technical_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `begin_date` date NOT NULL,
  `end_date` date NOT NULL,
  `pedagogic` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id_project`),
  FULLTEXT KEY `FULLNAME` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project_group`
--

DROP TABLE IF EXISTS `project_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `project_group` (
  `id_project_group` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `technical_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_project_group`),
  FULLTEXT KEY `FULLNAME` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project_has_group`
--

DROP TABLE IF EXISTS `project_has_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `project_has_group` (
  `id_project` int(11) NOT NULL,
  `id_project_group` int(11) NOT NULL,
  PRIMARY KEY (`id_project`,`id_project_group`),
  KEY `k_project_has_group_project1_idx` (`id_project`),
  KEY `k_project_has_group_project_group1_idx` (`id_project_group`),
  CONSTRAINT `fk_project_has_group_project1` FOREIGN KEY (`id_project`) REFERENCES `project` (`id_project`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_project_has_group_project_group1` FOREIGN KEY (`id_project_group`) REFERENCES `project_group` (`id_project_group`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project_subgroup`
--

DROP TABLE IF EXISTS `project_subgroup`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `project_subgroup` (
  `id_project_subgroup` int(11) NOT NULL AUTO_INCREMENT,
  `id_project_group` int(11) NOT NULL,
  `id_project` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `technical_name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `nb_contributors` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_project_subgroup`),
  KEY `k_project_subgroup_project1_idx` (`id_project`),
  KEY `k_project_subgroup_project_group1_idx` (`id_project_group`),
  FULLTEXT KEY `FULLNAME` (`name`),
  CONSTRAINT `fk_project_subgroup_project1` FOREIGN KEY (`id_project`) REFERENCES `project` (`id_project`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_project_subgroup_project_group1` FOREIGN KEY (`id_project_group`) REFERENCES `project_group` (`id_project_group`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `project_subgroup_has_contributor_group`
--

DROP TABLE IF EXISTS `project_subgroup_has_contributor_group`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `project_subgroup_has_contributor_group` (
  `id_project_subgroup` int(11) NOT NULL,
  `id_contributor_group` int(11) NOT NULL,
  PRIMARY KEY (`id_contributor_group`,`id_project_subgroup`),
  KEY `k_contributor_in_project_subgroup_subgroup1_idx` (`id_project_subgroup`),
  KEY `k_contributor_in_project_subgroup_contributor1_idx` (`id_contributor_group`),
  CONSTRAINT `fk_project_subgroup_has_contributor_group_group1` FOREIGN KEY (`id_contributor_group`) REFERENCES `contributor_group` (`id_group`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_project_subgroup_has_contributor_group_subgroup1` FOREIGN KEY (`id_project_subgroup`) REFERENCES `project_subgroup` (`id_project_subgroup`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role`
--

DROP TABLE IF EXISTS `role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `role` (
  `id_role` int(11) NOT NULL,
  `role_name` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_has_permission`
--

DROP TABLE IF EXISTS `role_has_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `role_has_permission` (
  `id_role` int(11) NOT NULL,
  `id_permission` int(11) NOT NULL,
  PRIMARY KEY (`id_role`,`id_permission`),
  KEY `k_permission_has_role_role1_idx` (`id_role`),
  KEY `k_permission_has_role_permission1_idx` (`id_permission`),
  CONSTRAINT `fk_permission_has_role_permission1` FOREIGN KEY (`id_permission`) REFERENCES `permission` (`id_permission`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_permission_has_role_role1` FOREIGN KEY (`id_role`) REFERENCES `role` (`id_role`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_actor_type`
--

DROP TABLE IF EXISTS `t_actor_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_actor_type` (
  `id_type` int(11) NOT NULL,
  `name` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_affiliation_actor_type`
--

DROP TABLE IF EXISTS `t_affiliation_actor_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_affiliation_actor_type` (
  `id_type` int(11) NOT NULL,
  `name` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_affiliation_subtype`
--

DROP TABLE IF EXISTS `t_affiliation_subtype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_affiliation_subtype` (
  `id_type` int(11) NOT NULL,
  `name` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_affiliation_type`
--

DROP TABLE IF EXISTS `t_affiliation_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_affiliation_type` (
  `id_type` int(11) NOT NULL,
  `id_actor_type` int(11) NOT NULL,
  `id_subtype` int(11) NOT NULL,
  `en` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`),
  KEY `t_affiliation_type_t_affiliation_actor_type1` (`id_actor_type`),
  KEY `t_affiliation_type_t_affiliation_subtype1` (`id_subtype`),
  CONSTRAINT `t_affiliation_type_t_affiliation_actor_type1` FOREIGN KEY (`id_actor_type`) REFERENCES `t_affiliation_actor_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `t_affiliation_type_t_affiliation_subtype1` FOREIGN KEY (`id_subtype`) REFERENCES `t_affiliation_subtype` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_argument_shade_type`
--

DROP TABLE IF EXISTS `t_argument_shade_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_argument_shade_type` (
  `id_shade` int(11) NOT NULL,
  `en` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_shade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_argument_type`
--

DROP TABLE IF EXISTS `t_argument_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_argument_type` (
  `id_type` int(11) NOT NULL,
  `en` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_business_sector`
--

DROP TABLE IF EXISTS `t_business_sector`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_business_sector` (
  `id_type` int(11) NOT NULL,
  `en` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_contribution_type`
--

DROP TABLE IF EXISTS `t_contribution_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_contribution_type` (
  `id_type` int(11) NOT NULL,
  `name` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `en` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_contribution_visibility`
--

DROP TABLE IF EXISTS `t_contribution_visibility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_contribution_visibility` (
  `id_type` int(11) NOT NULL,
  `description` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_contributor_picture_source`
--

DROP TABLE IF EXISTS `t_contributor_picture_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_contributor_picture_source` (
  `id_type` int(11) NOT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_copyrightfree_source`
--

DROP TABLE IF EXISTS `t_copyrightfree_source`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_copyrightfree_source` (
  `id_type` int(11) NOT NULL AUTO_INCREMENT,
  `domain_name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_country`
--

DROP TABLE IF EXISTS `t_country`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_country` (
  `id_country` char(2) COLLATE utf8_unicode_ci NOT NULL,
  `en` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(50) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_country`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_debate_shade_type`
--

DROP TABLE IF EXISTS `t_debate_shade_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_debate_shade_type` (
  `id_shade` int(11) NOT NULL,
  `en` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_shade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_external_source_name`
--

DROP TABLE IF EXISTS `t_external_source_name`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_external_source_name` (
  `id_type` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`),
  FULLTEXT KEY `name_idx` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_gender`
--

DROP TABLE IF EXISTS `t_gender`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_gender` (
  `id_gender` char(1) COLLATE utf8_unicode_ci NOT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_gender`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_justification_link_shade_type`
--

DROP TABLE IF EXISTS `t_justification_link_shade_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_justification_link_shade_type` (
  `id_shade` int(11) NOT NULL,
  `en` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_shade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_language`
--

DROP TABLE IF EXISTS `t_language`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_language` (
  `code` char(2) COLLATE utf8_unicode_ci NOT NULL,
  `en` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `own` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_legal_status`
--

DROP TABLE IF EXISTS `t_legal_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_legal_status` (
  `id_type` int(11) NOT NULL,
  `en` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_member_visibility`
--

DROP TABLE IF EXISTS `t_member_visibility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_member_visibility` (
  `id_type` int(11) NOT NULL,
  `description` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_modification_status`
--

DROP TABLE IF EXISTS `t_modification_status`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_modification_status` (
  `id_type` int(11) NOT NULL,
  `name` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `en` varchar(25) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(25) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(25) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_picture_licence_type`
--

DROP TABLE IF EXISTS `t_picture_licence_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_picture_licence_type` (
  `id_type` int(11) NOT NULL,
  `url` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_place_type`
--

DROP TABLE IF EXISTS `t_place_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_place_type` (
  `id_type` int(11) NOT NULL,
  `en` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_position_link_shade_type`
--

DROP TABLE IF EXISTS `t_position_link_shade_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_position_link_shade_type` (
  `id_shade` int(11) NOT NULL,
  `en` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_shade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_precision_date_type`
--

DROP TABLE IF EXISTS `t_precision_date_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_precision_date_type` (
  `id_type` int(11) NOT NULL,
  `is_past` tinyint(1) NOT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_profession_subtype`
--

DROP TABLE IF EXISTS `t_profession_subtype`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_profession_subtype` (
  `id_type` int(11) NOT NULL,
  `name` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_profession_type`
--

DROP TABLE IF EXISTS `t_profession_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_profession_type` (
  `id_type` int(11) NOT NULL,
  `id_subtype` int(11) NOT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`),
  KEY `k_t_profession_type_t_profession_subtype1` (`id_subtype`),
  CONSTRAINT `t_profession_type_t_profession_subtype1` FOREIGN KEY (`id_subtype`) REFERENCES `t_profession_subtype` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_similarity_link_shade_type`
--

DROP TABLE IF EXISTS `t_similarity_link_shade_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_similarity_link_shade_type` (
  `id_shade` int(11) NOT NULL,
  `en` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_shade`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_tag_type`
--

DROP TABLE IF EXISTS `t_tag_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_tag_type` (
  `id_type` int(11) NOT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_text_source_type`
--

DROP TABLE IF EXISTS `t_text_source_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_text_source_type` (
  `id_type` int(11) NOT NULL,
  `en` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_text_type`
--

DROP TABLE IF EXISTS `t_text_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_text_type` (
  `id_type` int(11) NOT NULL,
  `en` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_text_visibility`
--

DROP TABLE IF EXISTS `t_text_visibility`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_text_visibility` (
  `id_visibility` int(11) NOT NULL,
  `en` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(120) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_visibility`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_validation_state`
--

DROP TABLE IF EXISTS `t_validation_state`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_validation_state` (
  `id_type` int(11) NOT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_warned_word`
--

DROP TABLE IF EXISTS `t_warned_word`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_warned_word` (
  `id_warned_word` int(11) NOT NULL,
  `id_context_type` int(11) NOT NULL,
  `id_type` int(11) NOT NULL,
  `id_language` char(2) COLLATE utf8_unicode_ci NOT NULL,
  `title` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_warned_word`),
  KEY `k_warned_word_context_type` (`id_context_type`),
  KEY `k_warned_word_type` (`id_type`),
  KEY `k_warned_word_t_language` (`id_language`),
  CONSTRAINT `fk_warned_word_t_context_type` FOREIGN KEY (`id_context_type`) REFERENCES `t_warned_word_context_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_warned_word_t_language1` FOREIGN KEY (`id_language`) REFERENCES `t_language` (`code`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_warned_word_t_type` FOREIGN KEY (`id_type`) REFERENCES `t_warned_word_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_warned_word_context_type`
--

DROP TABLE IF EXISTS `t_warned_word_context_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_warned_word_context_type` (
  `id_type` int(11) NOT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_warned_word_type`
--

DROP TABLE IF EXISTS `t_warned_word_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_warned_word_type` (
  `id_type` int(11) NOT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `t_word_gender`
--

DROP TABLE IF EXISTS `t_word_gender`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `t_word_gender` (
  `id_word_gender` char(1) COLLATE utf8_unicode_ci NOT NULL,
  `en` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `fr` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  `nl` varchar(20) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_word_gender`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag`
--

DROP TABLE IF EXISTS `tag`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tag` (
  `id_contribution` bigint(20) NOT NULL,
  `id_type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_contribution`),
  KEY `k_tag_t_tag_type1` (`id_type`),
  CONSTRAINT `fk_tag_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_tag_t_tag_type1` FOREIGN KEY (`id_type`) REFERENCES `t_tag_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag_i18names`
--

DROP TABLE IF EXISTS `tag_i18names`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tag_i18names` (
  `id_contribution` bigint(20) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `id_language` varchar(2) COLLATE utf8_unicode_ci NOT NULL,
  `id_type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id_contribution`,`id_language`),
  UNIQUE KEY `k_tag_unique_language_title_idx` (`id_language`,`name`,`id_type`),
  KEY `k_tag_i18names_t_language` (`id_language`),
  KEY `fk_tag_i18names_t_tag_type1` (`id_type`),
  FULLTEXT KEY `FULLNAME` (`name`),
  CONSTRAINT `fk_tag_i18names_t_language1` FOREIGN KEY (`id_language`) REFERENCES `t_language` (`code`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_tag_i18names_t_tag_type1` FOREIGN KEY (`id_type`) REFERENCES `t_tag_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_tag_i18names_tag1` FOREIGN KEY (`id_contribution`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag_link`
--

DROP TABLE IF EXISTS `tag_link`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tag_link` (
  `id_contribution` bigint(20) NOT NULL,
  `id_tag_from` bigint(20) NOT NULL,
  `id_tag_to` bigint(20) NOT NULL,
  PRIMARY KEY (`id_contribution`),
  UNIQUE KEY `k_tag_unique_link_idx` (`id_tag_to`,`id_tag_from`),
  KEY `k_tag_has_tag_tag2_idx` (`id_tag_to`),
  KEY `k_tag_has_tag_tag1_idx` (`id_tag_from`),
  CONSTRAINT `fk_tag_has_tag_tag1` FOREIGN KEY (`id_tag_from`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_tag_has_tag_tag2` FOREIGN KEY (`id_tag_to`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_tag_link_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tag_rewording_i18names`
--

DROP TABLE IF EXISTS `tag_rewording_i18names`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tag_rewording_i18names` (
  `id_tag_rewording` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_contribution` bigint(20) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `id_language` varchar(2) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_tag_rewording`),
  KEY `k_tag_rewording_i18names_tag1` (`id_contribution`),
  KEY `k_tag_rewording_i18names_t_language` (`id_language`),
  FULLTEXT KEY `FULLNAME` (`name`),
  CONSTRAINT `fk_tag_rewording_i18names_t_language1` FOREIGN KEY (`id_language`) REFERENCES `t_language` (`code`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_tag_rewording_i18names_tag1` FOREIGN KEY (`id_contribution`) REFERENCES `tag` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `text`
--

DROP TABLE IF EXISTS `text`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `text` (
  `id_contribution` bigint(20) NOT NULL,
  `id_language` char(2) COLLATE utf8_unicode_ci DEFAULT NULL,
  `publication_date` varchar(9) COLLATE utf8_unicode_ci DEFAULT NULL,
  `id_type` int(11) DEFAULT '2',
  `id_source_type` int(11) NOT NULL,
  `id_source_name` int(11) DEFAULT NULL,
  `url` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `id_visibility` int(11) NOT NULL DEFAULT '0',
  `fetched` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id_contribution`),
  KEY `k_text_text_source_name1_idx` (`id_source_name`),
  KEY `k_text_t_text_type1` (`id_type`),
  KEY `k_text_t_text_source_type1` (`id_type`),
  KEY `k_text_t_language1` (`id_language`),
  KEY `k_text_t_text_visibility1` (`id_visibility`),
  KEY `fk_text_t_text_source_type1` (`id_source_type`),
  CONSTRAINT `fk_text_contribution1` FOREIGN KEY (`id_contribution`) REFERENCES `contribution` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_text_t_language1` FOREIGN KEY (`id_language`) REFERENCES `t_language` (`code`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_text_t_text_source_type1` FOREIGN KEY (`id_source_type`) REFERENCES `t_text_source_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_text_t_text_type1` FOREIGN KEY (`id_type`) REFERENCES `t_text_type` (`id_type`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_text_t_text_visibility1` FOREIGN KEY (`id_visibility`) REFERENCES `t_text_visibility` (`id_visibility`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_text_text_source_name1` FOREIGN KEY (`id_source_name`) REFERENCES `text_source_name` (`id_source`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `text_content`
--

DROP TABLE IF EXISTS `text_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `text_content` (
  `filename` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `id_contribution` bigint(20) NOT NULL,
  `id_contributor` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`filename`),
  KEY `k_text_content_contributor1_idx` (`id_contributor`),
  KEY `k_text_content_contribution1_idx` (`id_contribution`),
  CONSTRAINT `fk_text_content_contributor1` FOREIGN KEY (`id_contributor`) REFERENCES `contributor` (`id_contributor`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_text_content_text1` FOREIGN KEY (`id_contribution`) REFERENCES `text` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `text_i18names`
--

DROP TABLE IF EXISTS `text_i18names`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `text_i18names` (
  `id_contribution` bigint(20) NOT NULL,
  `lang` char(2) COLLATE utf8_unicode_ci NOT NULL,
  `spelling` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_contribution`,`lang`),
  KEY `k_text_i18names_t_language1` (`lang`),
  FULLTEXT KEY `text_idx` (`spelling`),
  CONSTRAINT `fk_text_i18names_t_language1` FOREIGN KEY (`lang`) REFERENCES `t_language` (`code`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_text_i18names_text1` FOREIGN KEY (`id_contribution`) REFERENCES `text` (`id_contribution`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `text_source_name`
--

DROP TABLE IF EXISTS `text_source_name`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `text_source_name` (
  `id_source` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_source`),
  FULLTEXT KEY `name_idx` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=93 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `tmp_contributor`
--

DROP TABLE IF EXISTS `tmp_contributor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
 SET character_set_client = utf8mb4 ;
CREATE TABLE `tmp_contributor` (
  `id_tmp_contributor` bigint(20) NOT NULL AUTO_INCREMENT,
  `id_project` int(11) NOT NULL,
  `id_project_subgroup` int(11) NOT NULL,
  `pseudo` varchar(60) COLLATE utf8_unicode_ci NOT NULL,
  `password_hash` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id_tmp_contributor`),
  UNIQUE KEY `uq_tmp_contributor_pseudo` (`pseudo`),
  KEY `k_tmp_contributor_project1` (`id_project`),
  KEY `k_tmp_contributor_subgroup1` (`id_project_subgroup`),
  CONSTRAINT `fk_tmp_contributor_project1` FOREIGN KEY (`id_project`) REFERENCES `project` (`id_project`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `fk_tmp_contributor_subgroup1` FOREIGN KEY (`id_project_subgroup`) REFERENCES `project_subgroup` (`id_project_subgroup`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-04-14  9:54:17
