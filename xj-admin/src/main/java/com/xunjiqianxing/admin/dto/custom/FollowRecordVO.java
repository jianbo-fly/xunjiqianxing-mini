package com.xunjiqianxing.admin.dto.custom;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 跟进记录VO
 */
@Data
@Schema(description = "跟进记录VO")
public class FollowRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "跟进内容")
    private String content;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
