DROP TABLE IF EXISTS book_api;

CREATE TABLE IF NOT EXISTS book_api (
  id   INT          NOT NULL AUTO_INCREMENT,
  name VARCHAR(128) NOT NULL,
  url  VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS book;

CREATE TABLE IF NOT EXISTS book (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  book_api_id INT          NOT NULL,
  item_id     VARCHAR(32)  NOT NULL,
  title       VARCHAR(255) NOT NULL,
  author      VARCHAR(255) NOT NULL,
  publisher   VARCHAR(255) NOT NULL,
  page_num    INT          NOT NULL,
  price       INT          NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT ui_book_01 UNIQUE (book_api_id, item_id),
  CONSTRAINT fk_book_01 FOREIGN KEY (book_api_id) REFERENCES book_api (id)
);

DROP TABLE IF EXISTS isbn;

CREATE TABLE IF NOT EXISTS isbn (
  book_id BIGINT      NOT NULL,
  isbn13  VARCHAR(13) NOT NULL,
  PRIMARY KEY (book_id),
  CONSTRAINT fk_isbn_01 FOREIGN KEY (book_id) REFERENCES book (id)
);

DROP TABLE IF EXISTS book_cover;

CREATE TABLE IF NOT EXISTS book_cover (
  book_id   BIGINT       NOT NULL,
  image_url VARCHAR(255) NOT NULL,
  PRIMARY KEY (book_id),
  CONSTRAINT fk_book_cover_01 FOREIGN KEY (book_id) REFERENCES book (id)
);
