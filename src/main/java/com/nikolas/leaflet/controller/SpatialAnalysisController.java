package com.nikolas.leaflet.controller;

import com.nikolas.leaflet.dto.CoverageAnalysisDTO;
import com.nikolas.leaflet.dto.RouteDTO;
import com.nikolas.leaflet.service.SpatialAnalysisService;
import com.nikolas.leaflet.service.ClinicaComunalService;
import com.nikolas.leaflet.service.UnidadMedicaService;
import com.nikolas.leaflet.domain.ClinicaComunal;
import com.nikolas.leaflet.domain.UnidadMedica;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/spatial")
public class SpatialAnalysisController {

    @Autowired
    private SpatialAnalysisService spatialAnalysisService;

    @Autowired
    private ClinicaComunalService clinicaComunalService;

    @Autowired
    private UnidadMedicaService unidadMedicaService;

    /**
     * Analiza la cobertura médica en una ubicación específica
     */
    @PostMapping("/coverage-analysis")
    public ResponseEntity<CoverageAnalysisDTO> analyzeCoverage(@RequestBody Map<String, Double> request) {
        try {
            double lat = request.get("lat");
            double lon = request.get("lon");

            List<ClinicaComunal> clinicas = clinicaComunalService.clinicaComunalGetAll();
            List<UnidadMedica> unidades = unidadMedicaService.getAllEntidadesMedicas();

            CoverageAnalysisDTO analysis = spatialAnalysisService.analyzeCoverage(lat, lon, clinicas, unidades);

            return ResponseEntity.ok(analysis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene la ruta desde una ubicación hasta un centro médico
     */
    @PostMapping("/route")
    public ResponseEntity<RouteDTO> getRoute(@RequestBody Map<String, Object> request) {
        try {
            double startLat = ((Number) request.get("startLat")).doubleValue();
            double startLon = ((Number) request.get("startLon")).doubleValue();
            double endLat = ((Number) request.get("endLat")).doubleValue();
            double endLon = ((Number) request.get("endLon")).doubleValue();

            RouteDTO route = spatialAnalysisService.getRoute(startLat, startLon, endLat, endLon);

            // Agregar geocodificación inversa para las direcciones
            route.setStartAddress(spatialAnalysisService.reverseGeocode(startLat, startLon));
            route.setEndAddress(spatialAnalysisService.reverseGeocode(endLat, endLon));

            return ResponseEntity.ok(route);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtiene geocodificación inversa para unas coordenadas
     */
    @GetMapping("/reverse-geocode")
    public ResponseEntity<Map<String, String>> reverseGeocode(
            @RequestParam double lat, 
            @RequestParam double lon) {
        try {
            String address = spatialAnalysisService.reverseGeocode(lat, lon);
            
            Map<String, String> response = new HashMap<>();
            response.put("address", address);
            response.put("lat", String.valueOf(lat));
            response.put("lon", String.valueOf(lon));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Encuentra áreas sin cobertura médica en una región
     */
    @PostMapping("/uncovered-areas")
    public ResponseEntity<List<Map<String, Object>>> findUncoveredAreas(@RequestBody Map<String, Double> request) {
        try {
            double minLat = request.get("minLat");
            double minLon = request.get("minLon");
            double maxLat = request.get("maxLat");
            double maxLon = request.get("maxLon");

            List<ClinicaComunal> clinicas = clinicaComunalService.clinicaComunalGetAll();
            List<UnidadMedica> unidades = unidadMedicaService.getAllEntidadesMedicas();

            List<Map<String, Object>> uncoveredAreas = spatialAnalysisService.findUncoveredAreas(
                minLat, minLon, maxLat, maxLon, clinicas, unidades
            );

            return ResponseEntity.ok(uncoveredAreas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Encuentra el centro médico más cercano y obtiene la ruta
     */
    @PostMapping("/nearest-center-route")
    public ResponseEntity<Map<String, Object>> getNearestCenterRoute(@RequestBody Map<String, Double> request) {
        try {
            double userLat = request.get("lat");
            double userLon = request.get("lon");

            List<ClinicaComunal> clinicas = clinicaComunalService.clinicaComunalGetAll();
            List<UnidadMedica> unidades = unidadMedicaService.getAllEntidadesMedicas();

            // Encontrar el centro más cercano
            double minDistance = Double.MAX_VALUE;
            Map<String, Object> nearestCenter = null;

            // Buscar en clínicas
            for (ClinicaComunal clinica : clinicas) {
                double distance = spatialAnalysisService.calculateDistance(
                    userLat, userLon, clinica.getCoorY(), clinica.getCoorX()
                );
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestCenter = new HashMap<>();
                    nearestCenter.put("id", clinica.getId());
                    nearestCenter.put("name", clinica.getNombre());
                    nearestCenter.put("type", "clinica");
                    nearestCenter.put("lat", clinica.getCoorY());
                    nearestCenter.put("lon", clinica.getCoorX());
                    nearestCenter.put("address", clinica.getDireccion());
                    nearestCenter.put("distance", distance);
                }
            }

            // Buscar en unidades médicas
            for (UnidadMedica unidad : unidades) {
                double distance = spatialAnalysisService.calculateDistance(
                    userLat, userLon, unidad.getCoorY(), unidad.getCoorX()
                );
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestCenter = new HashMap<>();
                    nearestCenter.put("id", unidad.getId());
                    nearestCenter.put("name", unidad.getNombre());
                    nearestCenter.put("type", "unidad");
                    nearestCenter.put("lat", unidad.getCoorY());
                    nearestCenter.put("lon", unidad.getCoorX());
                    nearestCenter.put("address", unidad.getDireccion());
                    nearestCenter.put("distance", distance);
                }
            }

            if (nearestCenter != null) {
                // Obtener la ruta al centro más cercano
                RouteDTO route = spatialAnalysisService.getRoute(
                    userLat, userLon, 
                    (Double) nearestCenter.get("lat"), 
                    (Double) nearestCenter.get("lon")
                );

                Map<String, Object> response = new HashMap<>();
                response.put("nearestCenter", nearestCenter);
                response.put("route", route);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }

        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}