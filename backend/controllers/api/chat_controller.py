from flask import Blueprint, request, jsonify
from services.firebase_service import firebase_service
from controllers.llm_config import run_query

chat_api = Blueprint('chat_api', __name__)

@chat_api.route('/api/chats/<user_id>', methods=['GET'])
def get_chats(user_id):
    """Get all chats for a specific user with enhanced mobile-friendly data"""
    try:
        chats = firebase_service.get_user_chats(user_id)
        
        # Format response for mobile consumption
        chat_list = []
        for chat in chats:
            # Get last message for preview
            messages = firebase_service.get_chat_messages(user_id, chat['id'])
            last_message = messages[-1] if messages else None
            message_count = len(messages)
            
            # Determine preview text
            if last_message:
                preview_text = last_message['message']
                # Truncate if too long
                if len(preview_text) > 100:
                    preview_text = preview_text[:100] + "..."
            else:
                preview_text = chat.get('first_message', 'New Chat')
            
            chat_list.append({
                'id': chat['id'],
                'title': chat.get('title', 'New Chat'),
                'preview': preview_text,
                'lastMessage': last_message['message'] if last_message else None,
                'lastSender': last_message['sender'] if last_message else None,
                'messageCount': message_count,
                'createdAt': chat['created_at'].isoformat() if chat.get('created_at') else None,
                'updatedAt': chat['updated_at'].isoformat() if chat.get('updated_at') else None
            })
        
        return jsonify({
            'success': True,
            'chats': chat_list,
            'totalChats': len(chat_list)
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@chat_api.route('/api/chats/<user_id>', methods=['POST'])
def create_chat(user_id):
    """Create a new chat for a specific user"""
    try:
        data = request.get_json() or {}
        title = data.get('title', 'New Chat')
        chat_id = firebase_service.create_chat(user_id, title)
        
        return jsonify({
            'success': True,
            'message': 'Chat created successfully',
            'chatId': chat_id
        }), 201
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@chat_api.route('/api/chats/<user_id>/<chat_id>', methods=['DELETE'])
def delete_chat(user_id, chat_id):
    """Delete a specific chat"""
    try:
        success = firebase_service.delete_chat(user_id, chat_id)
        
        if success:
            return jsonify({
                'success': True,
                'message': 'Chat deleted successfully'
            }), 200
        else:
            return jsonify({'error': 'Chat not found'}), 404
            
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@chat_api.route('/api/chats/<user_id>/<chat_id>/messages', methods=['GET'])
def get_chat_messages(user_id, chat_id):
    """Get all messages for a specific chat"""
    try:
        messages = firebase_service.get_chat_messages(user_id, chat_id)
        
        # Format messages for mobile consumption
        formatted_messages = []
        for message in messages:
            formatted_messages.append({
                'id': message['id'],
                'sender': message['sender'],
                'message': message['message'],
                'timestamp': message['timestamp'].isoformat() if message.get('timestamp') else None
            })
        
        return jsonify({
            'success': True,
            'messages': formatted_messages
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@chat_api.route('/api/chats/<user_id>/<chat_id>/messages', methods=['POST'])
def send_message(user_id, chat_id):
    """Send a message to a specific chat"""
    try:
        data = request.get_json()
        
        if not data or 'message' not in data:
            return jsonify({'error': 'Message is required'}), 400
        
        user_message = data['message']
        
        # Process message through LLM and save to Firebase
        response = run_query(user_message, user_id, chat_id)
        
        return jsonify({
            'success': True,
            'message': 'Message sent successfully',
            'response': response['response'],
            'chatId': response['chatId']
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@chat_api.route('/api/query/<user_id>', methods=['POST'])
def process_query(user_id):
    """Process a query (can create new chat or add to existing)"""
    try:
        data = request.get_json()
        
        if not data or 'query' not in data:
            return jsonify({'error': 'Query is required'}), 400
        
        query_text = data['query']
        chat_id = data.get('chatId')  # Optional - if not provided, creates new chat
        
        # Process query through LLM
        response = run_query(query_text, user_id, chat_id)
        
        return jsonify({
            'success': True,
            'response': response['response'],
            'chatId': response['chatId']
        }), 200
        
    except Exception as e:
        return jsonify({'error': str(e)}), 500 