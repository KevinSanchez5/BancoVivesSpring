# BancoVivesSpring 

Enlace de video: https://www.youtube.com/watch?v=FDJ-21XrlP0


### Accounts

| Endpoint                          | URL                                      | HTTP Verbo | AUTH                         | Descripción                                | HTTP Status Code                  | Otras Salidas                                                  |
|-----------------------------------|------------------------------------------|------------|------------------------------|--------------------------------------------|-----------------------------------|----------------------------------------------------------------|
| Obtener todas las cuentas         | `GET /api.version/accounts`              | GET        | ADMIN, SUPER_ADMIN           | Recupera una lista paginada de cuentas     | 200 OK                           | 400 Bad Request, 401 Unauthorized, 403 Forbidden               |
| Obtener cuenta por ID             | `GET /api.version/accounts/{id}`         | GET        | ADMIN, SUPER_ADMIN           | Recupera una cuenta específica por ID      | 200 OK                           | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found|
| Crear una cuenta                  | `POST /api.version/accounts`             | POST       | ADMIN, SUPER_ADMIN           | Crea una nueva cuenta                      | 201 Created                       | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 422 Unprocessable Entity |
| Obtener cuenta por IBAN           | `GET /api.version/accounts/iban/{iban}`  | GET        | ADMIN, SUPER_ADMIN           | Recupera una cuenta específica por IBAN    | 200 OK                           | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found|
| Actualizar una cuenta             | `PUT /api.version/accounts/{id}`         | PUT        | ADMIN, SUPER_ADMIN           | Actualiza una cuenta existente por ID      | 200 OK                           | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 422 Unprocessable Entity |
| Eliminar una cuenta               | `DELETE /api.version/accounts/{id}`      | DELETE     | ADMIN, SUPER_ADMIN           | Elimina (lógicamente) una cuenta por ID    | 200 OK                           | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found |
| Obtener mis cuentas               | `GET /api.version/accounts/myAccounts`   | GET        | USER, ADMIN, SUPER_ADMIN     | Recupera las cuentas del usuario autenticado | 200 OK                         | 400 Bad Request, 401 Unauthorized, 403 Forbidden               |


### Cards



| Endpoint                          | URL                                      | HTTP Verbo | AUTH                         | Descripción                                | HTTP Status Code                  | Otras Salidas                                                  |
|-----------------------------------|------------------------------------------|------------|------------------------------|--------------------------------------------|-----------------------------------|----------------------------------------------------------------|
| Obtener todas las tarjetas        | `GET /api.version/cards`                 | GET        | ADMIN, SUPER_ADMIN           | Recupera una lista paginada de tarjetas    | 200 OK                           | 401 Unauthorized, 403 Forbidden                                |
| Crear una tarjeta                 | `POST /api.version/cards`                | POST       | ADMIN, SUPER_ADMIN           | Crea una nueva tarjeta                     | 201 Created                       | 401 Unauthorized, 403 Forbidden                                |
| Obtener tarjeta por ID            | `GET /api.version/cards/{id}`            | GET        | ADMIN, SUPER_ADMIN           | Recupera una tarjeta específica por ID     | 200 OK                           | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Obtener tarjeta por nombre        | `GET /api.version/cards/name/{name}`     | GET        | ADMIN, SUPER_ADMIN           | Recupera una tarjeta específica por nombre del propietario | 200 OK | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Actualizar una tarjeta por ID     | `PUT /api.version/cards/{id}`            | PUT        | ADMIN, SUPER_ADMIN           | Actualiza los datos de una tarjeta existente | 200 OK                        | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Eliminar una tarjeta por ID       | `DELETE /api.version/cards/{id}`         | DELETE     | ADMIN, SUPER_ADMIN           | Elimina una tarjeta existente por ID       | 200 OK                           | 401 Unauthorized, 403 Forbidden, 404 Not Found                |


### Clients

| Endpoint                          | URL                                      | HTTP Verbo | AUTH                         | Descripción                                | HTTP Status Code                  | Otras Salidas                                                  |
|-----------------------------------|------------------------------------------|------------|------------------------------|--------------------------------------------|-----------------------------------|----------------------------------------------------------------|
| Obtener todos los clientes        | `GET /api.version/clients`               | GET        | ADMIN, SUPER_ADMIN           | Recupera una lista paginada de clientes    | 200 OK                           | 401 Unauthorized, 403 Forbidden                                |
| Crear un cliente                  | `POST /api.version/clients`              | POST       | ADMIN, SUPER_ADMIN           | Crea un nuevo cliente                      | 201 Created                       | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 409 Conflict |
| Obtener cliente por ID            | `GET /api.version/clients/{id}`          | GET        | ADMIN, SUPER_ADMIN           | Recupera un cliente específico por ID      | 200 OK                           | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Actualizar un cliente por ID      | `PUT /api.version/clients/{id}`          | PUT        | ADMIN, SUPER_ADMIN           | Actualiza los datos de un cliente existente | 200 OK                        | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict |
| Eliminar un cliente por ID        | `DELETE /api.version/clients/{id}`       | DELETE     | ADMIN, SUPER_ADMIN           | Elimina un cliente existente por ID        | 200 OK                           | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Validar un cliente por ID         | `PUT /api.version/clients/{id}/validate` | PUT        | ADMIN, SUPER_ADMIN           | Valida los datos de un cliente por ID      | 200 OK                           | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Obtener información del cliente autenticado | `GET /api.version/clients/me` | GET        | USER, ADMIN, SUPER_ADMIN     | Recupera la información del cliente autenticado | 200 OK                       | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Actualizar imagen del DNI         | `PATCH /api.version/clients/dniImage`    | PATCH      | USER, ADMIN, SUPER_ADMIN     | Actualiza la imagen del DNI del cliente    | 200 OK                           | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Actualizar foto de perfil         | `PATCH /api.version/clients/photo`       | PATCH      | USER, ADMIN, SUPER_ADMIN     | Actualiza la foto de perfil del cliente    | 200 OK                           | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Exportar datos del cliente autenticado | `GET /api.version/clients/exportMe` | GET        | USER, ADMIN, SUPER_ADMIN     | Exporta los datos del cliente autenticado  | 200 OK                           | 401 Unauthorized, 403 Forbidden, 404 Not Found                |

### Divisas

| Endpoint                          | URL                                      | HTTP Verbo | AUTH                         | Descripción                                | HTTP Status Code                  | Otras Salidas                                                  |
|-----------------------------------|------------------------------------------|------------|------------------------------|--------------------------------------------|-----------------------------------|----------------------------------------------------------------|
| Convertir divisas                 | `GET /api.version/convert`               | GET        | None                         | Convierte una cantidad de una divisa a otra | 200 OK                           | 400 Bad Request                                                |

### Movements

| Endpoint                          | URL                                      | HTTP Verbo | AUTH                         | Descripción                                | HTTP Status Code                  | Otras Salidas                                                  |
|-----------------------------------|------------------------------------------|------------|------------------------------|--------------------------------------------|-----------------------------------|----------------------------------------------------------------|
| Obtener todos los movimientos     | `GET /api.version/movements`             | GET        | ADMIN, SUPER_ADMIN           | Recupera una lista paginada de movimientos | 200 OK                           | 401 Unauthorized, 403 Forbidden                                |
| Obtener movimiento por ID         | `GET /api.version/movements/{id}`        | GET        | ADMIN, SUPER_ADMIN           | Recupera un movimiento específico por ID   | 200 OK                           | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Crear un movimiento               | `POST /api.version/movements`            | POST       | USER, ADMIN, SUPER_ADMIN     | Crea un nuevo movimiento                   | 201 Created                       | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found |
| Actualizar un movimiento por ID   | `PUT /api.version/movements/{id}`        | PUT        | ADMIN, SUPER_ADMIN           | Actualiza los datos de un movimiento existente | 200 OK                        | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found |
| Eliminar un movimiento por ID     | `DELETE /api.version/movements/{id}`     | DELETE     | USER, ADMIN, SUPER_ADMIN     | Elimina un movimiento existente por ID     | 204 No Content                    | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Añadir interés manualmente        | `POST /api.version/movements/addinterest`| POST       | ADMIN, SUPER_ADMIN           | Añade interés manualmente a una cuenta     | 201 Created                       | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found |
| Obtener movimientos del usuario   | `GET /api.version/movements/myMovements` | GET        | USER, ADMIN, SUPER_ADMIN     | Recupera los movimientos del usuario autenticado | 200 OK                       | 401 Unauthorized, 403 Forbidden                                |


### Products

| Endpoint                          | URL                                      | HTTP Verbo | AUTH                         | Descripción                                | HTTP Status Code                  | Otras Salidas                                                  |
|-----------------------------------|------------------------------------------|------------|------------------------------|--------------------------------------------|-----------------------------------|----------------------------------------------------------------|
| Obtener todos los tipos de cuentas | `GET /api.version/products/accounts`     | GET        | ADMIN, SUPER_ADMIN           | Recupera una lista paginada de tipos de cuentas | 200 OK                       | 401 Unauthorized, 403 Forbidden                                |
| Crear un tipo de cuenta           | `POST /api.version/products/accounts`    | POST       | ADMIN, SUPER_ADMIN           | Crea un nuevo tipo de cuenta                | 201 Created                       | 409 Conflict, 401 Unauthorized, 403 Forbidden                  |
| Obtener tipo de cuenta por ID     | `GET /api.version/products/accounts/{id}`| GET        | ADMIN, SUPER_ADMIN           | Recupera un tipo de cuenta específico por ID | 200 OK                       | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Obtener tipo de cuenta por nombre | `GET /api.version/products/accounts/name/{name}` | GET | ADMIN, SUPER_ADMIN | Recupera un tipo de cuenta específico por nombre | 200 OK | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Actualizar un tipo de cuenta por ID | `PUT /api.version/products/accounts/{id}` | PUT | ADMIN, SUPER_ADMIN | Actualiza los datos de un tipo de cuenta existente | 200 OK | 409 Conflict, 401 Unauthorized, 403 Forbidden, 404 Not Found |
| Eliminar un tipo de cuenta por ID | `DELETE /api.version/products/accounts/{id}` | DELETE | ADMIN, SUPER_ADMIN | Elimina un tipo de cuenta existente por ID | 200 OK | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Importar tipos de cuentas desde CSV | `POST /api.version/products/accounts/import` | POST | ADMIN, SUPER_ADMIN | Importa tipos de cuentas desde un archivo CSV | 201 Created | 400 Bad Request, 401 Unauthorized, 403 Forbidden              |
| Obtener todos los tipos de tarjetas | `GET /api.version/products/cards`       | GET        | ADMIN, SUPER_ADMIN           | Recupera una lista paginada de tipos de tarjetas | 200 OK                       | 401 Unauthorized, 403 Forbidden                                |
| Crear un tipo de tarjeta          | `POST /api.version/products/cards`       | POST       | ADMIN, SUPER_ADMIN           | Crea un nuevo tipo de tarjeta                | 201 Created                       | 409 Conflict, 401 Unauthorized, 403 Forbidden                  |
| Obtener tipo de tarjeta por ID    | `GET /api.version/products/cards/{id}`   | GET        | ADMIN, SUPER_ADMIN           | Recupera un tipo de tarjeta específico por ID | 200 OK                       | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Obtener tipo de tarjeta por nombre | `GET /api.version/products/cards/name/{name}` | GET | ADMIN, SUPER_ADMIN | Recupera un tipo de tarjeta específico por nombre | 200 OK | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Actualizar un tipo de tarjeta por ID | `PUT /api.version/products/cards/{id}` | PUT | ADMIN, SUPER_ADMIN | Actualiza los datos de un tipo de tarjeta existente | 200 OK | 409 Conflict, 401 Unauthorized, 403 Forbidden, 404 Not Found |
| Eliminar un tipo de tarjeta por ID | `DELETE /api.version/products/cards/{id}` | DELETE | ADMIN, SUPER_ADMIN | Elimina un tipo de tarjeta existente por ID | 200 OK | 401 Unauthorized, 403 Forbidden, 404 Not Found                |


### Users

| Endpoint                          | URL                                      | HTTP Verbo | AUTH                         | Descripción                                | HTTP Status Code                  | Otras Salidas                                                  |
|-----------------------------------|------------------------------------------|------------|------------------------------|--------------------------------------------|-----------------------------------|----------------------------------------------------------------|
| Obtener todos los usuarios        | `GET /api.version/users`                 | GET        | ADMIN, SUPER_ADMIN           | Recupera una lista paginada de usuarios    | 200 OK                           | 401 Unauthorized, 403 Forbidden                                |
| Obtener usuario por ID            | `GET /api.version/users/{id}`            | GET        | ADMIN, SUPER_ADMIN           | Recupera un usuario específico por ID      | 200 OK                           | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Añadir un administrador           | `POST /api.version/users/addAdmin`       | POST       | SUPER_ADMIN                  | Añade un nuevo administrador               | 201 Created                       | 401 Unauthorized, 403 Forbidden                                |
| Actualizar un usuario por ID      | `PUT /api.version/users/{id}`            | PUT        | ADMIN, SUPER_ADMIN           | Actualiza los datos de un usuario existente | 200 OK                        | 400 Bad Request, 401 Unauthorized, 403 Forbidden, 404 Not Found, 409 Conflict |
| Eliminar un usuario por ID        | `DELETE /api.version/users/{id}`         | DELETE     | ADMIN, SUPER_ADMIN           | Elimina un usuario existente por ID        | 204 No Content                    | 401 Unauthorized, 403 Forbidden, 404 Not Found                |
| Iniciar sesión                    | `POST /api.version/users/signIn`         | POST       | None                         | Inicia sesión y obtiene un token JWT       | 200 OK                           | 401 Unauthorized, 403 Forbidden                                |
