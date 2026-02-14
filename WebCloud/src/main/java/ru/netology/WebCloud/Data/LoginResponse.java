package ru.netology.WebCloud.Data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    @JsonProperty("auth-token") //Для совместимости с фронтом
    private String authToken;

    //private LocalDateTime expiresAt; //Время истечения
}
