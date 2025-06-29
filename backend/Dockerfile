# Start with a slim and official Python base image
FROM python:3.9-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the requirements file and install dependencies first for caching
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy your Firebase service account key into the image
# Ensure your key file is named 'firebase-service-account.json' in your project root
COPY firebase-service-account.json /app/firebase-service-account.json

# Copy the rest of your application code (respecting .dockerignore)
COPY . .

# Expose the port the app runs on
EXPOSE 5000

# The command to run your application using the main app.py
CMD ["python", "app.py"]