package com.lms.employee.client;

import com.lms.employee.client.dto.CreateUserRequest;
import com.lms.employee.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", configuration = FeignConfig.class)
public interface AuthClient {

    @PostMapping("/auth/users")
    void createUser(@RequestBody CreateUserRequest request);

    @DeleteMapping("/auth/users/{username}")
    void deleteUser(@PathVariable String username);
}

