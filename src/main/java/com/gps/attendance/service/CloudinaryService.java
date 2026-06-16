package com.gps.attendance.service;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadProfileImage(MultipartFile file, Long employeeId) throws IOException {

        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "gps-attendance/profile",
                        "public_id", "employee_" + employeeId,
                        "overwrite", true,
                        "resource_type", "image"
                )
        );

        return uploadResult.get("secure_url").toString();
    }

   public String uploadDoctorVisitImage(MultipartFile file, Long employeeId) throws IOException {

    String uniqueName = "employee_" + employeeId + "_" + System.currentTimeMillis();

    Map uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                    "folder", "gps-attendance/doctor-visits",
                    "public_id", uniqueName,
                    "resource_type", "image",
                    "overwrite", false
            )
    );

    return uploadResult.get("secure_url").toString();
    }
}