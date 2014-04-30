# --- !Ups

CREATE TABLE Rating (
  ratingID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  transactionID BIGINT NOT NULL UNIQUE,
  raterEmail VARCHAR(50) NOT NULL,
  rateeEmail VARCHAR(50) NOT NULL,
  score INT NOT NULL);

# --- !Downs
DROP TABLE Rating;
