CREATE DATABASE  IF NOT EXISTS `used_market_test` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `used_market_test`;
-- MySQL dump 10.13  Distrib 8.0.29, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: used_market_test
-- ------------------------------------------------------
-- Server version	8.0.29

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `auction`
--

DROP TABLE IF EXISTS `auction`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auction` (
  `auction_id` bigint NOT NULL AUTO_INCREMENT,
  `auction_end_date` datetime(6) DEFAULT NULL,
  `auction_start_date` datetime(6) DEFAULT NULL,
  `buyer_trans_status` varchar(255) DEFAULT NULL,
  `now_price` int NOT NULL,
  `price_unit` int NOT NULL,
  `seller_trans_status` varchar(255) DEFAULT NULL,
  `start_price` int NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`auction_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auction`
--

--
-- Table structure for table `auction_history`
--

DROP TABLE IF EXISTS `auction_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `auction_history` (
  `auction_history_id` bigint NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `last_modified_date` datetime(6) DEFAULT NULL,
  `bid_price` int NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `auction_id` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`auction_history_id`),
  KEY `FK2qysrbsciru7tvmd967bm9n1h` (`auction_id`),
  KEY `FK11ytgic3oeelf4icunr1baun0` (`member_id`),
  CONSTRAINT `FK11ytgic3oeelf4icunr1baun0` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FK2qysrbsciru7tvmd967bm9n1h` FOREIGN KEY (`auction_id`) REFERENCES `auction` (`auction_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `auction_history`
--

LOCK TABLES `auction_history` WRITE;
/*!40000 ALTER TABLE `auction_history` DISABLE KEYS */;
/*!40000 ALTER TABLE `auction_history` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `authority`
--

DROP TABLE IF EXISTS `authority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `authority` (
  `authority_name` varchar(50) NOT NULL,
  PRIMARY KEY (`authority_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `authority`
--

LOCK TABLES `authority` WRITE;
/*!40000 ALTER TABLE `authority` DISABLE KEYS */;
INSERT INTO `authority` VALUES ('ROLE_USER');
/*!40000 ALTER TABLE `authority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=55 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'디지털기기'),(2,'생활가전'),(3,'가구/인테리어'),(4,'생활/주방'),(5,'유아동'),(6,'유아도서'),(7,'여성의류'),(8,'여성잡화'),(9,'도서'),(10,'가공식품'),(11,'반려동물용품'),(12,'식품'),(13,'기타'),(14,'남성패션/잡화'),(15,'뷰티/미용'),(16,'티켓/교환권'),(17,'스포츠/레저'),(18,'취미/게임/음반');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_message`
--

DROP TABLE IF EXISTS `chat_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `chat_message_id` bigint NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `last_modified_date` datetime(6) DEFAULT NULL,
  `message` varchar(255) DEFAULT NULL,
  `read_or_not` bit(1) NOT NULL,
  `chat_room_id` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`chat_message_id`),
  KEY `FKj52yap2xrm9u0721dct0tjor9` (`chat_room_id`),
  KEY `FKynfbnbqot8mpd1tquoc2s1w5` (`member_id`),
  CONSTRAINT `FKj52yap2xrm9u0721dct0tjor9` FOREIGN KEY (`chat_room_id`) REFERENCES `chat_room` (`chat_room_id`),
  CONSTRAINT `FKynfbnbqot8mpd1tquoc2s1w5` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_message`
--

LOCK TABLES `chat_message` WRITE;
/*!40000 ALTER TABLE `chat_message` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_room`
--

DROP TABLE IF EXISTS `chat_room`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_room` (
  `chat_room_id` bigint NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `last_modified_date` datetime(6) DEFAULT NULL,
  `user_count` int NOT NULL,
  `member_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`chat_room_id`),
  KEY `FK6x6htd2o9ba2r2wcrs72qau17` (`member_id`),
  KEY `FK7onsxwdywfy83utrolrgxuy0` (`product_id`),
  CONSTRAINT `FK6x6htd2o9ba2r2wcrs72qau17` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FK7onsxwdywfy83utrolrgxuy0` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_room`
--

LOCK TABLES `chat_room` WRITE;
/*!40000 ALTER TABLE `chat_room` DISABLE KEYS */;
/*!40000 ALTER TABLE `chat_room` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `file`
--

DROP TABLE IF EXISTS `file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `file` (
  `dtype` varchar(31) NOT NULL,
  `file_id` bigint NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `last_modified_date` datetime(6) DEFAULT NULL,
  `full_path` varchar(255) DEFAULT NULL,
  `original_name` varchar(255) DEFAULT NULL,
  `path` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`file_id`),
  KEY `FKl4cec30ecbepecueqgyan5olo` (`product_id`),
  CONSTRAINT `FKl4cec30ecbepecueqgyan5olo` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `file`
--

LOCK TABLES `file` WRITE;
/*!40000 ALTER TABLE `file` DISABLE KEYS */;
/*!40000 ALTER TABLE `file` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `last_modified_date` datetime(6) DEFAULT NULL,
  `birth` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `login_id` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `status` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,'2023-04-20 23:46:34.658143','2023-04-20 23:46:34.658143','990428','addd@naver.com','20180584','호창','1234','010-5444-8888','EXIST'),(2,'2023-04-20 23:46:34.669140','2023-04-20 23:46:34.669140','990228','addd333@naver.com','20180012','광민','133234','010-5944-8288','EXIST'),(3,'2023-04-20 23:46:34.672143','2023-04-20 23:46:34.672143','990118','addsdfd333@naver.com','20180592','시철','1332234','010-4944-8288','EXIST'),(4,'2023-04-20 23:46:34.676148','2023-04-20 23:46:34.676148','990228','addd333@naver.com','20180004','대현','1323234','010-5944-8288','EXIST'),(5,'2023-04-20 23:46:34.679137','2023-04-20 23:46:34.679137','990118','addsdfd333@naver.com','20180211','성수','13322334','010-4244-8288','EXIST');
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member_authority`
--

DROP TABLE IF EXISTS `member_authority`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member_authority` (
  `member_id` bigint NOT NULL,
  `authority_name` varchar(50) NOT NULL,
  PRIMARY KEY (`member_id`,`authority_name`),
  KEY `FKasnmjar8jr5gaxvd7966p19ir` (`authority_name`),
  CONSTRAINT `FK8uf5ff5jr0nuvbj4yfp5ob9sq` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKasnmjar8jr5gaxvd7966p19ir` FOREIGN KEY (`authority_name`) REFERENCES `authority` (`authority_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member_authority`
--

LOCK TABLES `member_authority` WRITE;
/*!40000 ALTER TABLE `member_authority` DISABLE KEYS */;
INSERT INTO `member_authority` VALUES (1,'ROLE_USER'),(2,'ROLE_USER'),(3,'ROLE_USER'),(4,'ROLE_USER'),(5,'ROLE_USER');
/*!40000 ALTER TABLE `member_authority` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `product_id` bigint NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `last_modified_date` datetime(6) DEFAULT NULL,
  `info` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `product_status` varchar(255) DEFAULT NULL,
  `view_count` int NOT NULL,
  `auction_id` bigint DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  PRIMARY KEY (`product_id`),
  KEY `FKk15i3nqna4gw1bx9g7sdd3htd` (`auction_id`),
  KEY `FK1mtsbur82frn64de7balymq9s` (`category_id`),
  KEY `FKnlsnjukov4vm4dv1dw0l438hi` (`member_id`),
  CONSTRAINT `FK1mtsbur82frn64de7balymq9s` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`),
  CONSTRAINT `FKk15i3nqna4gw1bx9g7sdd3htd` FOREIGN KEY (`auction_id`) REFERENCES `auction` (`auction_id`),
  CONSTRAINT `FKnlsnjukov4vm4dv1dw0l438hi` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--


--
-- Table structure for table `question`
--

DROP TABLE IF EXISTS `question`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `question` (
  `question_id` bigint NOT NULL AUTO_INCREMENT,
  `created_date` datetime(6) DEFAULT NULL,
  `last_modified_date` datetime(6) DEFAULT NULL,
  `created_by` varchar(255) DEFAULT NULL,
  `last_modified_by` varchar(255) DEFAULT NULL,
  `content` varchar(255) DEFAULT NULL,
  `layer` int NOT NULL,
  `status` varchar(255) DEFAULT NULL,
  `member_id` bigint DEFAULT NULL,
  `parent_id` bigint DEFAULT NULL,
  `product_id` bigint DEFAULT NULL,
  PRIMARY KEY (`question_id`),
  KEY `FK1nuuke7olg7b9fkyp2ba9d5bx` (`member_id`),
  KEY `FKbul0jq6tresx05cfbc73ykwf4` (`parent_id`),
  KEY `FKr27h1qc056hx8lc60nvgyhw64` (`product_id`),
  CONSTRAINT `FK1nuuke7olg7b9fkyp2ba9d5bx` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `FKbul0jq6tresx05cfbc73ykwf4` FOREIGN KEY (`parent_id`) REFERENCES `question` (`question_id`),
  CONSTRAINT `FKr27h1qc056hx8lc60nvgyhw64` FOREIGN KEY (`product_id`) REFERENCES `product` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `question`
--

LOCK TABLES `question` WRITE;
/*!40000 ALTER TABLE `question` DISABLE KEYS */;
/*!40000 ALTER TABLE `question` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2023-04-21  0:35:29
