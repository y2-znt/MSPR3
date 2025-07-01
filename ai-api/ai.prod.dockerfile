FROM python:3.11-slim

WORKDIR /code

# Install system deps needed for pip packages (only what's needed)
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    gcc \
    libffi-dev \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY ./requirements.txt /code/requirements.txt
RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt

COPY ./app /code/app/

# Run uvicorn without --reload for production
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "80"] 