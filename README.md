🩺 FamCare: Family Mental Health Tracker

FamCare is a web application designed to help families track emotional well-being, stress levels, and overall mental health. The system allows family members to record daily moods, view analytics, and receive early insights for emotional support — all within a secure, privacy-first environment.

🌟 Features

Role-Based Access
Different views and permissions for parents, children, and therapists.

Mood & Journal Tracking
Each family member can log daily moods or write short emotional entries.

Analytics Dashboard
Visual representation of mood trends and family-wide emotional patterns.

Privacy-Focused
All entries are stored securely with user authentication and access control.

Suggestions & Insights
Automated insights for family intervention and well-being improvement.

🏗️ Tech Stack
Category	Technology
Backend	Spring Boot
Frontend	Thymeleaf (HTML/CSS/JS)
Database	PostgreSQL
APIs	RESTful APIs
Build Tool	Maven
⚙️ Configuration

In your application.properties file:

# Server Port
server.port=8080

# PostgreSQL Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5433/famcare_db
spring.datasource.username=postgres
spring.datasource.password=postgres123
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate (disabled - using JDBC)
spring.jpa.hibernate.ddl-auto=none

# Thymeleaf Configuration
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# Logging
logging.level.root=INFO
logging.level.com.famcare=DEBUG

🚀 Running the Application

Clone this repository:

git clone https://github.com/YOUR-USERNAME/FamCare.git
cd FamCare


Ensure PostgreSQL is running and configured as per your application.properties.

Build and run the project:

mvn spring-boot:run


Visit the app at:
http://localhost:8080

📊 Future Enhancements

AI-based mood prediction and alerts

Mobile-friendly responsive design

Integration with wearable devices

Family counseling recommendations

📄 License

This project is open source and available under the MIT License
.