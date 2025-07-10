import smtplib
import os
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
from datetime import datetime
from typing import Optional
import time

class EmailService:
    def __init__(self):
        # Email configuration from environment variables
        self.smtp_server = os.getenv('SMTP_SERVER', 'smtp.gmail.com')
        self.smtp_port = int(os.getenv('SMTP_PORT', '587'))
        self.sender_email = os.getenv('SENDER_EMAIL')
        self.sender_password = os.getenv('SENDER_PASSWORD')
        self.company_email = os.getenv('COMPANY_EMAIL')
        
        # Email template configuration
        self.app_name = os.getenv('APP_NAME', 'Pawspective')
        self.subject_prefix = os.getenv('FEEDBACK_SUBJECT_PREFIX', 'Pawspective App Feedback')
        self.retry_attempts = int(os.getenv('EMAIL_RETRY_ATTEMPTS', '3'))
        self.timeout = int(os.getenv('EMAIL_TIMEOUT', '30'))
        
    def _create_html_template(self, user_id: str, user_email: str, feedback: str) -> str:
        """Create professional HTML email template"""
        timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S UTC')
        
        html_content = f"""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>{self.app_name} Feedback</title>
            <style>
                body {{
                    font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    line-height: 1.6;
                    color: #333;
                    max-width: 600px;
                    margin: 0 auto;
                    padding: 20px;
                    background-color: #f9f9f9;
                }}
                .header {{
                    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                    color: white;
                    padding: 20px;
                    text-align: center;
                    border-radius: 10px 10px 0 0;
                }}
                .content {{
                    background: white;
                    padding: 30px;
                    border-radius: 0 0 10px 10px;
                    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                }}
                .info-section {{
                    background: #f8f9fa;
                    padding: 15px;
                    border-radius: 8px;
                    margin: 20px 0;
                    border-left: 4px solid #667eea;
                }}
                .feedback-box {{
                    background: #fff;
                    border: 1px solid #e9ecef;
                    border-radius: 8px;
                    padding: 20px;
                    margin: 20px 0;
                    white-space: pre-wrap;
                    word-wrap: break-word;
                }}
                .footer {{
                    text-align: center;
                    margin-top: 30px;
                    padding-top: 20px;
                    border-top: 1px solid #e9ecef;
                    color: #6c757d;
                    font-size: 12px;
                }}
                .label {{
                    font-weight: bold;
                    color: #495057;
                }}
                .value {{
                    color: #6c757d;
                }}
                .priority {{
                    display: inline-block;
                    background: #28a745;
                    color: white;
                    padding: 4px 8px;
                    border-radius: 4px;
                    font-size: 12px;
                    font-weight: bold;
                }}
            </style>
        </head>
        <body>
            <div class="header">
                <h1>🐾 {self.app_name}</h1>
                <p>New User Feedback Received</p>
            </div>
            
            <div class="content">
                <div class="info-section">
                    <h3>📋 Feedback Details</h3>
                    <p><span class="label">User ID:</span> <span class="value">{user_id}</span></p>
                    <p><span class="label">User Email:</span> <span class="value">{user_email}</span></p>
                    <p><span class="label">Timestamp:</span> <span class="value">{timestamp}</span></p>
                    <p><span class="label">Priority:</span> <span class="priority">NORMAL</span></p>
                </div>
                
                <h3>💬 User Feedback:</h3>
                <div class="feedback-box">
{feedback}
                </div>
                
                <div class="info-section">
                    <h4>📱 App Information</h4>
                    <p><span class="label">Application:</span> <span class="value">{self.app_name} Mobile App</span></p>
                    <p><span class="label">Platform:</span> <span class="value">Android</span></p>
                    <p><span class="label">Feedback Type:</span> <span class="value">User Feedback</span></p>
                </div>
                
                <div class="footer">
                    <p>This is an automated message from {self.app_name} App.</p>
                    <p>Please do not reply to this email. For technical support, contact your development team.</p>
                    <p>© 2024 {self.app_name}. All rights reserved.</p>
                </div>
            </div>
        </body>
        </html>
        """
        return html_content
    
    def _create_plain_text_template(self, user_id: str, user_email: str, feedback: str) -> str:
        """Create plain text email template as fallback"""
        timestamp = datetime.now().strftime('%Y-%m-%d %H:%M:%S UTC')
        
        plain_text = f"""
🐾 {self.app_name} - New User Feedback Received

═══════════════════════════════════════════════════════════════

📋 FEEDBACK DETAILS:
• User ID: {user_id}
• User Email: {user_email}
• Timestamp: {timestamp}
• Priority: NORMAL

═══════════════════════════════════════════════════════════════

💬 USER FEEDBACK:

{feedback}

═══════════════════════════════════════════════════════════════

📱 APP INFORMATION:
• Application: {self.app_name} Mobile App
• Platform: Android
• Feedback Type: User Feedback

═══════════════════════════════════════════════════════════════

This is an automated message from {self.app_name} App.
Please do not reply to this email. For technical support, contact your development team.

© 2024 {self.app_name}. All rights reserved.
        """
        return plain_text.strip()
    
    def send_feedback_email(self, user_id: str, user_email: str, feedback: str) -> bool:
        """Send feedback to company email with retry logic"""
        
        if not all([self.sender_email, self.sender_password, self.company_email]):
            print("Error: Email configuration is incomplete")
            return False
        
        for attempt in range(self.retry_attempts):
            try:
                # Create message
                message = MIMEMultipart('alternative')
                message["From"] = f"{self.app_name} <{self.sender_email}>"
                message["To"] = self.company_email
                message["Subject"] = f"{self.subject_prefix} - User {user_id[:8]}..."
                message["Reply-To"] = self.company_email
                
                # Create both plain text and HTML versions
                text_content = self._create_plain_text_template(user_id, user_email, feedback)
                html_content = self._create_html_template(user_id, user_email, feedback)
                
                # Attach both versions
                text_part = MIMEText(text_content, 'plain', 'utf-8')
                html_part = MIMEText(html_content, 'html', 'utf-8')
                
                message.attach(text_part)
                message.attach(html_part)
                
                # Send email
                with smtplib.SMTP(self.smtp_server, self.smtp_port) as server:
                    server.set_debuglevel(0)  # Set to 1 for debugging
                    server.starttls()
                    server.login(self.sender_email, self.sender_password)
                    server.send_message(message)
                
                print(f"Feedback email sent successfully to {self.company_email}")
                return True
                
            except smtplib.SMTPAuthenticationError as e:
                print(f"SMTP Authentication Error (attempt {attempt + 1}): {str(e)}")
                if attempt == self.retry_attempts - 1:
                    print("Failed to authenticate with email server. Check credentials.")
                    return False
                    
            except smtplib.SMTPException as e:
                print(f"SMTP Error (attempt {attempt + 1}): {str(e)}")
                if attempt == self.retry_attempts - 1:
                    print("Failed to send email after all retry attempts.")
                    return False
                    
            except Exception as e:
                print(f"General error sending feedback email (attempt {attempt + 1}): {str(e)}")
                if attempt == self.retry_attempts - 1:
                    return False
            
            # Wait before retry
            if attempt < self.retry_attempts - 1:
                time.sleep(2 ** attempt)  # Exponential backoff
        
        return False
    
    def test_email_connection(self) -> bool:
        """Test email configuration and connection"""
        try:
            with smtplib.SMTP(self.smtp_server, self.smtp_port) as server:
                server.starttls()
                server.login(self.sender_email, self.sender_password)
            print("Email configuration test successful")
            return True
        except Exception as e:
            print(f"Email configuration test failed: {str(e)}")
            return False

# Singleton instance
email_service = EmailService() 