<h1 align="center">
  <img src="melodi-web/public/android-chrome-192x192.png" width="56" alt="Melodi logo" valign="middle" />
  Melodi Instruments
</h1>

<p align="center">Music Instruments Shop</p>

<p align="center">
  <a href="https://www.youtube.com/watch?v=iO9lZi0Tevw">
    <img src="https://i.ytimg.com/vi/iO9lZi0Tevw/maxresdefault.jpg" alt="Demo video" width="640" />
  </a>
</p>

## Quick start

| Type       | URL |
|------------|------|
| **API**    | http://localhost:8080 |
| **Web UI** | http://localhost:5173 |


# 🌐 REST API Endpoints
___

## 🔒 Auth

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| POST | `/auth/users/register` | 📝 Register | Public |
| POST | `/auth/users/login` | 🔑 User login | Public |
| GET | `/auth/users/register/verify?token=` | ✉️ Verify email | Public |
| POST | `/auth/users/resetPassword` | 🔁 Request password reset | Public |
| POST | `/auth/users/resetPassword?token=` | 🔐 Activate password reset | Public |
| PUT | `/auth/users/change-password` | 🔒 Change password (logged in) | Private |

## 👤 Profile

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| GET | `/api/profile` | 👤 Get my profile | Private |
| PATCH | `/api/profile` | ✏️ Update profile | Private |
| PUT | `/api/profile/{userId}/image` | 📤 Upload profile image | Private |
| GET | `/api/profile/{userId}/image` | 🖼️ Get profile image | Public |
| DELETE | `/api/profile/{userId}/image` | ❌ Delete profile image | Private |

## 🎸 Categories

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| POST | `/api/categories` | ➕ Create category | Private |
| GET | `/api/categories` | 📃 Get all categories | Public |
| GET | `/api/categories/{id}` | 🔍 Get category by ID | Public |
| PUT | `/api/categories/{id}` | ✏️ Update category | Private |
| DELETE | `/api/categories/{id}` | ❌ Delete category | Private |

## 🏷️ Brands

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| POST | `/api/brands` | ➕ Create brand | Private |
| GET | `/api/brands` | 📃 Get all brands | Public |
| GET | `/api/brands/{id}` | 🔍 Get brand by ID | Public |
| PUT | `/api/brands/{id}` | ✏️ Update brand | Private |
| DELETE | `/api/brands/{id}` | ❌ Delete brand | Private |

## 🎹 Instruments

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| POST | `/api/instruments` | ➕ Create instrument | Private |
| GET | `/api/instruments` | 📃 Get all instruments | Public |
| GET | `/api/instruments?categoryId=` | 🔎 Filter by category | Public |
| GET | `/api/instruments?brandId=` | 🔎 Filter by brand | Public |
| GET | `/api/instruments?status=` | 🔎 Filter by status | Public |
| GET | `/api/instruments/{id}` | 🔍 Get instrument by ID | Public |
| PUT | `/api/instruments/{id}` | ✏️ Update instrument | Private |
| DELETE | `/api/instruments/{id}` | ❌ Delete instrument | Private |
| PUT | `/api/instruments/{id}/image` | 📤 Upload instrument image | Private |
| GET | `/api/instruments/{id}/image` | 🖼️ Get instrument image | Public |
| DELETE | `/api/instruments/{id}/image` | ❌ Delete instrument image | Private |

## 🛒 Cart

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| POST | `/api/cart/items` | ➕ Add to cart (BUY or RENT) | Private |
| GET | `/api/cart` | 📃 List my cart | Private |
| PATCH | `/api/cart/items/{id}` | ✏️ Update cart item | Private |
| DELETE | `/api/cart/items/{id}` | ❌ Remove cart item | Private |
| DELETE | `/api/cart` | 🗑️ Clear cart | Private |

## ❤️ Wishlist

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| POST | `/api/wishlist/items` | ➕ Add to wishlist | Private |
| GET | `/api/wishlist` | 📃 Get my wishlist | Private |
| DELETE | `/api/wishlist/items/{id}` | ❌ Remove from wishlist | Private |

## 📦 Orders

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| POST | `/api/orders/checkout` | 🛍️ Checkout from cart | Private |
| POST | `/api/orders/{id}/confirm-payment` | 💳 Simulate payment + confirm | Private |
| GET | `/api/orders` | 📃 List my orders | Private |
| GET | `/api/orders/{id}` | 🔍 Get order by ID | Private |
| POST | `/api/orders/{id}/cancel` | ↩️ Cancel pending order | Private |

## 🎵 Rentals

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| GET | `/api/rentals` | 📃 List my rentals | Private |
| POST | `/api/rentals/{id}/return` | ↩️ Return rental | Private |

## ⭐ Reviews

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| POST | `/api/instruments/{id}/reviews` | ➕ Create review | Private |
| GET | `/api/instruments/{id}/reviews` | 📃 Get instrument reviews | Public |
| PATCH | `/api/reviews/{id}` | ✏️ Update my review | Private |
| DELETE | `/api/reviews/{id}` | ❌ Delete my review | Private |

## 🛠️ Admin

| Request Type | URL | Functionality | Access |
|--------------|-----|---------------|--------|
| GET | `/api/admin/orders` | 📃 List all orders | Private |
| GET | `/api/admin/orders/{id}` | 🔍 Get order by ID | Private |
| GET | `/api/admin/rentals` | 📃 List all rentals | Private |
| POST | `/api/admin/rentals/{id}/return` | ↩️ Return rental (admin) | Private |
| GET | `/api/admin/users` | 📃 List all users | Private |
| GET | `/api/admin/users/{id}` | 🔍 Get user by ID | Private |
| PATCH | `/api/admin/users/{id}` | ✏️ Update user / soft delete | Private |


# 💠 Entity-Relationship Diagram (ERD)

![https://i.imgur.com/IyHmtO6.png](https://i.imgur.com/IyHmtO6.png)

# 🗓️ Trello

https://trello.com/b/G4XJpa2c/project-4-jdb-melodi

# Backend / Spring Boot Dependencies

- **SDK: Java Oracle OpenJDK (17.0.17)**
- **Maven**
- **Spring Boot: 4.0.6**
- **Spring Web**
- **Spring Data JPA**
- **PostgreSQL Driver**
- **Spring Security**
- **Validation**
- **Java Mail Sender**
- **Thymeleaf**
- **Lombok**
- **Spring Boot DevTools**

# Frontend (Vite + React + TS)
___
```bash
cd melodi-web  
npm install  
npm run dev  
http://localhost:5173/
```

## Demo users (seeded when `users` table is empty)
___
Seeded in **`DataSeeder.java`** only if the **`users`** table has no rows.

| Email                | Password | Role       |
|----------------------|----------|------------|
| `admin@melodi.local` | `admin123` | **ADMIN**  |
| `demo@melodi.local`  | `demo123` | **USER**   |



## Maintenance & Upgrades
___
```bash
cd melodi-web

# Security audit
npm audit
npm audit fix

# See outdated packages (package.json)
npm outdated

# Update/Upgrade packages.
npm update
npm upgrade

# Build
npm run build

# Run Dev
npm run dev
```

## Docs & References links
___

| Tool | URL |
|------|-----|
| **shadcn/ui — Vite install** | https://ui.shadcn.com/docs/installation/vite |
| **shadcn/ui — home** | https://ui.shadcn.com/ |
| **Tailwind CSS v4 + Vite** | https://tailwindcss.com/docs/installation/using-vite |
| **Font Awesome (React)** | https://fontawesome.com/docs/web/use-with/react |
| **Vite** | https://vite.dev/guide/ |
| **React** | https://react.dev/ |
| **React Router** | https://reactrouter.com/ |
| **Spring Boot** | https://spring.io/projects/spring-boot |
| **Spring Boot Initializer** | https://start.spring.io |
| **Spring Security** | https://spring.io/projects/spring-security |
| **Spring Data JPA** | https://spring.io/projects/spring-data-jpa |
| **PostgreSQL** | https://www.postgresql.org/docs/ |
| **jjwt (0.12.x)** | https://github.com/jwtk/jjwt |
| **Thymeleaf (email templates)** | https://www.thymeleaf.org/documentation.html |
| **Postman** | https://www.postman.com/downloads/ |