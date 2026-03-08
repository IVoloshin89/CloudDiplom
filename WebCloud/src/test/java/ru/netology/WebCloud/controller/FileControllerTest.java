package ru.netology.WebCloud.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.WebCloud.dto.RenameRequest;
import ru.netology.WebCloud.service.AuthService;
import ru.netology.WebCloud.service.FileService;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileControllerTest {
    @Mock
    private FileService fileService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private FileController fileController;

    private final String TEST_TOKEN = "Bearer test-jwt-token";
    private final String TEST_USERNAME = "testUser";
    private final String TEST_FILENAME = "test.txt";
    private final String TEST_NEW_FILENAME = "new_test.txt";


    @Test
    void uploadFileTest(){
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                TEST_FILENAME,
                MediaType.TEXT_PLAIN_VALUE, "test content".getBytes()
        );

        // Настраиваем поведение authService
        when(authService.getUserByToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);

        // When
        fileController.uploadFile(TEST_TOKEN, mockFile);

        // Then
        verify(authService, times(1)).getUserByToken(TEST_TOKEN);
        // Проверяем, что uploadFile вызван с правильным userLogin (не с токеном!)
        verify(fileService, times(1)).uploadFile(
                eq(TEST_USERNAME),  // Здесь должен быть userLogin, а не токен
                any(MultipartFile.class));

    }

    @Test
    void deleteFileTest() {
        // 1. Given (подготовка)
        when(authService.getUserByToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);

        // 2. When (действие) - ВЫ ЭТО ПРОПУСТИЛИ!
        fileController.deleteFile(TEST_TOKEN, TEST_FILENAME);

        // 3. Then (проверка)
        verify(authService).getUserByToken(TEST_TOKEN);
        verify(fileService).deleteFile(TEST_USERNAME, TEST_FILENAME);
        verifyNoMoreInteractions(authService, fileService);
    }

    @Test
    void downloadFileTest() throws IOException {
        // Given
        byte[] fileContent = "test file content".getBytes();

        // Создаем mock Resource
        Resource expectedResource = mock(Resource.class);
        when(expectedResource.getContentAsByteArray()).thenReturn(fileContent);

        when(authService.getUserByToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        when(fileService.downLoadFile(TEST_USERNAME, TEST_FILENAME)).thenReturn(expectedResource);

        // When
        Resource result = fileController.downLoadFile(TEST_TOKEN, TEST_FILENAME);

        // Then
        assertNotNull(result);

        byte[] resultBytes = result.getContentAsByteArray();
        assertArrayEquals(fileContent, resultBytes);

        // Проверяем вызовы
        verify(authService).getUserByToken(TEST_TOKEN);
        verify(fileService).downLoadFile(TEST_USERNAME, TEST_FILENAME);
    }

    @Test
    void renameFile() {
        // Given
        RenameRequest renameRequest = new RenameRequest();
        renameRequest.setFilename(TEST_NEW_FILENAME);

        when(authService.getUserByToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);
        doNothing().when(fileService).renameFile(TEST_USERNAME, TEST_FILENAME, TEST_NEW_FILENAME);

        // When
        fileController.renameFile(TEST_TOKEN, TEST_FILENAME, renameRequest);

        // Then
        verify(authService, times(1)).getUserByToken(TEST_TOKEN);  // Без substring!
        verify(fileService, times(1)).renameFile(TEST_USERNAME, TEST_FILENAME, TEST_NEW_FILENAME);
        verifyNoMoreInteractions(authService, fileService);
    }
}
