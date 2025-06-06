#!/bin/sh

# Replace environment variables in the nginx configuration template
envsubst '$SERVER_NAME' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf

# Start nginx
exec nginx -g 'daemon off;' 