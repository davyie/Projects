CREATE TABLE events (
    event_id UUID PRIMARY KEY,
      aggregate_id UUID NOT NULL,
      aggregate_type TEXT NOT NULL,
      event_type TEXT NOT NULL,
      event_version INT NOT NULL,
      sequence_number INT NOT NULL,
      payload JSONB NOT NULL,
      metadata JSONB NOT NULL DEFAULT '{}',
      created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
      global_seq BIGSERIAL,
      UNIQUE (aggregate_id, sequence_number)
  );
CREATE INDEX idx_events_aggregate ON events (aggregate_id, sequence_number);
CREATE INDEX idx_events_global_seq ON events (global_seq);