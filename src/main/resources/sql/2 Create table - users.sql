DROP TABLE IF EXISTS tweagle.users;
CREATE TABLE tweagle.users (

  u_name              VARCHAR(25) NOT NULL,
  password            VARCHAR(15) NOT NULL,
  email               VARCHAR(45) NOT NULL,
  u_male              BOOLEAN     NOT NULL,
  creation_date       DATE        NOT NULL,

  messages            INT         NOT NULL DEFAULT 0,
  following           INT         NOT NULL DEFAULT 0,
  followers           INT         NOT NULL DEFAULT 0,

  consumer_key        VARCHAR(70) NOT NULL DEFAULT '',
  consumer_secret     VARCHAR(70) NOT NULL DEFAULT '',
  access_token        VARCHAR(70) NOT NULL DEFAULT '',
  access_token_secret VARCHAR(70) NOT NULL DEFAULT '',
  PRIMARY KEY (u_name)
);