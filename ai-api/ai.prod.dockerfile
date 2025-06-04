FROM python:3.11-slim

WORKDIR /code

# Install system deps needed for pip packages (only what's needed)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    gcc \
    libffi-dev \
    && rm -rf /var/lib/apt/lists/*

COPY ./requirements.txt /code/requirements.txt
RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt

# Create model directory and copy app code first
RUN mkdir -p /code/app/model
COPY ./app /code/app

# Debug: List contents after app copy
RUN echo "Contents of /code/app:" && ls -la /code/app && \
    echo "Contents of /code/app/model:" && ls -la /code/app/model

# Copy the model file last to ensure it's not overwritten
COPY ./app/model/random_forest_model.pkl /code/app/model/

# Debug: List contents after model copy
RUN echo "Final contents of /code/app/model:" && ls -la /code/app/model

# Run uvicorn without --reload for production
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "80"] 