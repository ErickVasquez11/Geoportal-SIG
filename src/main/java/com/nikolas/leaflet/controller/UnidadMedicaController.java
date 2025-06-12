package com.nikolas.leaflet.controller;

import java.util.ArrayList;
import java.util.List;

import com.nikolas.leaflet.domain.UnidadMedica;
import com.nikolas.leaflet.service.UnidadMedicaServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nikolas.leaflet.dto.BusquedaDTO;

@RestController
@RequestMapping("/unidadMedica")
public class UnidadMedicaController {

    @Autowired
    private UnidadMedicaServiceImpl unidadMedicaService;

    // Buscar entidades por municipio
    @PostMapping("/buscar")
    public ResponseEntity<List<UnidadMedica>> getEntidadesByMunicipio(@RequestBody BusquedaDTO busqueda) {
        List<UnidadMedica> entidades = unidadMedicaService.buscarPorMunicipio(busqueda.getMunicipio());
        if (entidades.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entidades);
    }

    // Obtener lista de municipios distintos
    @PostMapping("/municipios")
    public ResponseEntity<List<String>> getDistinctMunicipios() {
        List<String> municipios = unidadMedicaService.getDistinctMunicipios();
        if (municipios.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(municipios);
    }

    // Buscar entidades en múltiples municipios
    @PostMapping("/buscarm")
    public ResponseEntity<List<UnidadMedica>> getEntidadesByMunicipios(@RequestBody List<String> municipios) {
        List<UnidadMedica> entidades = new ArrayList<>();
        for (String municipio : municipios) {
            entidades.addAll(unidadMedicaService.buscarPorMunicipio(municipio));
        }
        if (entidades.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entidades);
    }

    // Obtener todas las entidades
    @GetMapping("/all")
    public ResponseEntity<List<UnidadMedica>> getAllEntidades() {
        List<UnidadMedica> entidades = unidadMedicaService.getAllEntidadesMedicas();
        if (entidades.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entidades);
    }

    // Agregar una nueva entidad médica
    @PostMapping("/add")
    public ResponseEntity<UnidadMedica> addEntidadMedica(@RequestBody UnidadMedica entidadMedica) {
        UnidadMedica nuevaEntidad = unidadMedicaService.addUnidadMedica(entidadMedica);
        return ResponseEntity.ok(nuevaEntidad);
    }

    // Eliminar una entidad médica por ID
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteEntidadMedica(@PathVariable Integer id) {
        unidadMedicaService.deleteUnidadMedica(id);
        return ResponseEntity.ok().build();
    }

    // Actualizar una entidad médica por ID
    @PutMapping("/update/{id}")
    public ResponseEntity<UnidadMedica> updateEntidadMedica(@PathVariable Integer id, @RequestBody UnidadMedica entidadMedica) {
        UnidadMedica entidadActualizada = unidadMedicaService.updateUnidadMedica(id, entidadMedica);
        return ResponseEntity.ok(entidadActualizada);
    }
}