package ru.netology.WebCloud.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.netology.WebCloud.domain.User;
import ru.netology.WebCloud.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    private final Map<String, String> tokenStorage = new HashMap<>();

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
     *
     * @return возвращаем login
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
    }

    public String getUserByToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Токен отсуствует");
        }

        String clearToken = token;
        if (clearToken.startsWith("Bearer ")) {
            clearToken = clearToken.substring(7);
        }

        String login = tokenStorage.get(clearToken);
        if (login == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Не корректный токен");
        }

        return login;
    }

    public User authenticate(String login, String password) {
        log.info("Начат авторизация юзера {}", login);

        if (login == null || login.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Логин не может быть пустым");
        }

        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный логин"));

        if (!user.getPassword().equals(password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неверный пароль");
        }

        String token = generateUniqueToken(login);
        user.setToken(token);

        tokenStorage.put(token, login);

        log.info("Успешная авторизация юзера {}", login);
        log.info("Хранилище токенов = {}",tokenStorage.size());

        return user;
    }

}