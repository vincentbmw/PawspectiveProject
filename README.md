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
`
cd backend
`
# Build Docker image
docker build -t pawspective-backend .

# Run container
```
docker run -d \
  --name pawspective-api \
  -p 5000:5000 \
  --env-file .env \
  pawspective-backend
```

###  **Production Deployment**
```
# Using docker-compose (create docker-compose.yml)
docker-compose up -d
```
