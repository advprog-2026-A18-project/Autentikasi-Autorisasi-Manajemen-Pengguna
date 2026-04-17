

## 1. Authentication (`/auth`)

#### Local Register
```http
  POST /auth/register
```
**Request Body**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `username` | `string` | **Required**. User name |
| `fullname` | `string` | **Required**. User fullname |
| `email` | `string` | **Required**. User email |
| `password` | `string` | **Required**. User password |
| `role` | `string` | **Required**. Role (e.g., BURUH, MANDOR) |
| `nomorSertifikasi` | `string` | **Required (for Mandor)**. Certification number |

**Response**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `statusCode` | `int` | Standard HTTP Status Code (201) |
| `message` | `string` | Success/error message|
| `data` | `object` | `{ accessToken, refreshToken, user: {id, username, fullname, email, role, nomorSertifikasi} }` |

#### Local Login
```http
  POST /auth/login
```
**Request Body**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `email` | `string` | **Required**. User email |
| `password` | `string` | **Required**. User password |

**Response**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `statusCode` | `int` | Standard HTTP Status Code (200, 401) |
| `message` | `string` | Success/error message|
| `data` | `object` | `{ accessToken, refreshToken, user: {...} }` |

#### Google Login / Register
```http
  POST /auth/google
```
**Request Body**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `idToken` | `string` | **Required**. Google ID Token |
| `role` | `string` | **Optional**. Required if user is new (Register) |
| `nomorSertifikasi` | `string` | **Optional**. Required if new user is MANDOR |

**Response**

#### Refresh Token
```http
  POST /auth/refresh
```
**Request Body**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `refreshToken` | `string` | **Required**. Valid Refresh Token |

**Response** *(Returns new accessToken and user data)*

#### Logout
```http
  POST /auth/logout
```
**Request Body**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `refreshToken` | `string` | **Required**. Refresh Token to be revoked |

---

## 2. User General (`/users`)

**Headers Required (All Routes):**
`Authorization: Bearer <JWT_ACCESS_TOKEN>`

#### Get My Profile
```http
  GET /users/me
```
**Response**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `statusCode` | `int` | HTTP Status Code (200) |
| `message` | `string` | Success message |
| `data` | `object` | `{ id, username, fullname, email, role, nomorSertifikasi, namaMandor }` |

#### Update My Profile
```http
  PUT /users/me
```
**Request Body**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `fullname` | `string` | **Required**. New fullname |
| `username` | `string` | **Required**. New username |

---

## 3. Mandor Operations (`/mandor`)

**Headers Required (All Routes):**
`Authorization: Bearer <JWT_ACCESS_TOKEN>` *(Must have MANDOR role)*

#### Get Daftar Bawahan
```http
  GET /mandor/bawahan
```
**Query Parameters (Optional)**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `name` | `string` | Filter bawahan by name |

**Response**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `data` | `array` | List of User objects: `[{ id, username, fullname, email, role }]` |

---

## 4. Admin Management (`/admin/users`)

**Headers Required (All Routes):**
`Authorization: Bearer <JWT_ACCESS_TOKEN>` *(Must have ADMIN role)*

#### Search & List All Users
```http
  GET /admin/users
```
**Query Parameters (Optional)**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `name` | `string` | Filter by fullname |
| `email` | `string` | Filter by email |
| `role` | `string` | Filter by role |
| `page` | `int` | Page number (Default: 0) |
| `size` | `int` | Elements per page (Default: 10) |

**Response**
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `data` | `object` | Paging Object: `{ content: [...], currentPage, totalPages, totalElements }` |

#### Get User Detail
```http
  GET /admin/users/{userId}
```
*Returns full details of a specific user by UUID.*

#### Assign Mandor to Buruh
```http
  PUT /admin/users/{buruhId}/assign-mandor/{mandorId}
```

#### Unassign Mandor from Buruh
```http
  PUT /admin/users/{buruhId}/unassign-mandor
```

#### Delete User
```http
  DELETE /admin/users/{userId}
```

---

## Authors

- [@evanhwz](https://www.github.com/evanhwz)
