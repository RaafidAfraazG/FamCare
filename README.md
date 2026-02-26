# 🏥 FamCare

**FamCare** is a family mental health and wellness web application built with Spring Boot. It enables families to monitor children's emotional well-being through mood tracking, journaling, AI-powered sentiment analysis, family chat, and doctor coordination — all in one platform.

---

## ✨ Features

### 👦 Child Dashboard
- **Mood Logging** — Log daily moods with context and notes
- **Mood History** — View personal mood trends over time
- **Journal** — Create, view, and edit private journal entries
- **Doctor Access** — View assigned family doctors and contact info

### 👨‍👩‍👧 Parent Dashboard
- **Child Analytics** — Visualize children's mood trends with charts
- **Mood Chart** — Interactive mood data visualizations
- **Doctor Management** — Manage the family's doctor relationships
- **Intervention Alerts** — Receive alerts when a child may need support

### 👨‍👩‍👧‍👦 Family Dashboard
- **Family Insights** — AI-generated insights about overall family well-being
- **Child Insights** — Per-child emotional trend analysis
- **Alerts** — View intervention alerts flagged by the system
- **Family Chat** — Real-time family messaging

### 🩺 Doctor Portal
- Accessible by assigned family doctors to review relevant family data

### 🛡️ Admin Panel
- Create and manage users (children, parents, family accounts)
- View and manage all registered users

### 🤖 AI-Powered Services
- **Sentiment Analysis** — Analyzes journal entries and chat messages to detect emotional patterns
- **Intervention Alerts** — Automatically flags concerning sentiment trends
- **Family Insights** — Generates contextual insights from family data

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java, Spring Boot |
| **Frontend** | Thymeleaf, HTML/CSS |
| **Security** | Spring Security |
| **Database** | JPA/Hibernate (configurable) |
| **Build Tool** | Maven |
| **AI/NLP** | Custom Sentiment Analysis Service |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/famcare/
│   │   ├── controller/       # MVC Controllers (Admin, Auth, Child, Parent, Family, Chat, Doctor)
│   │   ├── model/            # Domain models (User, MoodEntry, JournalEntry, ChatMessage, etc.)
│   │   ├── repository/       # Spring Data JPA Repositories
│   │   ├── service/          # Business logic (Mood, Journal, Chat, Insights, Sentiment, Intervention)
│   │   ├── config/           # Security configuration
│   │   └── util/             # Utilities (PasswordEncoder)
│   └── resources/
│       ├── templates/        # Thymeleaf HTML templates
│       ├── static/css/       # Stylesheets
│       └── application.properties
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17+**
- **Maven 3.8+**
- A relational database (MySQL, PostgreSQL, or H2 for development)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/RaafidAfraazG/famcare.git
   cd famcare
   ```

2. **Configure the database**

   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/famcare
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the app**

   Open your browser and navigate to: `http://localhost:8080`

---

## 👥 User Roles

| Role | Description |
|---|---|
| `ADMIN` | Full system access; can create and manage all users |
| `PARENT` | Views child analytics, manages doctors, receives alerts |
| `CHILD` | Logs moods, writes journal entries, views own history |
| `FAMILY` | Accesses family-wide insights, chat, and alerts |
| `DOCTOR` | Views assigned family health data |

---

## 🔐 Security

- Spring Security handles authentication and role-based access control
- Passwords are encoded using a custom `PasswordEncoder` utility
- Each role has restricted access to its own set of routes and dashboards

---

## 📊 Key Models

- **User** — Core user entity with role assignment
- **MoodEntry** — Daily mood logs with timestamp and notes
- **JournalEntry** — Private journal entries per child
- **ChatMessage** — Family chat messages
- **FamilyDoctor** — Doctor-family relationship records
- **FamilyInsight** — AI-generated family wellness insights
- **InterventionAlert** — System-generated alerts for at-risk indicators
- **MoodTrend** — Aggregated mood trend data for analytics

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "Add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## 📬 Contact

For questions or support, please open an issue in the repository or contact the project maintainers.
