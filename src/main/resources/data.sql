ALTER TABLE clients
    DROP CONSTRAINT FKTIUQDLEDQ2LYBRDS2K3RFQRV4;

ALTER TABLE clients
    ADD CONSTRAINT FKTIUQDLEDQ2LYBRDS2K3RFQRV4
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;



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
VALUES ('c56a4180-65aa-42ec-a945-5fd21dec0538', 'NqHVMAzSSYcddpT2qAd', 'CUENTA EMPRESARIAL', 'Descripción cuenta empresarial', 2, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO account_type(id, public_id, name, description, interest, created_at, updated_at, is_deleted)
VALUES ('e02fa0e4-01ad-400f-9c98-68cd3f4d3a3f', 'OkIVMAzSSYeddqV4rCd', 'CUENTA VIP', 'Descripción cuenta VIP', 3, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO account_type(id, public_id, name, description, interest, created_at, updated_at, is_deleted)
VALUES ('8c698f22-3ee4-11ee-89da-0242ac120002', 'PkJVMAzSSYgddnU5rEd', 'CUENTA FAMILIAR', 'Descripción cuenta familiar', 1, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO account_type(id, public_id, name, description, interest, created_at, updated_at, is_deleted)
VALUES ('64c283f0-bb2e-4c8f-bd2d-3b60c860ef79', 'QkKVVAzSSYhddoW6sFd', 'CUENTA ESTUDIANTIL', 'Descripción cuenta estudiantil', 0.75, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

/*Usuarios*/
INSERT INTO users(id, public_id, username, password, is_deleted, created_at, updated_at)
VALUES('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'BgLL3psMG54VmML6ilx', 'admin', 'admin', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO user_roles(user_id, roles)VALUES ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'ADMIN');

INSERT INTO users(id, public_id, username, password, is_deleted, created_at, updated_at)
VALUES('9b0e9e1a-1f40-11ee-be56-0242ac120002', 'BgLL4KEb3KwG3H_zDiP', 'usertest', 'usertest', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO user_roles(user_id, roles)VALUES ('9b0e9e1a-1f40-11ee-be56-0242ac120002', 'USER');

INSERT INTO users(id, public_id, username, password, is_deleted, created_at, updated_at)
VALUES('3fa85f64-5717-4562-b3fc-2c963f66afa6', 'BgLL4KEb0KwG3H_zDiP', 'pepeuno', 'pepeuno', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO user_roles(user_id, roles)VALUES ('3fa85f64-5717-4562-b3fc-2c963f66afa6', 'USER');

INSERT INTO users(id, public_id, username, password, is_deleted, created_at, updated_at)
VALUES('d2886a1d-7b10-4f24-9a31-582542db0a4a', 'BgLL4KEb1KwG3H_zDiP', 'pepedos', 'pepedos', false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO user_roles(user_id, roles)VALUES ('d2886a1d-7b10-4f24-9a31-582542db0a4a', 'USER');

/* Clientes */
INSERT INTO clients(id, public_id, dni, complete_name, street, house_number, city, country , email, phone_number, photo, dni_picture, user_id, validated, is_deleted, created_at, updated_at)
VALUES('fb1ef73c-4c7e-44b4-b5d9-9135dc40b4fd', 'BgKW5Gx_kUAFvNX8dd2', '87654321B', 'Cliente test', 'calle falsa', '123', 'SPRINGFIELD', 'ESTADOS UNIDOS','test@test.com', 654123789, null, null,'9b0e9e1a-1f40-11ee-be56-0242ac120002', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO clients(id, public_id, dni, complete_name, street, house_number, city, country , email, phone_number, photo, dni_picture,user_id, validated, is_deleted, created_at, updated_at)
VALUES('11b12967-5ffd-455a-aa35-65172f9b86fe', 'BgKW5qHKll4b2bcmCkV', '87654321X', 'Pepe uno', 'calle leganes', '123', 'LEGANES', 'ESPAÑA','pepeuno@test.com', 654123788, null, null, '3fa85f64-5717-4562-b3fc-2c963f66afa6', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO clients(id, public_id, dni, complete_name, street, house_number, city, country , email, phone_number, photo, dni_picture,user_id, validated, is_deleted, created_at, updated_at)
VALUES('f885418f-e1c0-4476-bd0e-acf1f65dbda2', 'BgKW6LJLOuU92YGkVkQ', '45678912L', 'Pepe dos', 'calle getafe', '123', 'GETAFE', 'ESPAÑA','pepedos@test.com', 654123700, null, null, 'd2886a1d-7b10-4f24-9a31-582542db0a4a', true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

/* Cuentas */
INSERT INTO accounts(id, public_id, iban, balance, password, account_type, client_id, created_at, updated_at, is_deleted)
VALUES ('96257eff-0484-4a87-9509-51a9f24daf64', 'BgLi9HNRv_UnWaC0Kkw', 'ES71CU4N1759HNCGUNQSNXM6', 1000.0, 'JAKARTA24', '1e4b41d8-9d4c-4321-ade7-3c8a843626e2','fb1ef73c-4c7e-44b4-b5d9-9135dc40b4fd', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

INSERT INTO accounts(id, public_id, iban, balance, password, account_type, client_id, created_at, updated_at, is_deleted)
VALUES ('a6f20974-a880-46b6-8b6d-069694eeaf60', 'BgLi9quF_BMyN0F8Nxs', 'ES51W8N4JVONAWK5B6ZA50OV', 10.0, 'JAKARTA26', '1e4b41d8-9d4c-4321-ade7-3c8a843626e2','11b12967-5ffd-455a-aa35-65172f9b86fe',  CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(),false);

/* Tarjetas */
INSERT INTO cards(id, public_id, card_owner, card_number, expiration_date, cvv, pin, card_type_id, account_id, spent_today, spent_this_week, spent_this_month, daily_limit, weekly_limit, monthly_limit, is_inactive, created_at, updated_at, is_deleted)
VALUES ('f47ac10b-58cc-4372-a567-0e02b2c3d479', 'BgLi9quF_BMyN0F8Nxs','Cliente test', '1234567890123456', '12/25', 123, '1234', 'f47ac10b-58cc-4372-a567-0e02b2c3d479', '96257eff-0484-4a87-9509-51a9f24daf64', 0, 0, 0, 1000, 5000, 10000, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);
INSERT INTO cards(id, public_id, card_number, card_owner, expiration_date, cvv, pin, card_type_id, account_id, spent_today, spent_this_week, spent_this_month, daily_limit, weekly_limit, monthly_limit, is_inactive, created_at, updated_at, is_deleted)
VALUES ('9b0e9e1a-1f40-11ee-be56-0242ac120002', 'BgLi9quF_BMyN0F8Nxa', '1234567890123457','Pepe uno', '12/25', 123, '1234', '9b0e9e1a-1f40-11ee-be56-0242ac120002', 'a6f20974-a880-46b6-8b6d-069694eeaf60', 0, 0, 0, 500, 1000, 4500, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);

