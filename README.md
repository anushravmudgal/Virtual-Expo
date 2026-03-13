# 🚀 Virtual-Expo: AI-Driven Platform as a Service (PaaS)

An enterprise-grade, cloud-native Platform as a Service (PaaS) that automates the deployment of GitHub repositories using Generative AI. 

Instead of writing manual configuration files, Virtual-Expo utilizes the **Gemini AI API** to dynamically analyze project structures, generate optimized containerization scripts, and execute zero-touch deployments via Docker.

## 🏗️ How It Works (The Architecture Flow)
1. **Repository Ingestion:** The user provides a public GitHub repository URL via the frontend interface.
2. **Codebase Analysis:** The Spring Boot backend fetches and parses the repository's directory structure and dependency files (e.g., `package.json`, `pom.xml`, `requirements.txt`).
3. **AI Infrastructure Generation:** The system feeds the project context into the **Gemini API**, which dynamically generates a custom, highly optimized `docker-compose.yml` file tailored to the application's specific needs.
4. **Containerized Deployment:** The backend engine executes the AI-generated Docker commands, spinning up isolated, secure containers for the application.

## 🛠️ Tech Stack
* **Backend:** Java, Spring Boot, Spring Web
* **Artificial Intelligence:** Gemini API (Generative AI for Infrastructure as Code)
* **Containerization & DevOps:** Docker, Docker Compose
* **Frontend:** TypeScript, HTML/CSS
* **Version Control Integration:** GitHub API

## ⚙️ Local Setup & Installation

### Prerequisites
* Java 17 or higher
* Docker and Docker Compose installed and running
* A valid Google Gemini API Key

### Configuration
1. Clone the repository:
   ```bash
   git clone [https://github.com/anushravmudgal/Virtual-Expo.git](https://github.com/anushravmudgal/Virtual-Expo.git)
   ```
2. Navigate to the backend directory and configure your environment variables. You must provide your own Gemini API key. Create an `application-secret.properties` file (or set environment variables) with:
   ```properties
   gemini.api.key=YOUR_GEMINI_API_KEY
   ```
   *(Note: Never commit your actual API keys to version control).*

### Running the Application
1. Build and run the Spring Boot backend.
2. Ensure Docker Desktop (or your Docker daemon) is actively running in the background.
3. Access the client interface to submit a GitHub repository for AI-driven deployment.

## 👨‍💻 About the Developer
Architected and developed by **Anushrav Mudgal**. 
Focused on building scalable Java/Spring Boot microservices and integrating modern cloud-native technologies. 

🔗 [Connect with me on LinkedIn](https://www.linkedin.com/in/anushrav-mudgal/)
