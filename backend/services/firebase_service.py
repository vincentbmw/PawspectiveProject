import os
import firebase_admin
from firebase_admin import credentials, firestore, auth, storage
from datetime import datetime
from typing import Optional, Dict, List, Any
from dotenv import load_dotenv, find_dotenv, dotenv_values

load_dotenv()

class FirebaseService:
    _instance = None
    _db = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(FirebaseService, cls).__new__(cls)
            cls._instance._initialize()
        return cls._instance
    
    def _initialize(self):
        """Initialize Firebase Admin SDK"""
        # Load configuration
        config = dotenv_values(find_dotenv())
        
        if not firebase_admin._apps:
            # Get service account path
            service_account_path = config.get('FIREBASE_SERVICE_ACCOUNT_PATH', './firebase-service-account.json')
            
            if not os.path.exists(service_account_path):
                raise Exception(f"Firebase service account file not found: {service_account_path}")
            
            cred = credentials.Certificate(service_account_path)
            firebase_admin.initialize_app(cred, {
                'storageBucket': config.get('FIREBASE_PROJECT_ID', 'pawspective-2fe3c') + '.appspot.com'
            })
        
        self._db = firestore.client()
        
        # Initialize Storage
        self.storage_bucket = storage.bucket()
        
        print("Firebase initialized successfully!")
    
    @property
    def db(self):
        """Get Firestore database instance"""
        return self._db
    
    # User Management Methods
    def create_user(self, user_data: Dict[str, Any]) -> str:
        """Create a new user in Firestore"""
        try:
            user_data['created_at'] = datetime.now()
            user_data['updated_at'] = datetime.now()
            
            doc_ref = self._db.collection('users').add(user_data)
            return doc_ref[1].id
        except Exception as e:
            print(f"Error creating user: {str(e)}")
            return None
    
    def get_user_by_email(self, email: str) -> Optional[Dict[str, Any]]:
        """Get user by email"""
        try:
            users_ref = self._db.collection('users')
            query = users_ref.where('email', '==', email).limit(1)
            docs = query.stream()
            
            for doc in docs:
                user_data = doc.to_dict()
                user_data['id'] = doc.id
                return user_data
            
            return None
        except Exception as e:
            raise Exception(f"Error getting user by email: {str(e)}")
    
    def get_user_by_id(self, user_id: str) -> Optional[Dict[str, Any]]:
        """Get user by ID"""
        try:
            doc_ref = self._db.collection('users').document(user_id)
            doc = doc_ref.get()
            
            if doc.exists:
                user_data = doc.to_dict()
                user_data['id'] = doc.id
                return user_data
            
            return None
        except Exception as e:
            print(f"Error getting user: {str(e)}")
            return None
    
    def update_user(self, user_id: str, update_data: Dict[str, Any]) -> bool:
        """Update user data"""
        try:
            update_data['updated_at'] = datetime.now()
            
            doc_ref = self._db.collection('users').document(user_id)
            doc_ref.update(update_data)
            return True
        except Exception as e:
            print(f"Error updating user: {str(e)}")
            return False
    
    # Chat Management Methods
    def create_chat(self, user_id: str, title: str = None) -> str:
        """Create a new chat for a user"""
        try:
            chat_data = {
                'title': title or 'New Chat',
                'first_message': title or 'New Chat',
                'created_at': datetime.now(),
                'updated_at': datetime.now()
            }
            
            doc_ref = self._db.collection('users').document(user_id).collection('chats').add(chat_data)
            return doc_ref[1].id
        except Exception as e:
            print(f"Error creating chat: {str(e)}")
            return None
    
    def get_user_chats(self, user_id: str) -> List[Dict[str, Any]]:
        """Get all chats for a user"""
        try:
            chats_ref = self._db.collection('users').document(user_id).collection('chats')
            docs = chats_ref.order_by('updated_at', direction=firestore.Query.DESCENDING).stream()
            
            chats = []
            for doc in docs:
                chat_data = doc.to_dict()
                chat_data['id'] = doc.id
                chats.append(chat_data)
            
            return chats
        except Exception as e:
            print(f"Error getting chats: {str(e)}")
            return []
    
    def delete_chat(self, user_id: str, chat_id: str) -> bool:
        """Delete a chat and all its messages"""
        try:
            # Delete all messages in the chat
            messages_ref = self._db.collection('users').document(user_id).collection('chats').document(chat_id).collection('messages')
            messages = messages_ref.stream()
            
            for message in messages:
                message.reference.delete()
            
            # Delete the chat
            chat_ref = self._db.collection('users').document(user_id).collection('chats').document(chat_id)
            chat_ref.delete()
            
            return True
        except Exception as e:
            print(f"Error deleting chat: {str(e)}")
            return False
    
    # Message Management Methods
    def add_message(self, user_id: str, chat_id: str, sender: str, message: str) -> bool:
        """Add a message to a chat"""
        try:
            message_data = {
                'sender': sender,  # 'user' or 'bot'
                'message': message,
                'timestamp': datetime.now()
            }
            
            # Add message to chat
            messages_ref = self._db.collection('users').document(user_id).collection('chats').document(chat_id).collection('messages')
            messages_ref.add(message_data)
            
            # Update chat's updated_at timestamp
            chat_ref = self._db.collection('users').document(user_id).collection('chats').document(chat_id)
            chat_ref.update({'updated_at': datetime.now()})
            
            return True
        except Exception as e:
            print(f"Error adding message: {str(e)}")
            return False
    
    def get_chat_messages(self, user_id: str, chat_id: str) -> List[Dict[str, Any]]:
        """Get all messages for a chat"""
        try:
            messages_ref = self._db.collection('users').document(user_id).collection('chats').document(chat_id).collection('messages')
            docs = messages_ref.order_by('timestamp').stream()
            
            messages = []
            for doc in docs:
                message_data = doc.to_dict()
                message_data['id'] = doc.id
                messages.append(message_data)
            
            return messages
        except Exception as e:
            print(f"Error getting messages: {str(e)}")
            return []
    
    # Feedback Management
    def save_user_feedback(self, user_id: str, feedback: str) -> bool:
        """Save user feedback"""
        try:
            feedback_data = {
                'user_id': user_id,
                'feedback': feedback,
                'timestamp': datetime.now()
            }
            
            self._db.collection('feedback').add(feedback_data)
            return True
        except Exception as e:
            print(f"Error saving feedback: {str(e)}")
            return False
    
    def get_user_provider_info(self, user_id: str) -> Optional[str]:
        """Get user's authentication provider (password, google.com, etc.)"""
        try:
            user_record = auth.get_user(user_id)
            if user_record.provider_data:
                # Return the first provider (usually the primary one)
                return user_record.provider_data[0].provider_id
            return None
        except Exception as e:
            print(f"Error getting user provider info: {str(e)}")
            return None
    
    def change_user_password(self, user_id: str, new_password: str) -> bool:
        """Change user password (only for email/password users)"""
        try:
            auth.update_user(user_id, password=new_password)
            return True
        except Exception as e:
            print(f"Error changing password: {str(e)}")
            return False
    
    # Authentication Helper Methods
    def verify_firebase_token(self, id_token: str) -> Optional[Dict[str, Any]]:
        """Verify Firebase ID token and return user info"""
        try:
            decoded_token = auth.verify_id_token(id_token)
            return decoded_token
        except Exception as e:
            raise Exception(f"Error verifying token: {str(e)}")

# Singleton instance
firebase_service = FirebaseService() 