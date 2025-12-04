
---

# 🎉 **PAYMENT MICROSERVICE — `payment-svc/README.md`**

```markdown
# 💳 Payment Service — Microservice for Magelan

This microservice handles all payment-related operations for the **Magelan restaurant application**.  
It exposes a simple REST API that simulates a payment provider.

---

## 🚀 Features

- Create a new payment  
- Process (approve/deny) a payment  
- Retrieve payment details  
- Store payment statuses  
- Communicate with Magelan via **OpenFeign**

---

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3**
- **Spring Web**
- **Spring Data JPA**
- **MySQL**
- **OpenFeign** (used by Magelan to call this service)
- **Lombok**

---

## 🔌 REST API Endpoints

### **1️⃣ Create Payment**


### Example Response:
```json
{
  "id": "f23b2af3-8fcc-4e40-b812-5ee5c71d9c11",
  "status": "PENDING",
  "amount": 42.50
}

### 🧪 Testing

- Unit tests for services  
- API tests using MockMvc  
- In-memory H2 database for integration tests  

Part of the Magelan application ecosystem.

