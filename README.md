#  Pawspective

<div align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android">
  <img src="https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin">
  <img src="https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white" alt="Python">
  <img src="https://img.shields.io/badge/Flask-000000?style=for-the-badge&logo=flask&logoColor=white" alt="Flask">
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase">
  <img src="https://img.shields.io/badge/Google%20Gemini-4285F4?style=for-the-badge&logo=google&logoColor=white" alt="Google Gemini">
</div>

<div align="center">
  <h3> AI-Powered Dog Breed Recommendation App</h3>
  <p>An intelligent mobile application that helps users discover the perfect dog breed based on their lifestyle, preferences, and living situation using Google Gemini AI.</p>
</div>

---

##  Table of Contents

- [ Features](#-features)
- [ Tech Stack](#-tech-stack)
- [ Prerequisites](#-prerequisites)
- [ Quick Start](#-quick-start)
- [ Backend Setup](#-backend-setup)
- [ Android Setup](#-android-setup)
- [ Docker Deployment](#-docker-deployment)
- [ API Documentation](#-api-documentation)
- [ Usage Examples](#-usage-examples)
- [ Environment Variables](#-environment-variables)
- [ Contributing](#-contributing)
- [ Troubleshooting](#-troubleshooting)
- [ License](#-license)

---

##  Features

###  Core Features
- **AI-Powered Recommendations**: Intelligent dog breed suggestions using Google Gemini AI
- **Interactive Chat Interface**: Natural conversation flow for personalized recommendations
- **User Authentication**: Secure login with Email/Password and Google Sign-In
- **Profile Management**: Customizable user profiles with photo upload
- **Chat History**: Save and manage conversation history
- **Dark/Light Theme**: Adaptive UI theme support
- **Offline Support**: Local data caching for better performance

###  Security Features
- Firebase Authentication integration
- Secure API endpoints with user validation
- Environment-based configuration
- Data encryption and secure storage

###  UI/UX Features
- Modern Material Design 3 interface
- Smooth animations and transitions
- Responsive design for various screen sizes
- Intuitive onboarding experience
- Accessibility support

---

##  Tech Stack

###  **Frontend (Android)**
| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | Latest | Primary programming language |
| **Jetpack Compose** | Latest | Modern UI toolkit |
| **Material Design 3** | Latest | Design system |
| **Firebase Auth** | 23.2.1 | Authentication |
| **Retrofit** | 2.9.0 | HTTP client |
| **Room Database** | Latest | Local database |
| **Coil** | 2.5.0 | Image loading |
| **Coroutines** | 1.7.3 | Asynchronous programming |

###  **Backend (API)**
| Technology | Version | Purpose |
|------------|---------|---------|
| **Python** | 3.9+ | Backend language |
| **Flask** | Latest | Web framework |
| **Google Gemini AI** | Latest | LLM |
| **Firebase Admin SDK** | Latest | Database & auth |
| **Flask-CORS** | Latest | Cross-origin requests |
| **python-dotenv** | Latest | Environment management |

###  **Services & Infrastructure**
- **Firebase Firestore** - NoSQL database
- **Firebase Authentication** - User management
- **Firebase Storage** - File storage
- **Google Gemini AI** - Natural language processing
- **Docker** - Containerization

---

##  Prerequisites

###  **Development Environment**
- **Python 3.9+** with pip
- **Android Studio Arctic Fox** or newer
- **JDK 11** or newer
- **Git** for version control

###  **External Services**
- **Firebase Project** with Firestore and Authentication enabled
- **Google Cloud Project** with Gemini AI API access
- **Google API Key** for Gemini AI

###  **Android Development**
- **Android SDK API 24+** (Android 7.0)
- **Target SDK 35** (Android 15)
- **Gradle 8.0+**

---

##  Quick Start

### 1 **Clone Repository**
`
git clone https://github.com/your-username/pawspective.git
cd pawspective
`

### 2 **Backend Setup**
```
cd backend
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env  # Configure your environment variables
python app.py
```

### 3 **Android Setup**
```
cd Pawspective
# Open in Android Studio and sync project
# Configure Firebase (see detailed setup below)
# Run the app
```

---

##  Backend Setup

### 1 **Create Virtual Environment**
```
cd backend
python -m venv venv

# Activate virtual environment
# On Windows:
venv\Scripts\activate
# On macOS/Linux:
source venv/bin/activate
```

### 2 **Install Dependencies**
`
pip install -r requirements.txt
`

### 3 **Configure Environment Variables**
Create .env file in the ackend directory:
`env
# Google Gemini AI
GOOGLE_API_KEY=your_google_api_key_here

# Firebase Configuration
FIREBASE_PROJECT_ID=your_firebase_project_id
FIREBASE_SERVICE_ACCOUNT_PATH=./firebase-service-account.json
`

### 4 **Setup Firebase Service Account**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project  Project Settings  Service Accounts
3. Generate new private key
4. Save as firebase-service-account.json in backend directory

### 5 **Run Backend Server**
`
python app.py
`

The API will be available at http://localhost:5000

### 6 **Verify Installation**
`
curl http://localhost:5000/api/status
`

---

##  Android Setup

### 1 **Open in Android Studio**
` cd Pawspective `
# Open this directory in Android Studio

### 2 **Configure Firebase**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Add Android app to your project
3. Download google-services.json
4. Place it in Pawspective/app/ directory

### 3 **Enable Firebase Services**
In Firebase Console, enable:
- **Authentication** (Email/Password & Google Sign-In)
- **Firestore Database**
- **Storage** (for profile pictures)

### 4 **Configure Google Sign-In**
1. In Firebase Console  Authentication  Sign-in method
2. Enable Google Sign-In
3. Add your SHA-1 fingerprint:

### 5 **Update API Base URL**
In app/src/main/java/com/ppb/pawspective/data/api/ApiClient.kt:
`kotlin
private const val BASE_URL = "http://your-backend-url:5000/"
`

### 6 **Build and Run**
`
./gradlew assembleDebug # Or use Android Studio's Run button
`

---

##  Docker Deployment

###  **Backend Deployment**
`ash
cd backend

# Build Docker image
docker build -t pawspective-backend .

# Run container
docker run -d \
  --name pawspective-api \
  -p 5000:5000 \
  --env-file .env \
  pawspective-backend
`

###  **Production Deployment**
`ash
# Using docker-compose (create docker-compose.yml)
docker-compose up -d
`

---

##  API Documentation

###  **Health & Status**

#### GET /
Health check endpoint
`json
{
  "status": "healthy",
  "message": "Tubes-RPLL Dog Breed Recommendation API",
  "version": "3.0.0"
}
`

#### GET /api/status
Detailed API status
`json
{
  "api_status": "operational",
  "firebase_status": "connected",
  "ai_model": "Google Gemini 1.5 Flash",
  "ai_status": "operational"
}
`

###  **User Management**

#### GET /api/user/{user_id}/profile
Get user profile
`json
{
  "success": true,
  "profile": {
    "id": "user123",
    "nickname": "John Doe",
    "fullname": "John Doe",
    "email": "john@example.com",
    "profile_picture": "https://...",
    "login_provider": "password",
    "can_change_password": true
  }
}
`

#### PUT /api/user/{user_id}/profile
Update user profile
`json
{
  "nickname": "New Nickname",
  "fullname": "New Full Name",
  "email": "new@example.com"
}
`

#### POST /api/user/{user_id}/profile/picture
Upload profile picture
`json
{
  "image": "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQ..."
}
`

###  **Chat & AI**

#### GET /api/chats/{user_id}
Get user's chat history
`json
{
  "success": true,
  "chats": [
    {
      "id": "chat123",
      "title": "Dog Breed Recommendation",
      "preview": "I'm looking for a family-friendly dog...",
      "messageCount": 5,
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ]
}
`

#### POST /api/query/{user_id}
Send message to AI
`json
{
  "query": "I'm looking for a dog breed suitable for apartment living",
  "chatId": "chat123"  // Optional
}
`

Response:
`json
{
  "success": true,
  "response": "Based on your apartment living situation, I'd recommend considering breeds like French Bulldogs, Cavalier King Charles Spaniels, or Boston Terriers...",
  "chatId": "chat123"
}
`

###  **Testing**

#### POST /api/test-ai
Test AI functionality
`json
{
  "query": "What's the best dog breed for families?"
}
`

---

##  Usage Examples

###  **API Usage**

#### Python Example
`python
import requests

# Test AI endpoint
response = requests.post(
    'http://localhost:5000/api/test-ai',
    json={'query': 'Best dog breed for apartments?'}
)
print(response.json())
`

#### cURL Example
`ash
# Get user profile
curl -X GET \
  'http://localhost:5000/api/user/user123/profile' \
  -H 'Content-Type: application/json'

# Send AI query
curl -X POST \
  'http://localhost:5000/api/query/user123' \
  -H 'Content-Type: application/json' \
  -d '{
    "query": "I want a dog that is good with children and doesn'\''t shed much"
  }'
`

###  **Android Usage**

#### Making API Calls
`kotlin
// In your repository or service class
class PawspectiveRepository {
    private val apiService = ApiClient.create()
    
    suspend fun sendQuery(userId: String, query: String): ApiResponse<QueryResponse> {
        return try {
            val response = apiService.sendQuery(userId, QueryRequest(query))
            ApiResponse.Success(response)
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Unknown error")
        }
    }
}
`

---

##  Environment Variables

###  **Backend (.env)**
`env
# Required - Google Gemini AI API Key
GOOGLE_API_KEY=your_google_api_key_here

# Required - Firebase Configuration
FIREBASE_PROJECT_ID=your_firebase_project_id
FIREBASE_SERVICE_ACCOUNT_PATH=./firebase-service-account.json

# Optional - Development
DEBUG=true
FLASK_ENV=development
`

###  **Android Configuration**
Configuration is handled through:
- google-services.json - Firebase configuration
- local.properties - SDK paths and API keys
- Build configuration in uild.gradle.kts

---

##  Contributing

We welcome contributions! Please follow these guidelines:

###  **Development Workflow**
1. **Fork** the repository
2. **Create** a feature branch: git checkout -b feature/amazing-feature
3. **Commit** your changes: git commit -m 'Add amazing feature'
4. **Push** to the branch: git push origin feature/amazing-feature
5. **Open** a Pull Request

###  **Code Standards**
- **Python**: Follow PEP 8 style guide
- **Kotlin**: Follow official Kotlin coding conventions
- **Commits**: Use conventional commit messages
- **Documentation**: Update README for new features

###  **Testing**
`ash
# Backend tests
cd backend
python -m pytest

# Android tests
cd Pawspective
./gradlew test
`

###  **Pull Request Checklist**
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Tests added/updated
- [ ] Documentation updated
- [ ] No breaking changes (or clearly documented)

---

##  Troubleshooting

###  **Common Backend Issues**

#### Firebase Connection Error
`ash
# Check if firebase-service-account.json exists and is valid
ls -la backend/firebase-service-account.json

# Verify Firebase project ID in .env
cat backend/.env | grep FIREBASE_PROJECT_ID
`

#### Google API Key Issues
`ash
# Verify API key is set
echo 

# Test API key with curl
curl -H "Authorization: Bearer " \
  "https://generativelanguage.googleapis.com/v1/models"
`

#### Port Already in Use
`ash
# Find process using port 5000
lsof -i :5000

# Kill process (replace PID)
kill -9 <PID>
`

###  **Common Android Issues**

#### Build Errors
`ash
# Clean and rebuild
./gradlew clean
./gradlew build

# Clear Android Studio cache
# File  Invalidate Caches and Restart
`

#### Firebase Authentication Issues
1. Verify google-services.json is in correct location
2. Check SHA-1 fingerprint is added to Firebase Console
3. Ensure Authentication is enabled in Firebase Console

#### Network Connection Issues
1. Check if backend server is running
2. Verify API base URL in ApiClient.kt
3. Test network connectivity:
`kotlin
// Add network security config in AndroidManifest.xml
android:networkSecurityConfig="@xml/network_security_config"
`

###  **Debug Mode**

#### Enable Debug Logging
Backend:
`python
# In app.py
import logging
logging.basicConfig(level=logging.DEBUG)
`

Android:
`kotlin
// In build.gradle.kts
buildTypes {
    debug {
        isDebuggable = true
        buildConfigField("boolean", "DEBUG_MODE", "true")
    }
}
`

---

##  Support & Contact

###  **Getting Help**
- **Issues**: [GitHub Issues](https://github.com/your-username/pawspective/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-username/pawspective/discussions)
- **Email**: support@pawspective.com

###  **Resources**
- [Firebase Documentation](https://firebase.google.com/docs)
- [Google Gemini AI Documentation](https://ai.google.dev/docs)
- [Android Developer Guide](https://developer.android.com)
- [Flask Documentation](https://flask.palletsprojects.com/)

---

##  License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

`
MIT License

Copyright (c) 2024 Pawspective Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
`

---

<div align="center">
  <h3> Made with  for Dog Lovers</h3>
  <p>
    <a href="#-pawspective">Back to Top</a> 
    <a href="https://github.com/your-username/pawspective">GitHub</a> 
    <a href="mailto:support@pawspective.com">Contact</a>
  </p>
</div>
