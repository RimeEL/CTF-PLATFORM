FROM node:20-alpine AS build
WORKDIR /app


COPY ctf-front-master/package*.json ./
RUN npm install
COPY ctf-front-master/ .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80