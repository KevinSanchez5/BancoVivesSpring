package vives.bancovives.storage.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import vives.bancovives.storage.controller.FilesController;
import vives.bancovives.storage.exceptions.StorageException;
import vives.bancovives.storage.exceptions.StorageFileNotFoundException;
import vives.bancovives.utils.IdGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

/**
 * Esta clase proporciona métodos para almacenar, recuperar y eliminar archivos en un sistema de archivos.
 * Implementa la interfaz StorageService y utiliza Spring's FileSystemStorageService.
 *
 * @author Diego Novillo Luceño
 * @version 1.0
 */
@Service
public class FileSystemStorageService implements StorageService {

    /**
     * La ubicación raíz donde se almacenarán los archivos.
     */
    @Value("${images.storage}")
    private Path rootLocation;

    /**
     * Inicializa el almacenamiento creando el directorio raíz si no existe.
     * Este método está anotado con @PostConstruct para asegurarse de que se ejecuta después de que se crea el bean.
     */
    @PostConstruct
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("No se pudo inicializar el almacenamiento", e);
        }
    }

    /**
     * Almacena un archivo en el almacenamiento.
     *
     * @param file El archivo que se va a almacenar.
     * @return El nombre del archivo almacenado.
     * @throws StorageException Si no se puede almacenar el archivo.
     */
    @Override
    public String store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("No se pudo almacenar un archivo vacío.");
            }
            String newFileName = IdGenerator.generateId();
            String type = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
            Path destinationFile = this.rootLocation.resolve(
                            Paths.get(newFileName + type))
                    .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new StorageException(
                        "No se puede almacenar un archivo fuera del directorio actual.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }

            return destinationFile.getFileName().toString();
        } catch (NullPointerException | IOException e) {
            throw new StorageException("No se pudo almacenar el archivo.", e);
        }
    }

    /**
     * Recupera todos los nombres de archivo almacenados en el almacenamiento.
     *
     * @return Una lista de nombres de archivo.
     * @throws StorageException Si hay un error al leer los archivos almacenados.
     */
    @Override
    public List<String> loadAll() {
        try (Stream<Path> paths = Files.walk(rootLocation, 1)) {
            return paths.filter(path -> !path.equals(rootLocation))
                    .filter(Files::isRegularFile)
                    .map(path -> getUrl(path.getFileName().toString()))
                    .toList();
        } catch (IOException e) {
            throw new StorageException("No se pudieron leer los archivos almacenados", e);
        }
    }


    /**
     * Recupera la ruta de un archivo en el almacenamiento.
     *
     * @param filename El nombre del archivo.
     * @return La ruta del archivo.
     */
    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    /**
     * Recupera un archivo como recurso del almacenamiento.
     *
     * @param filename El nombre del archivo.
     * @return El archivo como recurso.
     * @throws StorageFileNotFoundException Si no se puede leer el archivo.
     */
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("No se pudo leer el archivo: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("No se pudo leer el archivo: " + filename + " " + e);
        }
    }

    /**
     * Elimina todos los archivos del almacenamiento.
     */
    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    /**
     * Recupera la URL de un archivo en el almacenamiento.
     *
     * @param filename El nombre del archivo.
     * @return La URL del archivo.
     */
    @Override
    public String getUrl(String filename) {
        return MvcUriComponentsBuilder
            .fromMethodName(FilesController.class, "serveFile", filename, null)
            .build().toUriString();
    }

    /**
     * Elimina un archivo del almacenamiento.
     *
     * @param filename El nombre del archivo.
     * @throws StorageFileNotFoundException Si no se puede eliminar el archivo.
     */
    @Override
    public void delete(String filename) {
        try {
            Files.delete(load(filename).toAbsolutePath());
        } catch (IOException e) {
            throw new StorageFileNotFoundException("No se pudo eliminar el archivo: " + filename, e);
        }
    }
}
