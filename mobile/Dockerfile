FROM nginx:alpine

# Remove default nginx configs
RUN rm -rf /etc/nginx/conf.d/default.conf

# Copy custom Nginx SPA configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built Flutter Web static assets
COPY build/web /usr/share/nginx/html

EXPOSE 3000

STOPSIGNAL SIGQUIT

CMD ["nginx", "-g", "daemon off;"]
