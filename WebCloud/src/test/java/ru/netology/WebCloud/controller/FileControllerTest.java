package ru.netology.WebCloud.controller;

import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.WebCloud.data.RenameRequest;
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

        // When
        fileController.uploadFile(TEST_TOKEN, mockFile);

        // Then
        verify(fileService, times(1)).uploadFile(
                eq(TEST_TOKEN.substring(7)),
                any(MultipartFile.class)
        );
    }

    @Test
    void deleteFileTest() {
        // When
        fileController.deleteFile(TEST_TOKEN, TEST_FILENAME);

        // Then
        verify(fileService, times(1)).deleteFile(TEST_TOKEN, TEST_FILENAME);
    }

    @Test
    void downloadFileTest() throws IOException {
        // Given
        byte[] fileContent = "test file content".getBytes();
        Resource expectedResource = new ByteArrayResource(fileContent);

        when(authService.validateToken(TEST_TOKEN.substring(7))).thenReturn(TEST_USERNAME);
        when(fileService.downLoadFile(TEST_USERNAME, TEST_FILENAME)).thenReturn(expectedResource);

        // When
        Resource result = fileController.downLoadFile(TEST_TOKEN, TEST_FILENAME);

        // Then
        assertNotNull(result);
        assertArrayEquals(fileContent, result.getContentAsByteArray());

        verify(authService, times(1)).validateToken(TEST_TOKEN.substring(7));
        verify(fileService, times(1)).downLoadFile(TEST_USERNAME, TEST_FILENAME);
    }

    @Test
    void renameFile() {
        // Given
        RenameRequest renameRequest = new RenameRequest();
        renameRequest.setFilename(TEST_NEW_FILENAME);

        when(authService.validateToken(TEST_TOKEN.substring(7))).thenReturn(TEST_USERNAME);
        doNothing().when(fileService).renameFile(TEST_USERNAME, TEST_FILENAME, TEST_NEW_FILENAME);

        // When
        fileController.renameFile(TEST_TOKEN, TEST_FILENAME, renameRequest);

        // Then
        verify(authService, times(1)).validateToken(TEST_TOKEN.substring(7));
        verify(fileService, times(1)).renameFile(TEST_USERNAME, TEST_FILENAME, TEST_NEW_FILENAME);
    }






}
