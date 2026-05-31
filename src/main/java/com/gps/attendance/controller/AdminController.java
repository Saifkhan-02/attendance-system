package com.gps.attendance.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;




@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/login")
    public String loginPage() {
        return "admin-login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password) {

        if ("admin".equals(username)
                && "admin123".equals(password)) {

            return "admin-dashboard";
        }
          

        return "admin-login";
    }
   @GetMapping("/logout")
public String logout(jakarta.servlet.http.HttpSession session) {

    // Session destroy
    session.invalidate();

    return "admin-logout";
}
}