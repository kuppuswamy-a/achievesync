FROM dart:stable AS build

WORKDIR /app

COPY pubspec.yaml pubspec.lock ./
RUN dart pub get

COPY . .
RUN dart pub get --offline
RUN dart compile js -O2 -o build/web/main.dart.js lib/main.dart

FROM nginx:alpine

COPY --from=build /app/build/web /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]