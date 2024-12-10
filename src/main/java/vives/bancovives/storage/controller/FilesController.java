package vives.bancovives.storage.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import vives.bancovives.storage.exceptions.UnsupportedFileTypeException;
import vives.bancovives.storage.service.StorageService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar archivos almacenados en el sistema de almacenamiento.
 *
 * @author Diego Novillo Luceño
 */
@RestController
@RequestMapping("${api.version}/files")
public class FilesController {
    private StorageService storageService;

    /**
     * Inyección de dependencia del servicio de almacenamiento.
     *
     * @param storageService Servicio de almacenamiento que se va a inyectar.
     */
    @Autowired
    public FilesController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Obtiene un archivo almacenado por su nombre y lo devuelve como recurso HTTP.
     *
     * @param filename Nombre del archivo que se va a obtener.
     * @param request  Solicitud HTTP que se utiliza para obtener información del contexto de la solicitud.
     * @return {@link ResponseEntity} con el recurso del archivo y el tipo de contenido adecuado.
     */
    @GetMapping(value = "{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, HttpServletRequest request) {
        Resource file = storageService.loadAsResource(filename);

        String contentType;
        try {
            contentType = request.getServletContext().getMimeType(file.getFile().getAbsolutePath());
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede determinar el tipo de fichero");
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(file);
    }

    /**
     * Sube un archivo al sistema de almacenamiento.
     *
     * @param file Archivo que se va a subir.
     * @return {@link ResponseEntity} con la URL del archivo almacenado y un código de estado HTTP 201 (Creado).
     */
    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadFile(
            @RequestPart("file") MultipartFile file) {

        String urlImagen = null;
        String filename = file.getOriginalFilename();

        if (filename == null || !isValidFileExtension(filename)) {
            throw new UnsupportedFileTypeException("Tipo de archivo no permitido. Solo se aceptan JPG, JPEG, GIF, PNG.");
        }

        if (!file.isEmpty()) {
            String imagen = storageService.store(file);
            urlImagen = storageService.getUrl(imagen);
            Map<String, Object> response = Map.of("url", urlImagen);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se puede subir un fichero vacío");
        }
    }

    /**
     * Elimina un archivo del sistema de almacenamiento.
     *
     * @param filename Nombre del archivo que se va a eliminar.
     * @return {@link ResponseEntity} con un código de estado HTTP 204 (Sin contenido).
     */
    @DeleteMapping(value = "{filename:.+}")
    public ResponseEntity<Void> deleteFile(@PathVariable String filename) {
        storageService.delete(filename);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene una lista de todos los nombres de archivos almacenados.
     *
     * @return {@link ResponseEntity} con una lista de nombres de archivos y un código de estado HTTP 200 (Correcto).
     */
    @GetMapping()
    public ResponseEntity<List<String>> getAllFiles() {
        return ResponseEntity.ok(storageService.loadAll());
    }

    private boolean isValidFileExtension(String filename) {
        String lowerCaseFilename = filename.toLowerCase();
        return lowerCaseFilename.endsWith(".jpg") || lowerCaseFilename.endsWith(".jpeg")
                || lowerCaseFilename.endsWith(".gif") || lowerCaseFilename.endsWith(".png");
    }
}
