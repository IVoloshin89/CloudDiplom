# Используем легкий образ с Java 17
FROM openjdk:17-jdk-slim

# Создаем директорию для приложения
WORKDIR /app

# Копируем собранный jar файл
COPY target/WebCloud-0.0.1-SNAPSHOT.jar app.jar

# Создаем папки для хранения данных
RUN mkdir -p /app/data /app/storage && \
    chmod 777 /app/data /app/storage

# Явно включаем H2 консоль через переменные окружения
ENV SPRING_PROFILES_ACTIVE=docker
ENV SPRING_H2_CONSOLE_ENABLED=true
ENV SPRING_H2_CONSOLE_SETTINGS_WEB_ALLOW_OTHERS=true
ENV SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/clouddb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
ENV SPRING_DATASOURCE_USERNAME=sa
ENV SPRING_DATASOURCE_PASSWORD=12
ENV SPRING_JPA_HIBERNATE_DDL_AUTO=update

# Указываем порт
EXPOSE 8080

# Запускаем приложение
CMD ["java", "-jar", "app.jar"]