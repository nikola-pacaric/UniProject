USE library_db;

CREATE TABLE IF NOT EXISTS author (
	id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    biography TEXT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS category (
	id BIGINT NOT NULL AUTO_INCREMENT,
	name VARCHAR(100) NOT NULL,
    description TEXT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_category_name (name)
);

CREATE TABLE IF NOT EXISTS member (
	id BIGINT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    membership_card_number VARCHAR(50) NOT NULL,
    email VARCHAR(150) NOT NULL,
    phone VARCHAR(50) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_card (membership_card_number)
);

CREATE TABLE IF NOT EXISTS librarian (
	id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_librarian_username (username),
    UNIQUE KEY uk_librarian_email (email)
);

CREATE TABLE IF NOT EXISTS book (
	id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    isbn VARCHAR(20) NOT NULL,
    publication_year INT NOT NULL,
    total_copies INT NOT NULL,
    available_copies INT NOT NULL,
    author_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_isbn (isbn),
    CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES author (id),
    CONSTRAINT fk_book_category FOREIGN KEY (category_id) REFERENCES category (id)
);

CREATE TABLE IF NOT EXISTS loan (
	id BIGINT NOT NULL AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    loan_date DATE NOT NULL,
    due_date DATE NOT NULL,
    return_date DATE NULL,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_loan_book FOREIGN KEY (book_id) REFERENCES book (id),
    CONSTRAINT fk_loan_member FOREIGN KEY (member_id) REFERENCES member (id)
);

INSERT INTO author (first_name, last_name, biography)
	VALUES ('Ivo', 'Andric', NULL);
INSERT INTO category (name, description)
	VALUES ('Roman', 'Proza duze forme.');