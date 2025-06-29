package com.ppb.pawspective.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.ppb.pawspective.data.repository.UserRepository
import com.ppb.pawspective.utils.SessionManager
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)
    private val sessionManager = SessionManager(context)
    private val userRepository = UserRepository(context)
    
    // Web client ID for Google Sign-In
    private val webClientId = "834034925173-5hfebg6k85ddast8u7dj0is7j4v149hu.apps.googleusercontent.com"
    
    sealed class AuthResult {
        data class Success(val user: FirebaseUser) : AuthResult()
        data class Error(val message: String) : AuthResult()
        object Loading : AuthResult()
    }
    
    /**
     * Register user with email and password
     */
    suspend fun registerWithEmailAndPassword(email: String, password: String, rememberMe: Boolean = true): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Save user to local database
                userRepository.createUserFromAuth(user.uid, user.email ?: email)
                
                // Create session after successful registration
                sessionManager.createLoginSession(
                    userId = user.uid,
                    email = user.email ?: email,
                    name = user.displayName,
                    loginMethod = SessionManager.LOGIN_METHOD_EMAIL,
                    rememberMe = rememberMe
                )
                AuthResult.Success(user)
            } ?: AuthResult.Error("Registration failed: User creation unsuccessful")
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        } catch (e: Exception) {
            AuthResult.Error("Registration failed: ${e.message}")
        }
    }
    
    /**
     * Sign in user with email and password
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String, rememberMe: Boolean = true): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Save/update user in local database
                userRepository.getUserOrCreate(user.uid, user.email ?: email)
                
                // Create session after successful login
                sessionManager.createLoginSession(
                    userId = user.uid,
                    email = user.email ?: email,
                    name = user.displayName,
                    loginMethod = SessionManager.LOGIN_METHOD_EMAIL,
                    rememberMe = rememberMe
                )
                AuthResult.Success(user)
            } ?: AuthResult.Error("Sign in failed: Authentication unsuccessful")
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        } catch (e: Exception) {
            AuthResult.Error("Sign in failed: ${e.message}")
        }
    }
    
    /**
     * Sign in with Google using Credential Manager
     */
    suspend fun signInWithGoogle(rememberMe: Boolean = true): AuthResult {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
            
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()
            
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )
            
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val googleCredential = GoogleAuthProvider.getCredential(credential.idToken, null)
            
            val authResult = auth.signInWithCredential(googleCredential).await()
            authResult.user?.let { user ->
                // Save/update user in local database
                userRepository.getUserOrCreate(user.uid, user.email ?: "")
                
                // Create session after successful Google sign in
                sessionManager.createLoginSession(
                    userId = user.uid,
                    email = user.email ?: "",
                    name = user.displayName,
                    loginMethod = SessionManager.LOGIN_METHOD_GOOGLE,
                    rememberMe = rememberMe
                )
                AuthResult.Success(user)
            } ?: AuthResult.Error("Google sign in failed: Authentication unsuccessful")
            
        } catch (e: GetCredentialException) {
            AuthResult.Error(getGoogleSignInErrorMessage(e))
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        } catch (e: Exception) {
            AuthResult.Error("Google sign in failed: ${e.message}")
        }
    }
    
    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            AuthResult.Success(auth.currentUser!!) // This is just for success indication
        } catch (e: FirebaseAuthException) {
            AuthResult.Error(getFirebaseAuthErrorMessage(e))
        } catch (e: Exception) {
            AuthResult.Error("Failed to send reset email: ${e.message}")
        }
    }
    
    /**
     * Sign out current user
     */
    fun signOut() {
        auth.signOut()
        sessionManager.logout()
    }
    
    /**
     * Get current user from Firebase Auth
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    /**
     * Check if user is signed in (combines Firebase Auth and SessionManager)
     */
    fun isUserSignedIn(): Boolean {
        val firebaseUser = auth.currentUser
        val sessionValid = sessionManager.isLoggedIn()
        
        return firebaseUser != null && sessionValid
    }
    
    /**
     * Get user session details
     */
    fun getUserSession() = sessionManager.getUserDetails()
    
    /**
     * Update user session data
     */
    fun updateUserSession(name: String? = null, email: String? = null) {
        sessionManager.updateUserSession(name, email)
    }
    
    /**
     * Extend current session (call when user is active)
     */
    fun extendSession() {
        sessionManager.extendSession()
    }
    
    /**
     * Check if session should auto-login
     */
    fun shouldAutoLogin(): Boolean {
        return sessionManager.isLoggedIn() && auth.currentUser != null
    }
    
    /**
     * Get session manager instance
     */
    fun getSessionManager(): SessionManager = sessionManager
    
    /**
     * Convert Firebase Auth error codes to user-friendly messages
     */
    private fun getFirebaseAuthErrorMessage(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "The email address is not valid."
            "ERROR_USER_DISABLED" -> "This account has been disabled."
            "ERROR_USER_NOT_FOUND" -> "No account found with this email address."
            "ERROR_WRONG_PASSWORD" -> "The password is incorrect."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists."
            "ERROR_WEAK_PASSWORD" -> "The password is too weak. Please choose a stronger password."
            "ERROR_OPERATION_NOT_ALLOWED" -> "Email/password accounts are not enabled."
            "ERROR_TOO_MANY_REQUESTS" -> "Too many unsuccessful attempts. Please try again later."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection and try again."
            "ERROR_INVALID_CREDENTIAL" -> "The authentication credential is invalid."
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "An account already exists with the same email but different sign-in credentials."
            "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "This credential is already associated with a different user account."
            "ERROR_USER_TOKEN_EXPIRED" -> "Your session has expired. Please sign in again."
            "ERROR_INVALID_USER_TOKEN" -> "Your authentication token is invalid. Please sign in again."
            "ERROR_REQUIRES_RECENT_LOGIN" -> "This operation requires recent authentication. Please sign in again."
            else -> "Authentication failed: ${exception.message}"
        }
    }
    
    /**
     * Convert Google Sign-In error to user-friendly messages
     */
    private fun getGoogleSignInErrorMessage(exception: GetCredentialException): String {
        return when (exception.type) {
            "android.credentials.GetCredentialException.TYPE_USER_CANCELED" -> "Sign in was cancelled."
            "android.credentials.GetCredentialException.TYPE_NO_CREDENTIAL" -> "No Google account found. Please add a Google account to your device."
            "android.credentials.GetCredentialException.TYPE_INTERRUPTED" -> "Sign in was interrupted. Please try again."
            else -> "Google sign in failed: ${exception.message}"
        }
    }
} 