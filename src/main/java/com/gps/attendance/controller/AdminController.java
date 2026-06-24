// package com.gps.attendance.controller;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// @Controller
// @RequestMapping("/admin")
// public class AdminController {
//     @GetMapping("/login")
//     public String loginPage() {
//         return "admin-login";
//     }
//     @PostMapping("/login")
//     public String login(
//             @RequestParam String username,
//             @RequestParam String password) {
//         if ("admin".equals(username)
//                 && "admin123".equals(password)) {
//             return "admin-dashboard";
//         }
//         return "admin-login";
//     }
//    @GetMapping("/logout")
// public String logout(jakarta.servlet.http.HttpSession session) {
//     // Session destroy
//     session.invalidate();
//     return "admin-logout";
// }
// }
package com.gps.attendance.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gps.attendance.dto.AdminChangePasswordRequest;
import com.gps.attendance.entity.Admin;
import com.gps.attendance.repository.AdminRepository;

@RestController
@CrossOrigin("*")
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminRepository repository;

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Admin admin) {

        Admin a
                = repository.findByUsernameAndPassword(
                        admin.getUsername(),
                        admin.getPassword());

        if (a != null) {

            return ResponseEntity.ok(a);

        }

        return ResponseEntity
                .badRequest()
                .body("Invalid Admin Credentials");
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody AdminChangePasswordRequest request) {

        Admin admin
                = repository.findById(
                        request.getAdminId())
                        .orElse(null);

        if (admin == null) {

            return ResponseEntity
                    .badRequest()
                    .body("Admin Not Found");
        }

        if (!admin.getPassword().equals(
                request.getCurrentPassword())) {

            return ResponseEntity
                    .badRequest()
                    .body("Current Password Incorrect");
        }

        String newPassword = request.getNewPassword();

        if (newPassword == null || newPassword.length() < 6) {
            return ResponseEntity
                    .badRequest()
                    .body("Password must be at least 6 characters");
        }

        if (admin.getPassword().equals(newPassword)) {
            return ResponseEntity
                    .badRequest()
                    .body("New password cannot be same as current password");

        }
        admin.setPassword(newPassword);

        repository.save(admin);

        return ResponseEntity.ok(
                "Password Updated Successfully");
    }
}
