package ru.netology.WebCloud.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private AuthService authService;

    private FileService fileService;

    @TempDir
    Path tempDir;

    private final String TEST_USERNAME = "testUser";
    private final String TEST_TOKEN = "Bearer test-jwt-token";
    private final String TEST_FILENAME = "test.txt";
    private final String TEST_NEW_FILENAME = "new_test.txt";
    private final String TEST_FILE_CONTENT = "test file content";

    @BeforeEach
    void setUp() throws Exception {
        fileService = new FileService(authService);
        // Устанавливаем временную директорию для тестов через рефлексию
        java.lang.reflect.Field field = FileService.class.getDeclaredField("storagePath");
        field.setAccessible(true);
        field.set(fileService, tempDir.toString());
    }

    // Тесты для uploadFile
    @Test
    void uploadFileTest() throws IOException {
        // Given
        MultipartFile mockFile = new MockMultipartFile(
                TEST_FILENAME,
                TEST_FILENAME,
                "text/plain",
                TEST_FILE_CONTENT.getBytes()
        );

        when(authService.validateToken(TEST_TOKEN)).thenReturn(TEST_USERNAME);

        // When
        fileService.uploadFile(TEST_TOKEN, mockFile);

        // Then
        Path expectedPath = tempDir.resolve(TEST_USERNAME).resolve(TEST_FILENAME);
        assertTrue(Files.exists(expectedPath));
        assertEquals(TEST_FILE_CONTENT, Files.readString(expectedPath));
        verify(authService, times(1)).validateToken(TEST_TOKEN);
    }

    @Test
    void deleteFileTest() throws IOException {
        // Given
        String tokenWithoutBearer = TEST_TOKEN.substring(7);
        when(authService.validateToken(tokenWithoutBearer)).thenReturn(TEST_USERNAME);

        // Создаем тестовый файл
        Path userDir = tempDir.resolve(TEST_USERNAME);
        Files.createDirectories(userDir);
        Path filePath = userDir.resolve(TEST_FILENAME);
        Files.writeString(filePath, TEST_FILE_CONTENT);

        // When
        fileService.deleteFile(TEST_TOKEN, TEST_FILENAME);

        // Then
        assertFalse(Files.exists(filePath));
        verify(authService, times(1)).validateToken(tokenWithoutBearer);
    }

    @Test
    void downloadFile() throws IOException {
        // Given
        // Создаем тестовый файл
        Path userDir = tempDir.resolve(TEST_USERNAME);
        Files.createDirectories(userDir);
        Path filePath = userDir.resolve(TEST_FILENAME);
        Files.writeString(filePath, TEST_FILE_CONTENT);

        // When
        Resource resource = fileService.downLoadFile(TEST_USERNAME, TEST_FILENAME);

        // Then
        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
        assertEquals(TEST_FILE_CONTENT, new String(resource.getContentAsByteArray()));
    }

    @Test
    void renameFile_ShouldRenameFileSuccessfully() throws IOException {
        // Given
        // Создаем тестовый файл
        Path userDir = tempDir.resolve(TEST_USERNAME);
        Files.createDirectories(userDir);
        Path oldPath = userDir.resolve(TEST_FILENAME);
        Files.writeString(oldPath, TEST_FILE_CONTENT);

        // When
        fileService.renameFile(TEST_USERNAME, TEST_FILENAME, TEST_NEW_FILENAME);

        // Then
        Path newPath = userDir.resolve(TEST_NEW_FILENAME);
        assertFalse(Files.exists(oldPath));
        assertTrue(Files.exists(newPath));
        assertEquals(TEST_FILE_CONTENT, Files.readString(newPath));
    }





}
