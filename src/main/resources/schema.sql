DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS item_request;
DROP TABLE IF EXISTS item;
DROP TABLE IF EXISTS booking;
DROP TABLE IF EXISTS comments;

CREATE TABLE IF NOT EXISTS users
(
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    user_name VARCHAR(30) NOT NULL,
    email VARCHAR(30) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS item_request
(
    item_request_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    item_request_description VARCHAR(128) NOT NULL,
    creation_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_item_request PRIMARY KEY (item_request_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS item
(
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    owner_id INT,
    item_name VARCHAR(30) NOT NULL,
    item_description VARCHAR(128),
    is_available BOOLEAN DEFAULT FALSE,
    request_id INT,
    CONSTRAINT pk_item PRIMARY KEY (item_id),
    FOREIGN KEY (owner_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (request_id) REFERENCES item_request (item_request_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking
(
    booking_id INT PRIMARY KEY AUTO_INCREMENT,
    start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    item_id INT,
    booker_id INT,
    status VARCHAR(8) NOT NULL DEFAULT 'WAITING',
    CONSTRAINT pk_booking PRIMARY KEY (booking_id),
    FOREIGN KEY (item_id) REFERENCES item (item_id) ON DELETE CASCADE,
    FOREIGN KEY (booker_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments
(
    comment_id INT PRIMARY KEY AUTO_INCREMENT,
    text TEXT(560) NOT NULL,
    item_id INT,
    author_id INT,
    created TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_comments PRIMARY KEY (comment_id),
    FOREIGN KEY (item_id) REFERENCES item (item_id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users (user_id) ON DELETE CASCADE
);