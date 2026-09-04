# Build context is frontend/ itself (unlike Employee/Dockerfile, this isn't
# part of the Maven multi-module reactor) - e.g. docker build -f
# frontend/Dockerfile -t nexushr-frontend --build-arg VITE_API_BASE_URL=... .
#
# VITE_API_BASE_URL is baked into the JS bundle at *build* time (Vite inlines
# import.meta.env.* during `vite build`, it isn't read at container runtime
# like a Spring Boot env var would be) - so it has to be a build ARG, not
# something set on `docker run`. Rebuild the image to point at a different
# backend.
FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
RUN npm run build


FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
