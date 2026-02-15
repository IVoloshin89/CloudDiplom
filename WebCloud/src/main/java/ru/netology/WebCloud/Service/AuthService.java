package ru.netology.WebCloud.Service;

import jakarta.security.auth.message.AuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.netology.WebCloud.Data.LoginRequest;
import ru.netology.WebCloud.Data.LoginResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    private final Map<String, String> userStorage = new HashMap<>();

    private final Map<String, String> tokenStorage = new HashMap<>();

    public AuthService() {
        userStorage.put("admin@mail.ru", "admin123");
        userStorage.put("user@mail.ru", "user123");
        userStorage.put("test@mail.ru", "test123");
        userStorage.put("a", "123");
    }

    /**
     * Основной метод авторизации
     *
     * @param request запрос с логином и паролем
     * @return ответ с токеном
     */
    public LoginResponse authenticate(LoginRequest request) {
        log.info("Начата авторизация для email: {}", request.getLogin());

        if (request.getLogin() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Поле email пустое");
        }

        String toklogin = request.getLogin();
        String tokpassword = request.getPassword();

        if (!userStorage.containsKey(toklogin)) {
            log.warn("Пользователь не найден: {}", toklogin);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный логин и пароль");
        }

        String storedPassword = userStorage.get(toklogin);
        if (!storedPassword.equals(tokpassword)) {
            log.warn("Неверный пароль пользователя: {}", toklogin);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Неверный логин и пароль");
        }

        //Добавлеяем токен в хранилище
        String token = generateUniqueToken(toklogin);
        tokenStorage.put(token, toklogin);


        log.info("Успешная авторизация для пользователя: {}. Токен: {}",
                toklogin, maskToken(token));

        log.info("Хранилище токеном сейчас пользователей = {}", tokenStorage.size());
        System.out.println(tokenStorage);

        return new LoginResponse(token);
    }

    private String generateUniqueToken(String login) {
        // Используем UUID и добавляем префикс для читаемости
        return "cloud_token_" + UUID.randomUUID().toString().replace("-", "_");
    }

    private String maskToken(String token) {
        if (token.length() <= 8) return "***";
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }

    /**
     * Проверка валидности токена
     */
    public String validateToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Токен отсутствует");
        }
        String login = tokenStorage.get(token);
        if (login == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный токен");
        }
        return login;
    }

    /**
     * выход из системы и удаление токена
     */
    public void logout(String token) {
        String name = tokenStorage.get(token.substring(7));
        String login = tokenStorage.remove(token.substring(7));
        if (login == null) {
            log.info("Пользователь не в системе");
        }
        log.info("Хранилище токенов сейчас пользователей = {}", tokenStorage.size());
        log.info("Пользователь {} вышел из системы", name);
        System.out.println(tokenStorage);
    }
}