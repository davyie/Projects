-- 1. Create the parent 'schedules' table
CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL
);

-- 2. Create the child 'time_slots' table to handle the List<TimeSlotEntity>
-- I've assumed standard fields for a TimeSlot (id, start_time, end_time, date)
CREATE TABLE IF NOT EXISTS time_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL, -- The foreign key for the relationship
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT fk_schedule
        FOREIGN KEY (schedule_id)
        REFERENCES schedules(id)
        ON DELETE CASCADE
);