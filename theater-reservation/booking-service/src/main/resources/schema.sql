CREATE TABLE IF NOT EXISTS timeslots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    slot_date DATE NOT NULL,
    start_time DATETIME(6), -- (6) preserves fractional seconds for Instant
    end_time DATETIME(6),
    duration BIGINT
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT PRIMARY KEY,
    user_id BIGINT,
    time_slot_id BIGINT,
    movie_id BIGINT,
    description VARCHAR(255),
    -- Define the constraint separately at the end
    CONSTRAINT fk_timeslot FOREIGN KEY (time_slot_id) REFERENCES timeslots(id)
);

