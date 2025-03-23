FROM node:18-alpine AS builder
WORKDIR /app
COPY package*.json ./
COPY public ./public
RUN npm install --force
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
