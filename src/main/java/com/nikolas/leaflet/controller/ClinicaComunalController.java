package com.nikolas.leaflet.controller;

import com.nikolas.leaflet.domain.ClinicaComunal;
import com.nikolas.leaflet.dto.BusquedaDTO;
import com.nikolas.leaflet.service.ClinicaComunalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clinicaComunal")
public class ClinicaComunalController {

    private final ClinicaComunalService clinicaComunalService;

    // Constructor injection para reemplazar @Autowired
    public ClinicaComunalController(ClinicaComunalService clinicaComunalService) {
        this.clinicaComunalService = clinicaComunalService;
    }

    @PostMapping("/buscar")
    public ResponseEntity<List<ClinicaComunal>> getClinicasByMunicipio(@RequestBody BusquedaDTO busqueda) {
        List<ClinicaComunal> clinicas = clinicaComunalService.buscarPorMunicipio(busqueda.getMunicipio());
        if (clinicas.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clinicas);
    }

    @PostMapping("/municipios")
    public ResponseEntity<List<String>> getDistinctMunicipios() {
        List<String> municipios = clinicaComunalService.getDistinctMunicipios();
        if (municipios.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(municipios);
    }

    @PostMapping("/buscarm")
    public ResponseEntity<List<ClinicaComunal>> getClinicasByMunicipios(@RequestBody List<String> municipios) {
        List<ClinicaComunal> clinicas = municipios.stream()
                .flatMap(municipio -> clinicaComunalService.buscarPorMunicipio(municipio).stream())
                .collect(Collectors.toList());
        if (clinicas.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clinicas);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ClinicaComunal>> getAllClinicas() {
        List<ClinicaComunal> clinicas = clinicaComunalService.clinicaComunalGetAll();
        if (clinicas.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clinicas);
    }

    @PostMapping("/add")
    public ResponseEntity<ClinicaComunal> addClinicaComunal(@RequestBody ClinicaComunal clinicaComunal) {
        ClinicaComunal nuevaClinica = clinicaComunalService.addClinicaComunal(clinicaComunal);
        return ResponseEntity.ok(nuevaClinica);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteClinicaComunal(@PathVariable Integer id) {
        clinicaComunalService.deleteClinicaComunal(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ClinicaComunal> updateClinicaComunal(@PathVariable Integer id, @RequestBody ClinicaComunal clinicaComunal) {
        ClinicaComunal updatedClinica = clinicaComunalService.updateClinicaComunal(id, clinicaComunal);
        return ResponseEntity.ok(updatedClinica);
    }
}
