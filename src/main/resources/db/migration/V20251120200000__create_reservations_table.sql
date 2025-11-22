CREATE TABLE booking.reservations (
  id          BIGSERIAL PRIMARY KEY,
  user_id     UUID NOT NULL,
  time_slot_id BIGINT NOT NULL REFERENCES booking.time_slots(id) ON DELETE RESTRICT,
  status      TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TRIGGER reservations_trg_set_updated_at
BEFORE UPDATE ON booking.reservations
FOR EACH ROW
EXECUTE FUNCTION public.set_updated_at();

-- One active reservation (HOLD or CONFIRMED) per slot
CREATE UNIQUE INDEX uniq_active_res_per_slot
  ON booking.reservations(time_slot_id)
  WHERE status IN ('HOLD','CONFIRMED');
