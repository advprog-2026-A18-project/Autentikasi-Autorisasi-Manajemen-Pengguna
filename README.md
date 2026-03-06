
# Authentikasi-Autorisasi-Manajemen-Pengguna API

## API Reference
Below are some examples of the API routes that can be used from the project
#### Register Manual

```
  POST /register
```
### Request Body
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `username` | `string` | **Required**. User name |
| `fullname` | `string` | **Required**. User fullname |
| `email` | `string` | **Required**. User email |
| `password` | `string` | **Required**. User password |
| `role` | `string` | **Required**. User password |
| `nomor_sertifikasi` | `string` | **Required (for Mandor)**. User cert_num |

### Response
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `statusCode` | `int` | it defines the http code (200, 400, 500, etc) |
| `message` | `string` | it defines the success/error message|
| `data` | `map/dictionary/null` | key: {'accessToken','id', 'username', 'fullname', 'email', 'role' } |

#### Login Manual
```
  POST /login
```
### Request Body
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `email` | `string` | **Required**. User email |
| `password` | `string` | **Required**. User password |

### Response
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `statusCode` | `int` | it defines the http code (200, 400, 500, etc) |
| `message` | `string` | it defines the success/error message|
| `data` | `map/dictionary/null` | key: {'accessToken','id', 'username', 'fullname', 'email', 'role' } |

#### Data Pribadi User
```
  GET /my-data
```

### Request Headers
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `Authorization` | `string` | **Required**. Bearer <JWT_ACCESS_TOKEN> |


### Request Body
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `id` | `string` | **Required**. User id |

### Response
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `statusCode` | `int` | it defines the http code (200, 400, 500, etc) |
| `message` | `string` | it defines the success/error message|
| `data` | `map/dictionary/null` | key: {'id', 'username', 'fullname', 'email', 'role' } |


#### Data Bawahan
```
  GET /my-bawahan
```

### Request Headers
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `Authorization` | `string` | **Required**. Bearer <JWT_ACCESS_TOKEN> |


### Request Body
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `id` | `string` | **Required**. User id |

### Response
| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `statusCode` | `int` | it defines the http code (200, 400, 500, etc) |
| `message` | `string` | it defines the success/error message|
| `data` | `array/list/null` | key: [{'id', 'username', 'fullname', 'email', 'role' }] |



## Authors

- [@evanhwz](https://www.github.com/evanhwz)

