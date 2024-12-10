package vives.bancovives.storage.service;


import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;

public interface StorageService {

    void init();

    String store(MultipartFile file);

    List<String> loadAll();

    Path load(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

    String getUrl(String filename);

    void delete(String filename);
}
