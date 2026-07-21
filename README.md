# ReadHub - Peer Lending Library Web Portal

ReadHub is a web-based system designed to allow students to lend and borrow academic materials within their campus community. It promotes collaboration, sharing, and sustainable access to resources.

---

## Tech Stack Used

### Backend (Java API)
* **Framework:** Java 17, Spring Boot 3.2.x, Spring MVC, Spring Data JPA, Spring Security (Stateless JWT Authentication)
* **Database:** H2 Database (In-Memory for Test/Dev Profiles), MySQL (Production Profile)
* **Task Scheduling:** Spring Scheduler + ShedLock (with database lock table configuration)
* **Metrics & Monitoring:** Spring Boot Actuator, Micrometer Prometheus Registry
* **API Documentation:** Springdoc OpenAPI (Swagger UI)
* **Media Handling:** Cloudinary File Storage Service

### Frontend (SPA client)
* **Framework:** React.js, Vite, React Router DOM, Axios
* **Styling:** Vanilla CSS Custom Layouts

---

## Core System Features

1. **JWT User Authentication:** Custom registration & token-based login workflows enforcing STUDENT or ADMIN authority boundaries.
2. **Book Catalog Management:** Search, view, edit, or delete books, filter results by keyword or category tag, and handle uploads.
3. **Borrow Request Lifecycle:**
   * Student submits a borrow request.
   * Admin approves or rejects the request, transitioning states from `REQUESTED` to `APPROVED`/`REJECTED`/`BORROWED`/`RETURNED`.
4. **Reliable Scheduler:** Daily automated task runners query for overdue transactions, issue notifications, and apply database locks.
5. **Secure Avatars:** Custom image file validator verifying mime-types, file traversal blocks, and maximum payload restrictions.

---

## Setup & Execution Instructions

### Prerequisites
* Java JDK 17+ installed
* Node.js v18+ & npm installed

### 1. Backend Setup (Spring Boot)
Change directory to the backend repository:
```bash
cd backend/readhub-book-management-system
```

Verify/run unit and integration tests:
```bash
./mvnw clean test
```

Start the local development server:
```bash
./mvnw spring-boot:run
```
The server will run on `http://localhost:8080`.

### 2. Frontend Setup (React)
Change directory to the frontend repository:
```bash
cd frontend
```

Install packages:
```bash
npm install
```

Start the Vite dev server:
```bash
npm run dev
```
The client will run on `http://localhost:5173`.

---

## Integrations & API Discovery

* **Swagger OpenAPI console:**
  Explore and run requests interactively at: `http://localhost:8080/swagger-ui/index.html`. Authenticate requests using the Bearer JWT token option.
* **Actuator Monitoring Checkpoints:**
  * Health Overview: `http://localhost:8080/actuator/health`
  * Prometheus Performance Metrics: `http://localhost:8080/actuator/prometheus`
* **Postman Collection:**
  The `ReadHub.postman_collection.json` file is located in the workspace root. Import it into Postman to test Auth, Books, Transactions, Users, Dashboard, and Notification requests instantly.

---

## Team Members

* Queddeng, James Adriane -- jamesadriane.queddeng@cit.edu
* Reyes, Amanda Patrice -- amandapatrice.reyes@cit.edu
* Riva, Zendrix -- zendrix.riva@cit.edu
* Rosel, Patricia Mae -- patriciamae.rosel@cit.edu
