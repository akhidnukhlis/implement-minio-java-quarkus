package akhid.development.service;

import akhid.development.config.S3ConfigProperties;
import com.google.common.base.Strings;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.apache.commons.compress.utils.IOUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ValidationException;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

@Singleton
public class MinioService {
    @ConfigProperty(name = "upload.directory")
    String UPLOAD_DIR;

    @Inject
    MinioClient minioClient;

    @Inject
    S3ConfigProperties s3ConfigProperties;

    public Map<String, Object> submit(MultipartFormDataInput fileInput){
        Map<String, List<InputPart>> uploadForm = fileInput.getFormDataMap();
        Map<String, Object> result = new HashMap<>();

        List<String> fileNames = new ArrayList<>();
        List<InputPart> inputParts = uploadForm.get("file");

        if (Strings.isNullOrEmpty(inputParts.toString())) {
            throw new ValidationException("BAD_REQUEST");
        }

        String fileName = null;
        String id = UUID.randomUUID().toString();

        for (InputPart inputPart : inputParts) {
            try {
                MultivaluedMap<String, String> header = inputPart.getHeaders();
                fileName = getFileName(header);
                fileNames.add(fileName);

                InputStream inputStream = inputPart.getBody(InputStream.class, null);
                writeLocalFile(inputStream, fileName);

                byte[] bytes = IOUtils.toByteArray(inputStream);
                writeCloudFile(id, new ByteArrayInputStream(bytes));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        result.put("id", id);
        result.put("name", fileNames);

        return result;
    }

    public List<String> getAllFiles(){
        Iterable<Result<Item>> results = minioClient.listObjects(ListObjectsArgs.builder().bucket(s3ConfigProperties.bucket()).build());
        List<String> resultToList = new ArrayList<>();

        results.forEach(value -> {
            try {
                resultToList.add(value.get().objectName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return resultToList;
    }

    private String getFileName(MultivaluedMap<String, String> header) {
        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {
                String[] name = filename.split("=");
                String finalFileName = name[1].trim().replaceAll("\"", "");
                return finalFileName;
            }
        }

        return null;
    }

    private void writeLocalFile(InputStream inputStream, String fileName) throws IOException {
        byte[] bytes = IOUtils.toByteArray(inputStream);

        File customDir = new File(UPLOAD_DIR);
        fileName = customDir.getAbsolutePath() + File.separator + fileName;
        Files.write(Paths.get(fileName), bytes, StandardOpenOption.CREATE_NEW);
    }

    private void writeCloudFile(String id, InputStream isFile) {
        try {
            var object = PutObjectArgs.builder()
                    .object(id)
                    .contentType("image/jpeg")
                    // Upload unknown sized input stream.
                    .stream(isFile, -1, 10485760)
                    .bucket(s3ConfigProperties.bucket())
                    .build();
            minioClient.putObject(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
