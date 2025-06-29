from flask import Blueprint, request, jsonify
from services.firebase_service import firebase_service
import base64
import uuid
from datetime import datetime

user_api = Blueprint('user_api', __name__)

@user_api.route('/api/user/<user_id>/profile', methods=['GET'])
def get_profile(user_id):
    """Get user's profile by user_id"""
    try:
        user = firebase_service.get_user_by_id(user_id)
        
        if not user:
            return jsonify({'error': 'User not found'}), 404
        
        # Get user's login provider info
        provider = firebase_service.get_user_provider_info(user_id)
        
        profile_data = {
            'id': user['id'],
            'nickname': user.get('nickname', ''),
            'fullname': user.get('fullname', ''),
            'email': user['email'],
            'profile_picture': user.get('profile_picture', 'https://firebasestorage.googleapis.com/v0/b/gotravel-9fad0.appspot.com/o/profile_pictures%2Fmale.png?alt=media&token=ed087933-e6cb-4781-b952-67cdf37b8dad'),
            'login_provider': provider,
            'can_change_password': provider == 'password',  # Only email/password users can change password
            'created_at': user.get('created_at').isoformat() if user.get('created_at') else None,
            'updated_at': user.get('updated_at').isoformat() if user.get('updated_at') else None
        }
        
        return jsonify({
            'success': True,
            'profile': profile_data
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@user_api.route('/api/user/<user_id>/profile', methods=['PUT'])
def update_profile(user_id):
    """Update user's profile (nickname, fullname, email, profile_picture)"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({'error': 'No data provided'}), 400
        
        # Allowed fields for update
        allowed_fields = ['nickname', 'fullname', 'email', 'profile_picture']
        update_data = {}
        
        for field in allowed_fields:
            if field in data:
                update_data[field] = data[field]
        
        if not update_data:
            return jsonify({'error': 'No valid fields to update'}), 400
        
        success = firebase_service.update_user(user_id, update_data)
        
        if success:
            # Get updated user data
            updated_user = firebase_service.get_user_by_id(user_id)
            
            return jsonify({
                'success': True,
                'message': 'Profile updated successfully',
                'profile': {
                    'id': updated_user['id'],
                    'nickname': updated_user.get('nickname', ''),
                    'fullname': updated_user.get('fullname', ''),
                    'email': updated_user['email'],
                    'profile_picture': updated_user.get('profile_picture', 'https://firebasestorage.googleapis.com/v0/b/gotravel-9fad0.appspot.com/o/profile_pictures%2Fmale.png?alt=media&token=ed087933-e6cb-4781-b952-67cdf37b8dad')
                }
            }), 200
        else:
            return jsonify({'error': 'Failed to update profile'}), 500
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@user_api.route('/api/user/<user_id>/profile/picture', methods=['POST'])
def upload_profile_picture(user_id):
    """Upload custom profile picture"""
    try:
        data = request.get_json()
        
        if not data or 'image' not in data:
            return jsonify({'error': 'Image data is required'}), 400
        
        # Get base64 image data
        image_data = data['image']
        
        # Validate base64 format
        if not image_data.startswith('data:image/'):
            return jsonify({'error': 'Invalid image format. Must be base64 with data:image/ prefix'}), 400
        
        # Extract image type and base64 data
        try:
            header, base64_data = image_data.split(',', 1)
            image_type = header.split('/')[1].split(';')[0]  # jpeg, png, etc
            
            # Validate image type
            if image_type not in ['jpeg', 'jpg', 'png', 'webp']:
                return jsonify({'error': 'Unsupported image type. Use JPEG, PNG, or WebP'}), 400
            
            # Decode base64
            image_bytes = base64.b64decode(base64_data)
            
            # Check file size (max 5MB)
            if len(image_bytes) > 5 * 1024 * 1024:
                return jsonify({'error': 'Image too large. Maximum size is 5MB'}), 400
            
        except Exception:
            return jsonify({'error': 'Invalid base64 image data'}), 400
        
        # Upload to Firebase Storage
        try:
            # Generate unique filename
            filename = f"profile_pictures/{user_id}_{uuid.uuid4().hex}.{image_type}"
            
            # Upload to Firebase Storage
            bucket = firebase_service.storage_bucket
            blob = bucket.blob(filename)
            blob.upload_from_string(image_bytes, content_type=f'image/{image_type}')
            
            # Make the file publicly accessible
            blob.make_public()
            
            # Get the public URL
            profile_picture_url = blob.public_url
            
            # Update user profile with new picture URL
            update_data = {'profile_picture': profile_picture_url}
            success = firebase_service.update_user(user_id, update_data)
            
            if success:
                return jsonify({
                    'success': True,
                    'message': 'Profile picture uploaded successfully',
                    'profile_picture_url': profile_picture_url
                }), 200
            else:
                return jsonify({'error': 'Failed to update profile with new picture'}), 500
                
        except Exception as e:
            return jsonify({'error': f'Failed to upload image: {str(e)}'}), 500
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@user_api.route('/api/user/<user_id>/change-password', methods=['PUT'])
def change_password(user_id):
    """Change user password (only for email/password users)"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({'error': 'No data provided'}), 400
        
        if 'new_password' not in data:
            return jsonify({'error': 'New password is required'}), 400
        
        new_password = data['new_password']
        
        # Validate password strength
        if len(new_password) < 6:
            return jsonify({'error': 'Password must be at least 6 characters long'}), 400
        
        # Check if user can change password (only email/password users)
        provider = firebase_service.get_user_provider_info(user_id)
        
        if provider != 'password':
            return jsonify({
                'error': 'Password change not available for Google Sign-in users',
                'login_provider': provider
            }), 400
        
        # Change password using Firebase Admin
        success = firebase_service.change_user_password(user_id, new_password)
        
        if success:
            return jsonify({
                'success': True,
                'message': 'Password changed successfully'
            }), 200
        else:
            return jsonify({'error': 'Failed to change password'}), 500
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@user_api.route('/api/user/<user_id>/profile', methods=['POST'])
def create_profile(user_id):
    """Create a new user profile"""
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({'error': 'No data provided'}), 400
        
        # Required fields for user creation
        required_fields = ['nickname', 'fullname', 'email']
        if not all(field in data for field in required_fields):
            return jsonify({'error': 'Missing required fields: nickname, fullname, email'}), 400
        
        # Set default profile picture
        default_profile_picture = 'https://firebasestorage.googleapis.com/v0/b/gotravel-9fad0.appspot.com/o/profile_pictures%2Fmale.png?alt=media&token=ed087933-e6cb-4781-b952-67cdf37b8dad'
        
        # Create user data
        user_data = {
            'nickname': data['nickname'],
            'fullname': data['fullname'],
            'email': data['email'],
            'profile_picture': data.get('profile_picture', default_profile_picture)
        }
        
        # Create user with specific ID
        doc_ref = firebase_service.db.collection('users').document(user_id)
        doc_ref.set(user_data)
        
        return jsonify({
            'success': True,
            'message': 'Profile created successfully',
            'user_id': user_id,
            'profile': user_data
        }), 201
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@user_api.route('/api/user/<user_id>/feedback', methods=['POST'])
def save_feedback(user_id):
    """Save user feedback"""
    try:
        data = request.get_json()
        
        if not data or 'feedback' not in data:
            return jsonify({'error': 'Feedback is required'}), 400
        
        feedback = data['feedback']
        
        if not feedback.strip():
            return jsonify({'error': 'Feedback cannot be empty'}), 400
        
        success = firebase_service.save_user_feedback(user_id, feedback)
        
        if success:
            return jsonify({
                'success': True,
                'message': 'Feedback saved successfully'
            }), 200
        else:
            return jsonify({'error': 'Failed to save feedback'}), 500
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@user_api.route('/api/user/<user_id>/stats', methods=['GET'])
def get_user_stats(user_id):
    """Get user statistics (chat count, etc.)"""
    try:
        chats = firebase_service.get_user_chats(user_id)
        
        # Calculate total messages
        total_messages = 0
        for chat in chats:
            messages = firebase_service.get_chat_messages(user_id, chat['id'])
            total_messages += len(messages)
        
        stats = {
            'total_chats': len(chats),
            'total_messages': total_messages,
            'latest_chat': chats[0] if chats else None
        }
        
        return jsonify({
            'success': True,
            'stats': stats
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500 