# Leo Shop API 文件

> **Base URL：** `http://localhost:8080`
> **認證方式：** JWT Bearer Token
> **預設帳號：** `admin@leoshop.com` / `admin123`

## 認證說明

需要認證的 API 請在 Header 加入：
```
Authorization: Bearer <token>
```

圖例：
- 🌐 公開 — 不需認證
- 🔓 需登入 — 需要 JWT Token
- 🔑 需 ADMIN — 需要 ADMIN 角色的 JWT Token

---

## 目錄

1. [Health Check](#1-health-check)
2. [認證 API](#2-認證-api)
3. [用戶 API](#3-用戶-api)
4. [商品 API](#4-商品-api)
5. [商品管理 API（Admin）](#5-商品管理-apiadmin)
6. [訂單 API](#6-訂單-api)
7. [訂單管理 API（Admin）](#7-訂單管理-apiadmin)
8. [地址 API](#8-地址-api)
9. [Dashboard API（Admin）](#9-dashboard-apiadmin)
10. [管理員管理 API（Admin）](#10-管理員管理-apiadmin)
11. [支付方式管理 API（Admin）](#11-支付方式管理-apiadmin)

---

## 1. Health Check

### `GET /api/health` 🌐

健康檢查，確認服務運行中。

**Response：**
```json
{
  "status": "ok",
  "timestamp": "2026-02-11T16:00:00.000"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 服務正常 |

---

## 2. 認證 API

### `POST /api/auth/register` 🌐

註冊新帳號。

**Request Body：**
```json
{
  "name": "Leo",
  "email": "leo@example.com",
  "password": "mypassword123"
}
```

**成功 Response（200）：**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "name": "Leo",
    "email": "leo@example.com",
    "phone": null,
    "role": "USER"
  }
}
```

**錯誤 Response（400）：**
```json
{
  "error": "Email already exists"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 註冊成功 |
| 400 | 驗證失敗（欄位空白、Email 格式錯誤、Email 已存在） |

---

### `POST /api/auth/login` 🌐

登入取得 JWT Token。

**Request Body：**
```json
{
  "email": "admin@leoshop.com",
  "password": "admin123"
}
```

**成功 Response（200）：**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "name": "Admin",
    "email": "admin@leoshop.com",
    "phone": null,
    "role": "ADMIN"
  }
}
```

**錯誤 Response（401）：**
```json
{
  "error": "Invalid email or password"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 登入成功 |
| 400 | 驗證失敗（欄位空白、Email 格式錯誤） |
| 401 | 帳號或密碼錯誤 |

---

## 3. 用戶 API

### `GET /api/user/profile` 🔓

取得目前登入用戶的個人資料。

**Request Headers：**
```
Authorization: Bearer <token>
```

**成功 Response（200）：**
```json
{
  "id": 1,
  "name": "Leo",
  "email": "leo@example.com",
  "phone": "0912345678",
  "role": "USER"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 401 | 未認證 |

---

### `PUT /api/user/profile` 🔓

更新個人資料。

**Request Headers：**
```
Authorization: Bearer <token>
```

**Request Body：**
```json
{
  "name": "Leo Chen",
  "phone": "0912345678"
}
```

**成功 Response（200）：**
```json
{
  "id": 1,
  "name": "Leo Chen",
  "email": "leo@example.com",
  "phone": "0912345678",
  "role": "USER"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 更新成功 |
| 401 | 未認證 |

---

### `PUT /api/user/password` 🔓

變更密碼。

**Request Headers：**
```
Authorization: Bearer <token>
```

**Request Body：**
```json
{
  "oldPassword": "mypassword123",
  "newPassword": "newpassword456"
}
```

**成功 Response（200）：** 空 body

**錯誤 Response（400）：**
```json
{
  "error": "Old password is incorrect"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 變更成功 |
| 400 | 舊密碼錯誤或驗證失敗 |
| 401 | 未認證 |

---

## 4. 商品 API

### `GET /api/products` 🌐

取得商品列表（支援篩選、搜尋、排序、分頁）。

**Query Parameters：**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|------|------|------|--------|------|
| `category` | string | 否 | — | 分類篩選 |
| `keyword` | string | 否 | — | 關鍵字搜尋 |
| `sort` | string | 否 | — | 排序方式（如 `price_asc`、`price_desc`、`newest`） |
| `page` | int | 否 | 0 | 頁碼（從 0 開始） |
| `size` | int | 否 | 12 | 每頁筆數 |

**範例：**
```
GET /api/products?category=手工藝品&keyword=陶&sort=price_asc&page=0&size=12
```

**成功 Response（200）：**
```json
{
  "content": [
    {
      "id": 1,
      "name": "手工陶瓷杯",
      "slug": "handmade-ceramic-cup",
      "description": "精緻手工陶瓷杯...",
      "price": 580.00,
      "comparePrice": 780.00,
      "imageUrl": "https://example.com/cup.jpg",
      "category": "手工藝品",
      "stock": 25,
      "active": true,
      "createdAt": "2026-02-11T10:00:00",
      "updatedAt": "2026-02-11T10:00:00"
    }
  ],
  "totalPages": 3,
  "totalElements": 30,
  "currentPage": 0
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |

---

### `GET /api/products/categories` 🌐

取得所有商品分類列表。

**成功 Response（200）：**
```json
["手工藝品", "居家生活", "飾品配件"]
```

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |

---

### `GET /api/products/{slug}` 🌐

以 slug 取得單一商品詳情。

**範例：**
```
GET /api/products/handmade-ceramic-cup
```

**成功 Response（200）：**
```json
{
  "id": 1,
  "name": "手工陶瓷杯",
  "slug": "handmade-ceramic-cup",
  "description": "精緻手工陶瓷杯...",
  "price": 580.00,
  "comparePrice": 780.00,
  "imageUrl": "https://example.com/cup.jpg",
  "category": "手工藝品",
  "stock": 25,
  "active": true,
  "createdAt": "2026-02-11T10:00:00",
  "updatedAt": "2026-02-11T10:00:00"
}
```

**錯誤 Response（404）：**
```json
{
  "error": "Product not found"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 404 | 商品不存在 |

---

## 5. 商品管理 API（Admin）

### `POST /api/admin/products` 🔑

新增商品。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**Request Body：**
```json
{
  "name": "手工陶瓷杯",
  "slug": "handmade-ceramic-cup",
  "description": "精緻手工陶瓷杯，每個都是獨一無二的作品。",
  "price": 580.00,
  "comparePrice": 780.00,
  "imageUrl": "https://example.com/cup.jpg",
  "category": "手工藝品",
  "stock": 25,
  "active": true
}
```

**成功 Response（200）：**
```json
{
  "id": 1,
  "name": "手工陶瓷杯",
  "slug": "handmade-ceramic-cup",
  "description": "精緻手工陶瓷杯，每個都是獨一無二的作品。",
  "price": 580.00,
  "comparePrice": 780.00,
  "imageUrl": "https://example.com/cup.jpg",
  "category": "手工藝品",
  "stock": 25,
  "active": true,
  "createdAt": "2026-02-11T10:00:00",
  "updatedAt": "2026-02-11T10:00:00"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 新增成功 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |

---

### `PUT /api/admin/products/{id}` 🔑

更新商品。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**Request Body：** 同新增商品格式

**成功 Response（200）：** 同商品 Response 格式

| Status Code | 說明 |
|-------------|------|
| 200 | 更新成功 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |
| 404 | 商品不存在 |

---

### `DELETE /api/admin/products/{id}` 🔑

刪除商品。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**成功 Response：** 204 No Content

| Status Code | 說明 |
|-------------|------|
| 204 | 刪除成功 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |
| 404 | 商品不存在 |

---

## 6. 訂單 API

### `POST /api/orders` 🔓

建立訂單。

**Request Headers：**
```
Authorization: Bearer <token>
```

**Request Body：**
```json
{
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 3, "quantity": 1 }
  ],
  "shippingName": "Leo Chen",
  "shippingPhone": "0912345678",
  "shippingEmail": "leo@example.com",
  "shippingAddress": "台北市大安區忠孝東路一段 100 號",
  "paymentMethod": "CREDIT_CARD",
  "note": "請小心包裝"
}
```

**成功 Response（200）：**
```json
{
  "id": 1,
  "orderNumber": "ORD-20260211-0001",
  "status": "PENDING",
  "totalAmount": 1740.00,
  "shippingFee": 60.00,
  "shippingName": "Leo Chen",
  "shippingPhone": "0912345678",
  "shippingEmail": "leo@example.com",
  "shippingAddress": "台北市大安區忠孝東路一段 100 號",
  "paymentMethod": "CREDIT_CARD",
  "note": "請小心包裝",
  "items": [
    {
      "id": 1,
      "productId": 1,
      "productName": "手工陶瓷杯",
      "productPrice": 580.00,
      "quantity": 2,
      "subtotal": 1160.00
    },
    {
      "id": 2,
      "productId": 3,
      "productName": "棉麻圍巾",
      "productPrice": 520.00,
      "quantity": 1,
      "subtotal": 520.00
    }
  ],
  "createdAt": "2026-02-11T16:00:00",
  "updatedAt": "2026-02-11T16:00:00"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 建立成功 |
| 400 | 驗證失敗（庫存不足等） |
| 401 | 未認證 |

---

### `GET /api/orders` 🔓

取得目前用戶的訂單列表（分頁）。

**Request Headers：**
```
Authorization: Bearer <token>
```

**Query Parameters：**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|------|------|------|--------|------|
| `page` | int | 否 | 0 | 頁碼 |
| `size` | int | 否 | 10 | 每頁筆數 |

**成功 Response（200）：**
```json
{
  "content": [
    {
      "id": 1,
      "orderNumber": "ORD-20260211-0001",
      "status": "PENDING",
      "totalAmount": 1740.00,
      "shippingFee": 60.00,
      "shippingName": "Leo Chen",
      "shippingPhone": "0912345678",
      "shippingEmail": "leo@example.com",
      "shippingAddress": "台北市大安區忠孝東路一段 100 號",
      "paymentMethod": "CREDIT_CARD",
      "note": "請小心包裝",
      "items": [],
      "createdAt": "2026-02-11T16:00:00",
      "updatedAt": "2026-02-11T16:00:00"
    }
  ],
  "totalPages": 1,
  "totalElements": 1,
  "currentPage": 0
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 401 | 未認證 |

---

### `GET /api/orders/{orderNumber}` 🔓

以訂單編號取得訂單詳情。

**Request Headers：**
```
Authorization: Bearer <token>
```

**成功 Response（200）：** 同訂單 Response 格式

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 401 | 未認證 |
| 404 | 訂單不存在 |

---

### `PUT /api/orders/{id}/cancel` 🔓

取消訂單。

**Request Headers：**
```
Authorization: Bearer <token>
```

**成功 Response（200）：** 同訂單 Response 格式（status 變為 `CANCELLED`）

| Status Code | 說明 |
|-------------|------|
| 200 | 取消成功 |
| 400 | 訂單狀態不可取消 |
| 401 | 未認證 |
| 404 | 訂單不存在 |

---

## 7. 訂單管理 API（Admin）

### `GET /api/admin/orders` 🔑

取得所有訂單（支援狀態篩選、分頁）。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**Query Parameters：**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|------|------|------|--------|------|
| `status` | string | 否 | — | 狀態篩選（`PENDING`、`CONFIRMED`、`SHIPPED`、`DELIVERED`、`CANCELLED`） |
| `page` | int | 否 | 0 | 頁碼 |
| `size` | int | 否 | 10 | 每頁筆數 |

**成功 Response（200）：** 同 OrderListResponse 格式

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |

---

### `GET /api/admin/orders/{id}` 🔑

以 ID 取得訂單詳情。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**成功 Response（200）：** 同訂單 Response 格式

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |
| 404 | 訂單不存在 |

---

### `PUT /api/admin/orders/{id}/status` 🔑

更新訂單狀態。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**Request Body：**
```json
{
  "status": "SHIPPED"
}
```

可用狀態：`PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED`、`CANCELLED`

**成功 Response（200）：** 同訂單 Response 格式

| Status Code | 說明 |
|-------------|------|
| 200 | 更新成功 |
| 400 | 狀態轉換不合法 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |
| 404 | 訂單不存在 |

---

## 8. 地址 API

### `GET /api/addresses` 🔓

取得目前用戶的所有收件地址。

**Request Headers：**
```
Authorization: Bearer <token>
```

**成功 Response（200）：**
```json
[
  {
    "id": 1,
    "name": "Leo Chen",
    "phone": "0912345678",
    "address": "台北市大安區忠孝東路一段 100 號",
    "isDefault": true
  },
  {
    "id": 2,
    "name": "Leo（公司）",
    "phone": "0223456789",
    "address": "台北市信義區松仁路 50 號",
    "isDefault": false
  }
]
```

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 401 | 未認證 |

---

### `POST /api/addresses` 🔓

新增收件地址。

**Request Headers：**
```
Authorization: Bearer <token>
```

**Request Body：**
```json
{
  "name": "Leo Chen",
  "phone": "0912345678",
  "address": "台北市大安區忠孝東路一段 100 號",
  "isDefault": true
}
```

**成功 Response（200）：**
```json
{
  "id": 1,
  "name": "Leo Chen",
  "phone": "0912345678",
  "address": "台北市大安區忠孝東路一段 100 號",
  "isDefault": true
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 新增成功 |
| 401 | 未認證 |

---

### `PUT /api/addresses/{id}` 🔓

更新收件地址。

**Request Headers：**
```
Authorization: Bearer <token>
```

**Request Body：** 同新增地址格式

**成功 Response（200）：** 同地址 Response 格式

| Status Code | 說明 |
|-------------|------|
| 200 | 更新成功 |
| 401 | 未認證 |
| 404 | 地址不存在 |

---

### `DELETE /api/addresses/{id}` 🔓

刪除收件地址。

**Request Headers：**
```
Authorization: Bearer <token>
```

**成功 Response：** 204 No Content

| Status Code | 說明 |
|-------------|------|
| 204 | 刪除成功 |
| 401 | 未認證 |
| 404 | 地址不存在 |

---

### `PUT /api/addresses/{id}/default` 🔓

設定預設收件地址。

**Request Headers：**
```
Authorization: Bearer <token>
```

**成功 Response（200）：** 同地址 Response 格式（`isDefault: true`）

| Status Code | 說明 |
|-------------|------|
| 200 | 設定成功 |
| 401 | 未認證 |
| 404 | 地址不存在 |

---

## 9. Dashboard API（Admin）

### `GET /api/admin/dashboard/stats` 🔑

取得總覽統計數據。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**成功 Response（200）：**
```json
{
  "totalRevenue": 125600.00,
  "totalOrders": 48,
  "totalProducts": 30,
  "totalUsers": 156
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |

---

### `GET /api/admin/dashboard/revenue` 🔑

取得近 7 天每日營收趨勢。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**成功 Response（200）：**
```json
[
  { "date": "2026-02-05", "revenue": 15800.00 },
  { "date": "2026-02-06", "revenue": 22400.00 },
  { "date": "2026-02-07", "revenue": 18600.00 },
  { "date": "2026-02-08", "revenue": 9200.00 },
  { "date": "2026-02-09", "revenue": 31000.00 },
  { "date": "2026-02-10", "revenue": 16500.00 },
  { "date": "2026-02-11", "revenue": 12100.00 }
]
```

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |

---

## 10. 管理員管理 API（Admin）

### `GET /api/admin/admin-users` 🔑

取得所有管理員列表。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**成功 Response（200）：**
```json
[
  {
    "id": 1,
    "email": "admin@leoshop.com",
    "name": "Admin",
    "createdAt": "2026-02-11T10:00:00"
  }
]
```

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |

---

### `POST /api/admin/admin-users` 🔑

新增管理員帳號。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**Request Body：**
```json
{
  "email": "newadmin@leoshop.com",
  "password": "password123",
  "name": "New Admin"
}
```

**成功 Response（200）：**
```json
{
  "id": 2,
  "email": "newadmin@leoshop.com",
  "name": "New Admin",
  "createdAt": "2026-03-04T10:00:00"
}
```

**錯誤 Response（400）：**
```json
{
  "error": "Email already exists"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 新增成功 |
| 400 | Email 已存在或驗證失敗 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |

---

### `PUT /api/admin/admin-users/{id}` 🔑

更新管理員資料。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**Request Body：**
```json
{
  "email": "updated@leoshop.com",
  "name": "Updated Name",
  "password": "newpassword123"
}
```

> `password` 為選填，若不提供則不更新密碼。

**成功 Response（200）：**
```json
{
  "id": 2,
  "email": "updated@leoshop.com",
  "name": "Updated Name",
  "createdAt": "2026-03-04T10:00:00"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 更新成功 |
| 400 | 驗證失敗 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |
| 404 | 管理員不存在 |

---

### `DELETE /api/admin/admin-users/{id}` 🔑

刪除管理員帳號。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**成功 Response：** 204 No Content

| Status Code | 說明 |
|-------------|------|
| 204 | 刪除成功 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |
| 404 | 管理員不存在 |

---

## 11. 支付方式管理 API（Admin）

### `POST /api/admin/payment-methods/refresh-rates` 🔑

刷新所有加密貨幣匯率（呼叫 CoinGecko API）。

**Request Headers：**
```
Authorization: Bearer <admin-token>
```

**成功 Response（200）：**
```json
{
  "message": "Rates refreshed successfully",
  "updatedCount": 3,
  "timestamp": "2026-03-04T10:00:00"
}
```

**錯誤 Response（500）：**
```json
{
  "error": "Failed to fetch rates from CoinGecko API"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 刷新成功 |
| 401 | 未認證 |
| 403 | 非 ADMIN 角色 |
| 500 | API 呼叫失敗 |

---

### `GET /api/orders/shipping-fee` 🌐

查詢運費（公開 API，根據商品小計計算）。

**Query Parameters：**

| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| `subtotal` | decimal | 是 | 商品小計金額 |

**範例：**
```
GET /api/orders/shipping-fee?subtotal=1500
```

**成功 Response（200）：**
```json
{
  "subtotal": 1500.00,
  "shippingFee": 0.00,
  "freeShippingThreshold": 1000.00,
  "message": "Free shipping (order >= NT$1000)"
}
```

**範例（未達免運）：**
```
GET /api/orders/shipping-fee?subtotal=800
```

**成功 Response（200）：**
```json
{
  "subtotal": 800.00,
  "shippingFee": 60.00,
  "freeShippingThreshold": 1000.00,
  "message": "Add NT$200 more for free shipping"
}
```

| Status Code | 說明 |
|-------------|------|
| 200 | 成功 |
| 400 | subtotal 參數缺失或格式錯誤 |

---

## CORS 設定更新

後端 CORS 設定已更新，支援以下 HTTP 方法：
```
GET, POST, PUT, DELETE, PATCH, OPTIONS
```

前端可使用 `PATCH` 方法進行局部更新操作。

---

## API 端點總覽

| Method | URL | 認證 | 說明 |
|--------|-----|------|------|
| GET | `/api/health` | 🌐 | 健康檢查 |
| POST | `/api/auth/register` | 🌐 | 註冊 |
| POST | `/api/auth/login` | 🌐 | 登入 |
| GET | `/api/user/profile` | 🔓 | 取得個人資料 |
| PUT | `/api/user/profile` | 🔓 | 更新個人資料 |
| PUT | `/api/user/password` | 🔓 | 變更密碼 |
| GET | `/api/products` | 🌐 | 商品列表 |
| GET | `/api/products/categories` | 🌐 | 分類列表 |
| GET | `/api/products/{slug}` | 🌐 | 商品詳情 |
| POST | `/api/admin/products` | 🔑 | 新增商品 |
| PUT | `/api/admin/products/{id}` | 🔑 | 更新商品 |
| DELETE | `/api/admin/products/{id}` | 🔑 | 刪除商品 |
| POST | `/api/orders` | 🔓 | 建立訂單 |
| GET | `/api/orders` | 🔓 | 我的訂單 |
| GET | `/api/orders/{orderNumber}` | 🔓 | 訂單詳情 |
| PUT | `/api/orders/{id}/cancel` | 🔓 | 取消訂單 |
| GET | `/api/admin/orders` | 🔑 | 所有訂單 |
| GET | `/api/admin/orders/{id}` | 🔑 | 訂單詳情（Admin） |
| PUT | `/api/admin/orders/{id}/status` | 🔑 | 更新訂單狀態 |
| GET | `/api/addresses` | 🔓 | 地址列表 |
| POST | `/api/addresses` | 🔓 | 新增地址 |
| PUT | `/api/addresses/{id}` | 🔓 | 更新地址 |
| DELETE | `/api/addresses/{id}` | 🔓 | 刪除地址 |
| PUT | `/api/addresses/{id}/default` | 🔓 | 設定預設地址 |
| GET | `/api/admin/dashboard/stats` | 🔑 | 統計數據 |
| GET | `/api/admin/dashboard/revenue` | 🔑 | 營收趨勢 |
| GET | `/api/admin/admin-users` | 🔑 | 管理員列表 |
| POST | `/api/admin/admin-users` | 🔑 | 新增管理員 |
| PUT | `/api/admin/admin-users/{id}` | 🔑 | 更新管理員 |
| DELETE | `/api/admin/admin-users/{id}` | 🔑 | 刪除管理員 |
| POST | `/api/admin/payment-methods/refresh-rates` | 🔑 | 刷新加密貨幣匯率 |
| GET | `/api/orders/shipping-fee` | 🌐 | 查詢運費 |
