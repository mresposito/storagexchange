# --- !Ups

CREATE TABLE Location (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(70) NOT NULL,
  lat DECIMAL(9,6) NOT NULL,
  lng DECIMAL(9,6) NOT NULL,
  city VARCHAR(70) NOT NULL,
  state VARCHAR(70) NOT NULL,
  address VARCHAR(200) NOT NULL,
  zip VARCHAR(32) NOT NULL);
  
CREATE TABLE University (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  locationID BIGINT NOT NULL,
  name VARCHAR(80) NOT NULL UNIQUE,
  website VARCHAR(2083) NOT NULL,
  logo VARCHAR(400) NOT NULL,
  colors VARCHAR(200) NOT NULL,
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
  startDate BIGINT,
  endDate BIGINT,
  approved BOOLEAN NOT NULL DEFAULT 0,
  canceled BOOLEAN  NOT NULL DEFAULT 0,
  timestamp TIMESTAMP);

CREATE TABLE Rating (
  ratingID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  transactionID BIGINT NOT NULL UNIQUE,
  raterEmail VARCHAR(50) NOT NULL,
  rateeEmail VARCHAR(50) NOT NULL,
  score INT NOT NULL,
)

# --- !Downs
DROP TABLE Post;
DROP TABLE University;
DROP TABLE Location;
DROP TABLE Message;
