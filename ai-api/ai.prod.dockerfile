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

# Debug: Show source files
RUN echo "Contents of workspace before copy:" && ls -la

# Create model directory and copy files
RUN mkdir -p /code/app/model
COPY ./app/model/random_forest_model.pkl /code/app/model/
COPY ./app /code/app/

# Debug: Show copied files
RUN echo "Contents of /code/app:" && ls -la /code/app && \
    echo "Contents of /code/app/model:" && ls -la /code/app/model

# Run uvicorn without --reload for production
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "80"] 