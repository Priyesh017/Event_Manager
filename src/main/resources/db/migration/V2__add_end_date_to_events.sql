-- V2__add_end_date_to_events.sql
-- Add end_date column to support event duration and status tracking

ALTER TABLE events ADD COLUMN IF NOT EXISTS end_date TIMESTAMP;

-- Backfill: for existing events set end_date = event_date + 2 hours
UPDATE events SET end_date = event_date + INTERVAL '2 hours' WHERE end_date IS NULL;

-- Make end_date NOT NULL now that it is filled
ALTER TABLE events ALTER COLUMN end_date SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_events_end_date ON events(end_date);
