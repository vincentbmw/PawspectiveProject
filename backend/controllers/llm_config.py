import os
import google.generativeai as genai
from services.firebase_service import firebase_service

# Gemini AI Configuration
model = None

def setup_llm(api_key):
    """Initialize Gemini AI with API key"""
    global model
    
    # Configure Gemini AI
    genai.configure(api_key=api_key)
    
    # Initialize the model
    model = genai.GenerativeModel('gemini-1.5-flash')
    
    print("✅ Gemini AI initialized successfully!")

def run_query(text, user_id, chat_id=None):
    """Process user query with pure Gemini AI (no RAG) with conversation context"""
    global model
    
    try:
        if not model:
            raise Exception("Gemini AI model not initialized")
        
        # Create system prompt for dog breed recommendations
        system_prompt = """You are a helpful AI assistant specializing in dog breed recommendations. 
        You provide concise, accurate information about dog breeds, their characteristics, care requirements, 
        and suitability for different lifestyles. If users don't like a suggested breed, offer alternatives. 
        Keep responses helpful and to the point. Maintain conversation context and refer to previous messages when relevant."""
        
        # Build conversation context if chat_id exists
        conversation_context = ""
        if chat_id:
            # Get previous messages for context
            previous_messages = firebase_service.get_chat_messages(user_id, chat_id)
            
            if previous_messages:
                conversation_context = "\n\nPrevious conversation:\n"
                for msg in previous_messages:
                    sender_label = "User" if msg['sender'] == 'user' else "Assistant"
                    conversation_context += f"{sender_label}: {msg['message']}\n"
                conversation_context += "\n"
        
        # Combine system prompt, conversation context, and user query
        full_prompt = f"{system_prompt}{conversation_context}\nUser question: {text}"
        
        # Generate response using Gemini
        response = model.generate_content(full_prompt)
        
        # Extract response text
        ai_response = response.text

        # Save chat history to Firebase
        if chat_id:
            # Add user message
            firebase_service.add_message(user_id, chat_id, 'user', text)
            # Add bot response
            firebase_service.add_message(user_id, chat_id, 'bot', ai_response)
        else:
            # Create new chat
            chat_title = text[:50] + "..." if len(text) > 50 else text
            chat_id = firebase_service.create_chat(user_id, chat_title)
            # Add user message
            firebase_service.add_message(user_id, chat_id, 'user', text)
            # Add bot response
            firebase_service.add_message(user_id, chat_id, 'bot', ai_response)

        print("✅ Message successfully saved to Firebase")

        return {"response": ai_response, "chatId": chat_id}
        
    except Exception as e:
        print(f"❌ Error in run_query: {str(e)}")
        raise Exception(f"Query processing failed: {str(e)}") 