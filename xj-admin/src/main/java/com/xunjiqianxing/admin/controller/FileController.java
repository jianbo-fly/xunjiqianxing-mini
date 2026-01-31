package com.xunjiqianxing.admin.controller;

import com.xunjiqianxing.admin.service.FileUploadService;
import com.xunjiqianxing.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传接口
 */
@Tag(name = "文件上传")
@RestController
@RequestMapping("/admin/file")
@RequiredArgsConstructor
public class FileController {

    private final FileUploadService fileUploadService;

    @Operation(summary = "上传单个文件")
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "folder", defaultValue = "common") String folder) {
        return Result.success(fileUploadService.uploadFile(file, folder));
    }

    @Operation(summary = "批量上传文件")
    @PostMapping("/uploadBatch")
    public Result<List<String>> uploadBatch(@RequestParam("files") MultipartFile[] files,
                                            @RequestParam(value = "folder", defaultValue = "common") String folder) {
        return Result.success(fileUploadService.uploadFiles(files, folder));
    }
}
