package com.freediving.controller;

import com.freediving.dto.AdminLoginRequest;
import com.freediving.dto.AdminLoginResponse;
import com.freediving.dto.UserLoginResponse;
import com.freediving.dto.WechatLoginRequest;
import com.freediving.service.AdminAuthService;
import com.freediving.service.UserAuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminAuthService adminAuthService;
    private final UserAuthService userAuthService;

    public AuthController(AdminAuthService adminAuthService, UserAuthService userAuthService) {
        this.adminAuthService = adminAuthService;
        this.userAuthService = userAuthService;
    }

    @PostMapping("/admin/login")
    public AdminLoginResponse adminLogin(@Valid @RequestBody AdminLoginRequest request) {
        return adminAuthService.login(request);
    }

    @PostMapping("/wechat/login")
    public UserLoginResponse wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        return userAuthService.login(request);
    }
}
