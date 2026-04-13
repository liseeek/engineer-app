FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json .

RUN npm install

COPY . .

ARG REACT_APP_DOCTOR_SIGNUP_ENABLED=true
ENV REACT_APP_DOCTOR_SIGNUP_ENABLED=$REACT_APP_DOCTOR_SIGNUP_ENABLED

RUN NODE_OPTIONS="--max-old-space-size=4096" npm run build

FROM nginx:alpine

COPY --from=build /app/build /usr/share/nginx/html

RUN rm /etc/nginx/conf.d/default.conf

COPY nginx/default.conf /etc/nginx/conf.d

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:80/ >/dev/null 2>&1 || exit 1

CMD ["nginx", "-g", "daemon off;"]

