# --- !Ups

CREATE TABLE Transactions (
  storageTaken INT NOT NULL,
  fromDate TIMESTAMP NOT NULL,
  toDate TIMESTAMP NOT NULL,
  timestamp TIMESTAMP NOT NULL,
  postingId BIGINT NOT NULL,
  buyerUserId BIGINT NOT NULL, 
  fromUserId BIGINT NOT NULL, 
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY);

CREATE TABLE User (
  userID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  surname VARCHAR(50) NOT NULL,
  email VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(200) NOT NULL,
  verifiedEmail SMALLINT DEFAULT 0,
  thumbnail VARCHAR(250),
  universityID BIGINT NOT NULL,
  creationTime BIGINT NOT NULL,
  lastLogin BIGINT);

CREATE VIEW VerifiedUser AS
  SELECT * 
  FROM User
  WHERE verifiedEmail = 1;

# --- !Downs

DROP TABLE User;
DROP VIEW VerifiedUser;
DROP TABLE Transactions;
