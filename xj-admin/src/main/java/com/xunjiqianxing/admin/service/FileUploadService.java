package com.xunjiqianxing.admin.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import com.xunjiqianxing.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传服务
 */
@Slf4j
@Service
public class FileUploadService {

    @Value("${tencent.cos.secret-id}")
    private String secretId;

    @Value("${tencent.cos.secret-key}")
    private String secretKey;

    @Value("${tencent.cos.region}")
    private String region;

    @Value("${tencent.cos.bucket}")
    private String bucket;

    private COSClient cosClient;

    /**
     * 允许上传的文件类型
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp", "bmp"
    );

    private static final List<String> ALLOWED_DOC_TYPES = Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx"
    );

    /**
     * 最大文件大小 (10MB)
     */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @PostConstruct
    public void init() {
        if (!"your-secret-id".equals(secretId)) {
            COSCredentials credentials = new BasicCOSCredentials(secretId, secretKey);
            ClientConfig clientConfig = new ClientConfig(new Region(region));
            cosClient = new COSClient(credentials, clientConfig);
            log.info("COS客户端初始化成功");
        } else {
            log.warn("COS配置未设置，文件上传功能不可用");
        }
    }

    /**
     * 上传单个文件
     */
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file);
        return doUpload(file, folder);
    }

    /**
     * 批量上传文件
     */
    public List<String> uploadFiles(MultipartFile[] files, String folder) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file, folder));
        }
        return urls;
    }

    /**
     * 执行上传
     */
    private String doUpload(MultipartFile file, String folder) {
        if (cosClient == null) {
            throw new BizException("文件上传服务未配置");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = FileUtil.extName(originalFilename);

            // 生成文件路径: folder/yyyy/MM/dd/uuid.ext
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String key = String.format("%s/%s/%s.%s", folder, datePath, IdUtil.simpleUUID(), extension);

            InputStream inputStream = file.getInputStream();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            PutObjectRequest request = new PutObjectRequest(bucket, key, inputStream, metadata);
            cosClient.putObject(request);

            // 返回完整URL
            String url = String.format("https://%s.cos.%s.myqcloud.com/%s", bucket, region, key);
            log.info("文件上传成功: {}", url);
            return url;

        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BizException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 验证文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择文件");
        }

        // 检查文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BizException("文件大小不能超过10MB");
        }

        // 检查文件类型
        String extension = FileUtil.extName(file.getOriginalFilename());
        if (extension == null) {
            throw new BizException("无法识别的文件类型");
        }

        extension = extension.toLowerCase();
        if (!ALLOWED_IMAGE_TYPES.contains(extension) && !ALLOWED_DOC_TYPES.contains(extension)) {
            throw new BizException("不支持的文件类型: " + extension);
        }
    }
}
