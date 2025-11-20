CREATE TABLE booking.time_slots (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL REFERENCES booking.rooms(id) ON DELETE CASCADE,
    start_time TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    end_time TIMESTAMP(0) WITHOUT TIME ZONE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER time_slots_trg_set_updated_at
BEFORE UPDATE ON booking.time_slots
FOR EACH ROW
EXECUTE FUNCTION public.set_updated_at();

-- No overlaps
CREATE EXTENSION IF NOT EXISTS btree_gist;
ALTER TABLE booking.time_slots
  ADD CONSTRAINT time_slots_ex_slots_no_overlap
  EXCLUDE USING gist (
    room_id WITH =,
    tsrange(start_time, end_time, '[)') WITH &&
  );
