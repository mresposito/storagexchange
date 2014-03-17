# --- !Ups

CREATE TABLE Post (
  postID BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  email email VARCHAR(50) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  storageSize INT NOT NULL);

# --- !Downs
DROP TABLE Post;