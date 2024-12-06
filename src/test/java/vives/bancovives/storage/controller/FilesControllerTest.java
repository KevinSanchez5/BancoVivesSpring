package vives.bancovives.storage.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import vives.bancovives.storage.service.StorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    private String fileName;
    private Resource mockResource;

    @BeforeEach
    void setUp() throws IOException {
        Path tempFilePath = Files.createTempFile("test", ".jpg");
        mockResource = new UrlResource(tempFilePath.toUri());
        fileName = mockResource.getFilename();
    }

    @Test
    void serveFile() throws Exception {
        // Arrange
        when(storageService.loadAsResource(any(String.class))).thenReturn(mockResource);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        get("/v1/files/" + fileName)
                                .accept(MediaType.APPLICATION_OCTET_STREAM))
                .andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        assertEquals("image/jpeg", response.getContentType());
    }

    @Test
    void uploadFile() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "sample.jpg", "image/jpeg", "some Image".getBytes());
        String storedFileName = "sample.jpg";

        when(storageService.store(file)).thenReturn(storedFileName);
        when(storageService.getUrl(storedFileName)).thenReturn(fileName);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        multipart("/v1/files")
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn().getResponse();

        // Assert
        assertEquals(201, response.getStatus());
        assertTrue(response.getContentAsString().contains("url"));
        assertTrue(response.getContentAsString().contains(fileName));
    }
    @Test
    void uploadFileWithInvalidContentType() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "sample.txt", "text/plain", new byte[0]);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        multipart("/v1/files")
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn().getResponse();

        // Assert
        assertEquals(415, response.getStatus());
    }

    @Test
    void deleteFile() throws Exception {
        // Arrange
        doNothing().when(storageService).delete(any());

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        delete("/v1/files/" + "default.png"))
                .andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        verify(storageService, times(1)).delete(any());
    }

    @Test
    void getAllFiles() throws Exception {
        // Arrange
        List<String> files = List.of("sample.jpg", "example.png");
        when(storageService.loadAll()).thenReturn(files);

        // Act
        MockHttpServletResponse response = mockMvc.perform(
                        get("/v1/files"))
                .andReturn().getResponse();

        // Assert
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("example.png"));
        assertTrue(response.getContentAsString().contains("sample.jpg"));

    }

    @Test
    void uploadInvalidFile() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "sample.txt", "text/plain", new byte[0]);

        // Act & Assert
        mockMvc.perform(
                        multipart("/v1/files")
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn().getResponse();
    }

    @Test
    void uploadEmptyFile() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "", "text/plain", new byte[0]);

        // Act & Assert
        MockHttpServletResponse response = mockMvc.perform(
                        multipart("/v1/files")
                                .file(file)
                                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andReturn().getResponse();

        // Assert
        assertEquals(415, response.getStatus());
    }
}
