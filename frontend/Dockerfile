FROM node:18-alpine AS builder
WORKDIR /app

# Argument z docker-compose (przekazywany z głównego .env)
ARG REACT_APP_API_BASE_URL
ENV REACT_APP_API_BASE_URL=$REACT_APP_API_BASE_URL

COPY package*.json ./
COPY public ./public
RUN npm install --force
COPY src ./src
COPY tsconfig.json* ./
COPY *.config.js* ./

# Build z zmienną środowiskową
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
