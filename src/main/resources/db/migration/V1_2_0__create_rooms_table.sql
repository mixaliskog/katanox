
CREATE SEQUENCE rooms_seq;

CREATE TABLE rooms (
  id bigint check (id > 0) NOT NULL DEFAULT NEXTVAL ('rooms_seq'),
  hotel_id bigint check (hotel_id > 0) NOT NULL,
  name text NOT NULL DEFAULT '',
  description text  NOT NULL,
  number_of_this_type int NOT NULL DEFAULT '1',
  max_adults int NOT NULL DEFAULT '2',
  max_children int NOT NULL DEFAULT '0',
  max_occupancy int NOT NULL DEFAULT '2',
  PRIMARY KEY (id),
  CONSTRAINT room_hotel_id_foreign FOREIGN KEY (hotel_id) REFERENCES hotels (id) ON DELETE CASCADE
)   ;

CREATE INDEX room_hotel_id_foreign ON rooms (hotel_id);

INSERT INTO rooms
(hotel_id, name, description, number_of_this_type, max_adults, max_children, max_occupancy) VALUES
(1, 'Room 1', 'Main room of amsterdam hotel', 4, 2, 2, 2),
(1, 'Room 2', 'Main room of amsterdam hotel', 2, 4, 4, 4),
(2, 'Room 1', 'Main room of berlin hotel', 4, 2, 2, 2),
(2, 'Room 1', 'Main room of berlin hotel', 2, 4, 4, 4);

