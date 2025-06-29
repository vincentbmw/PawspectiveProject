import os
from flask import Flask, jsonify, request
from dotenv import find_dotenv, dotenv_values
from flask_cors import CORS

# Import API controllers
from controllers.api.chat_controller import chat_api
from controllers.api.user_controller import user_api

# Import services
from services.firebase_service import firebase_service
from controllers.llm_config import setup_llm

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Register API blueprints
app.register_blueprint(chat_api)
app.register_blueprint(user_api)

@app.route('/')
def health_check():
    """API health check endpoint"""
    return jsonify({
        'status': 'healthy',
        'message': 'Tubes-RPLL Dog Breed Recommendation API',
        'version': '3.0.0',
        'ai_model': 'Google Gemini (Pure AI - No RAG)',
        'note': 'Authentication handled by Android client with Firebase Auth',
        'endpoints': {
            'user_profile': [
                'GET /api/user/{user_id}/profile',
                'PUT /api/user/{user_id}/profile',
                'POST /api/user/{user_id}/profile',
                'POST /api/user/{user_id}/profile/picture',
                'DELETE /api/user/{user_id}/profile/picture',
                'POST /api/user/{user_id}/feedback',
                'GET /api/user/{user_id}/stats'
            ],
            'chats': [
                'GET /api/chats/{user_id}',
                'POST /api/chats/{user_id}',
                'DELETE /api/chats/{user_id}/{chat_id}',
                'GET /api/chats/{user_id}/{chat_id}/messages',
                'POST /api/chats/{user_id}/{chat_id}/messages',
                'POST /api/query/{user_id}'
            ],
            'test': [
                'POST /api/test-ai'
            ]
        }
    }), 200

@app.route('/api/test-ai', methods=['POST'])
def test_ai():
    """Test AI endpoint untuk testing Gemini AI"""
    try:
        data = request.get_json()
        if not data or 'query' not in data:
            return jsonify({'error': 'Query is required'}), 400
        
        query = data['query']
        
        # Import run_query function
        from controllers.llm_config import run_query
        
        # Process query (tanpa user_id dan chat_id untuk testing)
        response = run_query(query, 'test_user', None)
        
        return jsonify({
            'success': True,
            'query': query,
            'response': response['response'],
            'chatId': response['chatId']
        }), 200

    except Exception as e:
        return jsonify({'error': str(e)}), 500
    
@app.route('/api/status', methods=['GET'])
def api_status():
    """Detailed API status for monitoring"""
    try:
        # Test Firebase connection
        firebase_status = "connected"
        try:
            firebase_service.db.collection('test').limit(1).get()
        except Exception:
            firebase_status = "disconnected"
        
        return jsonify({
            'api_status': 'operational',
            'firebase_status': firebase_status,
            'ai_model': 'Google Gemini 1.5 Flash',
            'ai_status': 'operational' if 'model' in globals() else 'not_initialized',
            'authentication': 'handled_by_android_firebase_auth',
            'rag_status': 'disabled - pure AI mode'
        }), 200

    except Exception as e:
        return jsonify({
            'api_status': 'error',
            'error': str(e)
        }), 500

@app.errorhandler(404)
def not_found(error):
    """Handle 404 errors with JSON response"""
    return jsonify({
        'error': 'Endpoint not found',
        'message': 'The requested API endpoint does not exist'
    }), 404

@app.errorhandler(405)
def method_not_allowed(error):
    """Handle 405 errors with JSON response"""
    return jsonify({
        'error': 'Method not allowed',
        'message': 'The HTTP method is not allowed for this endpoint'
    }), 405

@app.errorhandler(500)
def internal_error(error):
    """Handle 500 errors with JSON response"""
    return jsonify({
        'error': 'Internal server error',
        'message': 'An unexpected error occurred'
    }), 500

# Add healthcheck endpoint
@app.route('/health')
def health_check():
    return jsonify({"status": "healthy"}), 200

if __name__ == '__main__':
    try:
        print("🚀 Starting Tubes-RPLL API Server...")
        
        # Initialize Firebase
        print("📱 Initializing Firebase...")
        firebase_service  # This triggers Firebase initialization
        print("✅ Firebase initialized successfully!")
        
        # Initialize Gemini AI
        print("🤖 Setting up Gemini AI...")
        google_api_key = os.environ.get('GOOGLE_API_KEY')
        if not google_api_key:
            raise Exception("GOOGLE_API_KEY not found in environment variables")
        
        setup_llm(google_api_key)
        print("✅ Gemini AI setup completed!")
        
        print("🎉 All services initialized successfully!")
        print("📡 API Server running on http://localhost:5000")
        print("📚 API Documentation available at http://localhost:5000")
        print("🔐 Authentication: Handled by Android Firebase Auth")
        print("🤖 AI Mode: Pure Gemini (No RAG/Vector Search)")
        print("🧪 Test AI endpoint: POST /api/test-ai")

        app.run(host='0.0.0.0', port=5000)
        
    except Exception as e:
        print(f"❌ Failed to start server: {str(e)}")
        exit(1)