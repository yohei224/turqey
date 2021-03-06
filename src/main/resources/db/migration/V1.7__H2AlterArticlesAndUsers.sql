ALTER TABLE ARTICLES ADD COLUMN (
  PUBLISHED  BOOLEAN NOT NULL DEFAULT FALSE
);

ALTER TABLE USERS DROP COLUMN IMG_URL; 
CREATE INDEX IDX_USERS2 ON USERS (LOGIN_ID);

CREATE TABLE DRAFTS (
  ID          bigint auto_increment primary key,
  ARTICLE_ID  bigint NOT NULL,
  TITLE       varchar(255) NOT NULL,
  CONTENT     CLOB NOT NULL,
  OWNER_ID    bigint NOT NULL,
  CREATED     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IDX_DRAFTS1 ON DRAFTS (ARTICLE_ID);

DROP TABLE ARTICLE_HISTORIES;
CREATE TABLE ARTICLE_HISTORIES (
  ID          bigint auto_increment primary key,
  ARTICLE_ID  bigint NOT NULL,
  USER_ID     bigint,
  COMMIT_ID   char(40),
  CREATED     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IDX_ARTICLE_HISTORIES1 ON ARTICLE_HISTORIES (ARTICLE_ID);


