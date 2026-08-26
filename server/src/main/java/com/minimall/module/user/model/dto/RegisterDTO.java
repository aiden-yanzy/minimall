package com.minimall.module.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Schema(description = "注册参数")
public class RegisterDTO {

    @Schema(description = "手机号", example = "13800138000")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @Schema(description = "密码(6-20位)", example = "123456")
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^\\S{6,20}$", message = "密码长度需为6-20位")
    private String password;

    @Schema(description = "注册渠道：h5/miniapp/wechat", example = "h5")
    private String channel;
}
