INSERT INTO products(id, name, product_type, description, interest, created_at, updated_at, is_deleted)
VALUES ('1695ce31-d70a-48d9-af8b-90dea3d1e664','CUENTA CORRIENTE', 'CUENTA BANCARIA', 'descripcion cuenta corriente', 0.0, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);
INSERT INTO products(id, name, product_type, description, interest, created_at, updated_at, is_deleted)
VALUES ('5f9e55c2-4053-4351-8666-5c3692efdac9','CUENTA AHORRO', 'CUENTA BANCARIA', 'descripcion cuenta bancaria', 1.5, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), false);



INSERT INTO clients(id, id_path, dni, complete_name, street, house_number, city, country , email, phone_number, photo, dni_picture, validated, is_deleted, created_at, updated_at)
VALUES('fb1ef73c-4c7e-44b4-b5d9-9135dc40b4fd', null, '87654321B', 'Cliente test', 'calle falsa', '123', 'SPRINGFIELD', 'ESTADOS UNIDOS','test@test.com', 654123789, null, null, true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO clients(id, id_path, dni, complete_name, street, house_number, city, country , email, phone_number, photo, dni_picture, validated, is_deleted, created_at, updated_at)
VALUES('11b12967-5ffd-455a-aa35-65172f9b86fe', null, '87654321X', 'Pepe uno', 'calle leganes', '123', 'LEGANES', 'ESPAÑA','pepeuno@test.com', 654123788, null, null, true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
INSERT INTO clients(id, id_path, dni, complete_name, street, house_number, city, country , email, phone_number, photo, dni_picture, validated, is_deleted, created_at, updated_at)
VALUES('f885418f-e1c0-4476-bd0e-acf1f65dbda2', null, '45678912L', 'Pepe dos', 'calle getafe', '123', 'GETAFE', 'ESPAÑA','pepedos@test.com', 654123700, null, null, true, false, CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());
