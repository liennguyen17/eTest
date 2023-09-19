package com.example.ttcn2etest.fireBase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class FirebaseStorageService {
    public final Long EXPIRATION_TIME = System.currentTimeMillis() + 100L * 365 * 24 * 60 * 60 * 1000;
    @Value("${firebase.storage.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        InputStream serviceAccount = getClass().getResourceAsStream("/e-test-3d981-firebase-adminsdk-u31s6-1a408ddb35.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
        StorageOptions storageOptions = StorageOptions.newBuilder().setCredentials(credentials).build();
        Storage storage = storageOptions.getService();

        String originalFilename = file.getOriginalFilename();

        // Generate a unique filename
//        String filename = UUID.randomUUID().toString() + getFileExtension(originalFilename);

        //Lấy tên file là tên gốc mình upload
        String filename = originalFilename;

        Blob blob = storage.create(BlobInfo.newBuilder(bucketName, filename)
                .setContentType(file.getContentType())
                .build(), file.getInputStream());

        // Ví dụ: Thời gian hết hạn là 100 năm
        long expirationTime = System.currentTimeMillis() + 100L * 365 * 24 * 60 * 60 * 1000;
        String downloadUrl = blob.signUrl(expirationTime, TimeUnit.MILLISECONDS).toString();

        return downloadUrl;
    }

    private String getFileExtension(String filename) {
        if (filename != null && filename.lastIndexOf(".") != -1) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    public String uploadFileExcel(String filePath, String bucketName) throws IOException {
        // Xác thực google firebase
        InputStream serviceAccountKey = getClass().getResourceAsStream("/e-test-3d981-firebase-adminsdk-u31s6-1a408ddb35.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccountKey);

        // Nhận storage service
        StorageOptions storageOptions = StorageOptions.newBuilder().setCredentials(credentials).build();
        Storage storage = storageOptions.getService();

        // Lấy tên file
        String fileName = filePath.split("/")[filePath.split("/").length - 1];

        // Tìm kiếm và xóa file từ Firebase Storage
        Bucket bucket = storage.get(bucketName);
        for (Blob blob : bucket.list().iterateAll()) {
            if (blob.getName().equals(fileName)) {
                blob.delete();
            }
        }

        // Tải lên tệp tin vào Firebase Storage
        BlobId blobId = BlobId.of(bucketName, filePath);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        FileInputStream fileInputStream = new FileInputStream(filePath);

        return storage.create(blobInfo, fileInputStream)
                .signUrl(EXPIRATION_TIME, TimeUnit.MILLISECONDS)
                .toString();
    }

}
