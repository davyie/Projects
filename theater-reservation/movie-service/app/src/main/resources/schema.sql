CREATE TABLE IF NOT EXISTS movies (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    screen_time BIGINT,
    description VARCHAR(1000), -- Increased length for movie descriptions
    PRIMARY KEY (id)
);