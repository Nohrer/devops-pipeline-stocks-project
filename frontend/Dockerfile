FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json .
RUN npm install --legacy-peer-deps

COPY . .

ENV REACT_APP_API_URL=http://localhost:8090

RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
EXPOSE 80

CMD ["nginx","-g","deamon off;"]

