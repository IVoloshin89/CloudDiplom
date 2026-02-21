package ru.netology.WebCloud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import ru.netology.WebCloud.data.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileService {

    @Value("${cloud.storage.path:./storage}")
    private String storagePath;

    private final AuthService authService;

    public FileService(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Загрузка файла (POST /file)
     */
    public void uploadFile(String authToken, MultipartFile file) {
        String userLogin = authService.validateToken(authToken);
        String fileName = file.getOriginalFilename();
        log.info("Загрузка файла {} пользователя: {}", fileName, userLogin);

        // Валидация файла
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл пустой");
        }

        try {
            Path userDir = Paths.get(storagePath, sanitizeUsername(userLogin));
            Files.createDirectories(userDir);

            Path filePath = userDir.resolve(fileName);
            log.info("Папка {} создана c документом {}", filePath, fileName);

            if (Files.exists(filePath)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл уже существует");
            }

            Files.copy(file.getInputStream(), filePath);
            log.info("Файл {} загружен пользователем {}", fileName, userLogin);

        } catch (IOException e) {
            log.error("Ошибка при загрузке файла: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка загрузки файла");
        }

    }

    /**
     * Удаление файла (DELETE /file)
     */
    public void deleteFile(String authToken, String filename) {
        String userLogin = authService.validateToken(authToken.substring(7));
        log.info("Удаление файла {} пользователем: {}", filename, userLogin);

        if (filename == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Недопустимое имя файла");
        }

        try {
            Path filePath = Paths.get(storagePath, sanitizeUsername(userLogin), filename);

            if (!Files.exists(filePath)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не найден");
            }

            Files.delete(filePath);
            log.info("Файл {} удален пользователем {}", filename, userLogin);

        } catch (IOException e) {
            log.error("Ошибка при удалении файла: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка удаления файла");
        }
    }

    /**
     * Скачиваение файла (GET /file)
     */
    public Resource downLoadFile(String userName, String fileName) {
        Path userDir = Paths.get(storagePath, sanitizeUsername(userName));
        Path filePath = userDir.resolve(fileName);

        try {
            // Проверяем, что файл существует
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл отсутсвует");
            }

            return new UrlResource(filePath.toUri());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Невозможно скачать файл");
        }
    }

    /**
     * Переименование файла (PUT /file)
     */
    public void renameFile(String userName, String fileName, String newFileName) {

        try {

            Path userDir = Paths.get(storagePath, userName);
            Path oldPath = userDir.resolve(fileName).normalize();
            Path newPath = userDir.resolve(newFileName).normalize();


            if (!Files.exists(oldPath)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Файл не найден");
            }

            // Переименовываем
            Files.move(oldPath, newPath);

        } catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,"Ошибка переимнования файла");
        }
    }

    /**
     * Очистка имени пользователя для файловой системы
     */
    private String sanitizeUsername(String username) {
        return username.replaceAll("[^a-zA-Z0-9._-]", "_");
    }


    public List<FileInfo> getUserFiles(String userLogin) {
        Path userDir = Paths.get(storagePath, sanitizeUsername(userLogin));
        //Получаем список файлов
        try {
            List<FileInfo> userFiles = Files.list(userDir)
                    .filter(Files::isRegularFile)  //Только файлы, не папки/ссылки
                    .map(this::createFileInfo)
                    .collect(Collectors.toList());
            return userFiles;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ошбка чтения файлов в папке");
        }
    }

    //Собираем FileInfo
    public FileInfo createFileInfo(Path path) {
        try {
            FileTime fileTime = Files.getLastModifiedTime(path);
            String formatTime = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                    .withZone(ZoneOffset.UTC)

                    .format(Files.getLastModifiedTime(path).toInstant());
            return new FileInfo(
                    path.getFileName().toString(),
                    Files.size(path),
                    formatTime
            );
        } catch (IOException e) {
            return null;
        }
    }
}



