/* Tipos de tarjetas */
INSERT INTO card_type(id, public_id, name, description, created_at, updated_at, is_deleted)
VALUES ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'BgKVWAzSSYcddqW8rRd', 'TARJETA DE CRÉDITO', 'Descripción de tarjeta de crédito', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO card_type(id, public_id, name, description, created_at, updated_at, is_deleted)
VALUES ('9b0e9e1a-1f40-11ee-be56-0242ac120002', 'XkJVaMzSSYhddrL8rTd', 'TARJETA DE DÉBITO', 'Descripción de tarjeta de débito', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO card_type(id, public_id, name, description, created_at, updated_at, is_deleted)
VALUES ('3fa85f64-5717-4562-b3fc-2c963f66afa6', 'ZyKVVAzSSYzddqK8rPd', 'TARJETA CORPORATIVA', 'Descripción tarjeta corporativa', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO card_type(id, public_id, name, description, created_at, updated_at, is_deleted)
VALUES ('d2886a1d-7b10-4f24-9a31-582542db0a4a', 'VqGVMAzSSYxddlJ9sTd', 'TARJETA DE REGALO', 'Descripción tarjeta de regalo', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO card_type(id, public_id, name, description, created_at, updated_at, is_deleted)
VALUES ('6c3a4e1c-9f01-4d89-9b37-dc9b3e277e90', 'WpGVNAzSSYuddnL6rAd', 'TARJETA VIRTUAL', 'Descripción tarjeta virtual', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

/* Tipos de cuentas */
INSERT INTO account_type(id, public_id, name, description, interest, created_at, updated_at, is_deleted)
VALUES ('1e4b41d8-9d4c-4321-ade7-3c8a843626e2', 'LmHVMAzSSYbddoR3qBd', 'CUENTA JUVENIL', 'Descripción cuenta juvenil', 0.5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO account_type(id, public_id, name, description, interest, created_at, updated_at, is_deleted)
VALUES ('c56a4180-65aa-42ec-a945-5fd21dec0538', 'NqHVMAzSSYcddpT2qAd', 'CUENTA EMPRESARIAL', 'Descripción cuenta empresarial', 2.0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO account_type(id, public_id, name, description, interest, created_at, updated_at, is_deleted)
VALUES ('e02fa0e4-01ad-400f-9c98-68cd3f4d3a3f', 'OkIVMAzSSYeddqV4rCd', 'CUENTA VIP', 'Descripción cuenta VIP', 3.0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO account_type(id, public_id, name, description, interest, created_at, updated_at, is_deleted)
VALUES ('8c698f22-3ee4-11ee-89da-0242ac120002', 'PkJVMAzSSYgddnU5rEd', 'CUENTA FAMILIAR', 'Descripción cuenta familiar', 1.0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO account_type(id, public_id, name, description, interest, created_at, updated_at, is_deleted)
VALUES ('64c283f0-bb2e-4c8f-bd2d-3b60c860ef79', 'QkKVVAzSSYhddoW6sFd', 'CUENTA ESTUDIANTIL', 'Descripción cuenta estudiantil', 0.75, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);
