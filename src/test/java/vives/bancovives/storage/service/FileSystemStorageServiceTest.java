package vives.bancovives.storage.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import vives.bancovives.storage.controller.FilesController;
import vives.bancovives.storage.exceptions.StorageException;
import vives.bancovives.storage.exceptions.StorageFileNotFoundException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.*;

class FileSystemStorageServiceTest {

    @InjectMocks
    private FileSystemStorageService storageService;

    private Path tempFilePath;

    @BeforeEach
    void setUp() throws IOException {
        tempFilePath = Files.createTempDirectory("storage");
        storageService = new FileSystemStorageService();
        ReflectionTestUtils.setField(storageService, "rootLocation", tempFilePath);
    }

    @Test
    void testStore() throws IOException {
        // Arrange
        MultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "content".getBytes());

        // Act
        String storedFileName = storageService.store(file);

        // Assert
        assertNotNull(storedFileName);
        assertTrue(Files.exists(tempFilePath.resolve(storedFileName)));

        // Clean up
        Files.deleteIfExists(tempFilePath.resolve(storedFileName));
    }


    @Test
    void testStoreEmptyFile() {
        // Arrange
        MultipartFile emptyFile = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

        // Act & Assert
        assertThrows(StorageException.class, () -> storageService.store(emptyFile));
    }

    @Test
    void testLoadAll() throws IOException {
        // Arrange
        Path file1 = Files.createFile(tempFilePath.resolve("file1.jpg"));
        Path file2 = Files.createFile(tempFilePath.resolve("file2.jpg"));

        String fileName1 = file1.getFileName().toString();
        String fileName2 = file2.getFileName().toString();
        String expectedUrl1 = "http://localhost/v1/files/" + fileName1;
        String expectedUrl2 = "http://localhost/v1/files/" + fileName2;

        try (MockedStatic<MvcUriComponentsBuilder> mockedBuilder = Mockito.mockStatic(MvcUriComponentsBuilder.class)) {
            // Mock para el primer fichero
            UriComponentsBuilder uriComponentsBuilder1 = mock(UriComponentsBuilder.class);
            UriComponents uriComponents1 = mock(UriComponents.class);
            mockedBuilder.when(() -> MvcUriComponentsBuilder.fromMethodName(
                            eq(FilesController.class), eq("serveFile"), eq(fileName1), isNull()))
                    .thenReturn(uriComponentsBuilder1);
            when(uriComponentsBuilder1.build()).thenReturn(uriComponents1);
            when(uriComponents1.toUriString()).thenReturn(expectedUrl1);

            //Mock para el uri del segundo fichero
            UriComponentsBuilder uriComponentsBuilder2 = mock(UriComponentsBuilder.class);
            UriComponents uriComponents2 = mock(UriComponents.class);
            mockedBuilder.when(() -> MvcUriComponentsBuilder.fromMethodName(
                            eq(FilesController.class), eq("serveFile"), eq(fileName2), isNull()))
                    .thenReturn(uriComponentsBuilder2);
            when(uriComponentsBuilder2.build()).thenReturn(uriComponents2);
            when(uriComponents2.toUriString()).thenReturn(expectedUrl2);

            // Act
            List<String> files = storageService.loadAll();

            // Assert
            assertEquals(2, files.size());
            assertEquals(expectedUrl1, files.get(0));
            assertEquals(expectedUrl2, files.get(1));
        }
    }



    @Test
    void testLoadAsResource() throws IOException {
        // Arrange
        ReflectionTestUtils.setField(storageService, "rootLocation", tempFilePath);

        String fileName = "file.jpg";
        Path filePath = tempFilePath.resolve(fileName);

        // Create the file to mock its existence
        Files.createFile(filePath);

        // Mockeo Files
        try (MockedStatic<Files> filesMock = Mockito.mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(filePath)).thenReturn(true);
            filesMock.when(() -> Files.isReadable(filePath)).thenReturn(true);

            Resource mockResource = mock(Resource.class);
            when(mockResource.exists()).thenReturn(true);
            when(mockResource.isReadable()).thenReturn(true);

            // Act
            Resource resource = storageService.loadAsResource(fileName);

            // Assert
            assertNotNull(resource);
            assertTrue(resource.exists());
            assertTrue(resource.isReadable());
        }

        // Clean up
        Files.deleteIfExists(filePath);
    }



    @Test
    void testLoadAsResourceNotFound() {
        // Arrange
        String fileName = "fileNotFound.jpg";

        // Act & Assert
        assertThrows(StorageFileNotFoundException.class, () -> storageService.loadAsResource(fileName));
    }

    @Test
    void testDeleteAll() throws IOException {
        // Arrange
        Files.createFile(tempFilePath.resolve("file1.jpg"));
        Files.createFile(tempFilePath.resolve("file2.jpg"));

        // Act
        storageService.deleteAll();

        // Assert
        assertFalse(Files.exists(tempFilePath.resolve("file1.jpg")));
        assertFalse(Files.exists(tempFilePath.resolve("file2.jpg")));
    }

    @Test
    void testDelete() throws IOException {
        // Arrange
        Files.createFile(tempFilePath.resolve("file.jpg"));

        // Act
        storageService.delete("file.jpg");

        // Assert
        assertFalse(Files.exists(tempFilePath.resolve("file.jpg")));
    }

    @Test
    void testDeleteFileNotFound() {
        // Arrange
        String fileName = "fileNotFound.jpg";

        // Act & Assert
        assertThrows(StorageFileNotFoundException.class, () -> storageService.delete(fileName));
    }

    @Test
    void testGetUrl() {
        // Arrange
        String filename = "file.jpg";
        String expectedUrl = "http://localhost/v1/files/" + filename;

        try (MockedStatic<MvcUriComponentsBuilder> mockedBuilder = Mockito.mockStatic(MvcUriComponentsBuilder.class)) {
            UriComponentsBuilder uriComponentsBuilder = mock(UriComponentsBuilder.class);
            UriComponents uriComponents = mock(UriComponents.class);

            mockedBuilder.when(() -> MvcUriComponentsBuilder.fromMethodName(
                            eq(FilesController.class), eq("serveFile"), eq(filename), isNull()))
                    .thenReturn(uriComponentsBuilder);

            when(uriComponentsBuilder.build()).thenReturn(uriComponents);
            when(uriComponents.toUriString()).thenReturn(expectedUrl);


            // Act
            String url = storageService.getUrl(filename);

            // Assert
            assertEquals(expectedUrl, url);
        }
    }
}
