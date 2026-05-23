# Melodi Instruments

Music Instruments Shop


## Quick start

| Type       | URL |
|------------|------|
| **API**    | http://localhost:8080 |
| **Web UI** | http://localhost:5173 |


# 💠 ERD (to be updated)

![https://i.imgur.com/k4HH5IT.png](https://i.imgur.com/k4HH5IT.png)

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