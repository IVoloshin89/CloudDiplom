package ru.netology.WebCloud.Controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.WebCloud.Data.FileInfo;
import ru.netology.WebCloud.Service.AuthService;
import ru.netology.WebCloud.Service.FileService;

import java.util.List;

@RestController
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private AuthService authService;

    @GetMapping("/testFile")
    public String test() {
        return "Hello from File Controller";
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadFile(
            @RequestHeader("auth-token") String authToken,
            @RequestPart("file") MultipartFile file) {

        String realToken = authToken.substring(7);
        String fileName = file.getOriginalFilename();
        log.info("Имя файла загрузки {}", fileName);

        // Улучшенное логирование с проверкой на null
        log.info("=== ЗАГРУЗКА ФАЙЛА ===");
        log.info("Filename из запроса: '{}'", fileName);
        log.info("Оригинальное имя файла: '{}'", file.getOriginalFilename());
        log.info("Токен входа {}", authToken);
        log.info("Токен входа {}", realToken);
        log.info("Размер файла: {} байт", file.getSize());
        log.info("Content type: {}", file.getContentType());
        //log.info("Hash передан: {}", hash != null ? "да" : "нет");

        log.info("POST /file - Запрос на загрузку файла {}", fileName);
        fileService.uploadFile(realToken, file);
        log.info("Файл {} успешно загружен", fileName);
    }

    /**
     * DELETE /file - Удаление файла
     *
     * @param authToken токен авторизации (в заголовке)
     * @param filename  имя файла для удаления (в query параметре)
     */
    @DeleteMapping("/file")
    public void deleteFile(
            @RequestHeader("auth-token") String authToken,
            @RequestParam("filename") String filename) {

        log.info("DELETE /file - Запрос на удаление файла: {}", filename);
        fileService.deleteFile(authToken, filename);
        log.info("Файл {} успешно удален", filename);
    }

    @GetMapping("/file")
    public Resource downLoadFile(@RequestHeader("auth-token") String authToken,
                                 @RequestParam("filename") String filename){
        log.info("Запрос на скачивание файла {}",filename);
        String userName = authService.validateToken(authToken.substring(7));

        return fileService.downLoadFile(userName,filename);
    }

    @GetMapping("/list")
    public List<FileInfo> userFiles(@RequestHeader("auth-token") String authToken,
                                    @RequestParam(required = false) Integer limit) {
        String userName = authService.validateToken(authToken.substring(7));
        log.info("Запрос листа файлов для пользователя {}", userName);
        return fileService.getUserFiles(userName);
    }


}
