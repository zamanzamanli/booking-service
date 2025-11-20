CREATE TABLE booking.rooms (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TRIGGER rooms_trg_set_updated_at
BEFORE UPDATE ON booking.rooms
FOR EACH ROW
EXECUTE FUNCTION public.set_updated_at();
