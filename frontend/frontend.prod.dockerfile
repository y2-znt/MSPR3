# Stage 1: Build the Angular application
FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./

RUN npm install -g @angular/cli && \
    npm ci

COPY . .

# Build the application for production
RUN npm run build -- --configuration production

# Stage 2: Serve the application with Nginx
FROM nginx:alpine

# Copy the build output from the builder stage
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Copy the nginx configuration as a template
COPY nginx.conf /etc/nginx/conf.d/default.conf.template

# Copy the entrypoint script
COPY entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 80

# Use the entrypoint script
ENTRYPOINT ["/docker-entrypoint.sh"]