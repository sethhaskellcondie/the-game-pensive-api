CREATE TABLE IF NOT EXISTS custom_fields (
    id SERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    type VARCHAR NOT NULL, -- This will be an enum enforced by the Repository
    entity_key VARCHAR NOT NULL,
    deleted BOOLEAN default false
);

CREATE TABLE IF NOT EXISTS custom_field_values (
    custom_field_id INTEGER REFERENCES  custom_fields (id),
    entity_id INTEGER NOT NULL, -- This will be a foreign key enforced by the Repository
    entity_key VARCHAR NOT NULL,
    value_text VARCHAR,
    value_number BIGINT
);

-- Undo
-- DROP TABLE custom_fields;
-- DROP TABLE custom_field_values;
-- DELETE FROM flyway_schema_history WHERE version = '1.3';