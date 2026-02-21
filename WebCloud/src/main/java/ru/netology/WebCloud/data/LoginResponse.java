package ru.netology.WebCloud.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {

    @JsonProperty("auth-token") //Для совместимости с фронтом
    private String authToken;

    //private LocalDateTime expiresAt; //Время истечения
}
