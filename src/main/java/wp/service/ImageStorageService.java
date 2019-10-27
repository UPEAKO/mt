package wp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import wp.bean.ResponseBean;
import wp.exception.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class ImageStorageService {

    private final static Logger logger = LoggerFactory.getLogger(ImageStorageService.class);

    @Value("${wp.service.ImageStorageService.path}")
    private String root;

    @Value("${wp.service.ImageStorageService.baseUrl}")
    private String baseUrl;

    public ResponseBean store(MultipartFile file) {
        logger.debug("step into");
        logger.debug("root path inject into ImageStorageService is [{}]", root);
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException("Cannot store file with relative path outside current directory " + filename);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, Paths.get(root).resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                return new ResponseBean(200,"image upload succeed", baseUrl + filename);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }
}
