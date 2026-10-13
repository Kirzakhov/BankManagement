# Bank Management

This project contains a Spring Boot API and an Angular frontend.

## Requirements

- Java 21 or later
- Maven
- Node.js and npm

## Start the backend

In PowerShell, set an initial administrator password and start Spring Boot:

```powershell
$env:BANK_ADMIN_PASSWORD = "choose-a-strong-password"
Set-Location .\BankManagement
mvn spring-boot:run
```

The API listens on `http://localhost:8082` and uses an in-memory H2 database.
The initial administrator is created only when `BANK_ADMIN_PASSWORD` is set
and no administrator account already exists.

## Start the frontend

In a second terminal:

```powershell
Set-Location .\BankManagement-UI
npm ci
npm start
```

Open `http://localhost:4200`. The Angular development server proxies API
requests to the backend on port 8082. Sign in as `admin` with the password
set in the backend terminal to manage customer accounts.

## Tests

Run the backend tests from `BankManagement` with `mvn test`. Run the frontend
tests from `BankManagement-UI` with `npm test`.
