package com.nikolas.leaflet.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/clinicMedic")
    public String geoAdmin() {
        return "admin/clinicMedic";
    }

    @GetMapping("/unidMedic")
    public String clinicas() {
        return "admin/unidMedic";
    }

}
