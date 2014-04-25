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

# --- !Downs
DROP TABLE Post;
DROP TABLE University;
DROP TABLE Location;
