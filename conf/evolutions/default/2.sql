# --- !Ups

CREATE TABLE Location (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,
  name VARCHAR NOT NULL,
  lat DECIMAL(9,6) NOT NULL,
  lng DECIMAL(9,6) NOT NULL,
  city VARCHAR(70) NOT NULL,
  state VARCHAR(70) NOT NULL,
  address VARCHAR NOT NULL,
  zip VARCHAR(32) NOT NULL);
  
CREATE TABLE University (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY(START WITH 1) NOT NULL PRIMARY KEY,
  locationID BIGINT NOT NULL,
  name VARCHAR NOT NULL UNIQUE,
  website VARCHAR(2083) NOT NULL,
  logo VARCHAR NOT NULL,
  colors VARCHAR NOT NULL,
  FOREIGN KEY(locationID) REFERENCES Location(id) ON DELETE CASCADE);

CREATE TABLE Post (
  postID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email VARCHAR(50) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  locationID BIGINT NOT NULL,
  storageSize INT NOT NULL,
  FOREIGN KEY(locationID) REFERENCES Location(id) ON DELETE CASCADE);

CREATE TABLE Message (
  fromUser VARCHAR(50) NOT NULL,
  toUser VARCHAR(50) NOT NULL,
  message VARCHAR(2000) NOT NULL,
  parentID BIGINT,
  childID BIGINT,
  messageID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY
);

CREATE TABLE Transaction (
  transactionID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  postID BIGINT NOT NULL,
  buyerEmail VARCHAR(50) NOT NULL,
  sellerEmail VARCHAR(50) NOT NULL,
  storageTaken INT NOT NULL,
  startDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  endDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  approved BOOLEAN NOT NULL DEFAULT 0,
  canceled INT  NOT NULL DEFAULT 0,
  timestamp TIMESTAMP);

# --- !Downs
DROP TABLE Post;
DROP TABLE Message;
DROP TABLE University;
DROP TABLE Location;
