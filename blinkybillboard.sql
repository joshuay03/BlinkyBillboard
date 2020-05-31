-- MariaDB dump 10.17  Distrib 10.4.12-MariaDB, for Linux (x86_64)
--
-- Host: localhost
-- ------------------------------------------------------
-- Server version	10.4.12-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `Billboards`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
create TABLE IF NOT EXISTS `Billboards` (
  `billboard_name` varchar(100) NOT NULL,
  `creator` varchar(100) NULL COMMENT 'User ID of the billboard''s creator',
  `backgroundColour` int(11) DEFAULT NULL,
  `messageColour` int(11) DEFAULT NULL,
  `informationColour` int(11) DEFAULT NULL,
  `message` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `information` text COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `billboardImage` MEDIUMBLOB DEFAULT NULL,
  PRIMARY KEY (`billboard_name`),
  KEY `fk_creator_idx` (`creator`),
  CONSTRAINT `Billboards_FK` FOREIGN KEY (`creator`) REFERENCES `Users` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Scheduling`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
create TABLE IF NOT EXISTS `Scheduling` (
  `schedule_id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT 'The ID is per billboard, per viewer',
  `billboard_name` varchar(100) NOT NULL,
  `viewer_id` int(11) unsigned NOT NULL,
  `start_time` timestamp NOT NULL,
  `end_time` timestamp NULL,
  `duration` int(11) unsigned NOT NULL COMMENT 'How long will one instance of this billboard be up for?',
  `interval` int(11) unsigned NOT NULL DEFAULT 0 COMMENT 'How often should the billboard repeat itself in minutes? (must be more than the duration )',
  `scheduled_at` timestamp DEFAULT NOW() NOT NULL COMMENT 'When was the billboard created?',
  PRIMARY KEY (`schedule_id`,`billboard_name`,`viewer_id`),
  KEY `billboard_name_idx` (`billboard_name`),
  KEY `viewer_id_idx` (`viewer_id`),
  CONSTRAINT `Scheduling_FK` FOREIGN KEY (`viewer_id`) REFERENCES `Viewers` (`viewer_id`),
  CONSTRAINT `billboard_name` FOREIGN KEY (`billboard_name`) REFERENCES `Billboards` (`billboard_name`) ON delete RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `TokenBlacklist`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
create TABLE IF NOT EXISTS `TokenBlacklist` (
  `tokenCode` binary(128) NOT NULL,
  `expiry` timestamp NOT NULL,
  PRIMARY KEY (`tokenCode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Users`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
create TABLE IF NOT EXISTS `Users` (
  `user_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_permissions` varchar(5) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password_hash` binary(32) NOT NULL,
  `salt` binary(100) NOT NULL,
  PRIMARY KEY (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `Viewers`
--

/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
create TABLE IF NOT EXISTS `Viewers` (
  `viewer_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `socket` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'IP + port of viewer',
  PRIMARY KEY (`viewer_id`),
  UNIQUE KEY `viewer_id_UNIQUE` (`viewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2020-05-30  1:02:45
