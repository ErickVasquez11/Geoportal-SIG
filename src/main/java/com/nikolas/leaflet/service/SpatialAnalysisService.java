package com.nikolas.leaflet.service;

import com.nikolas.leaflet.domain.ClinicaComunal;
import com.nikolas.leaflet.domain.UnidadMedica;
import com.nikolas.leaflet.dto.CoverageAnalysisDTO;
import com.nikolas.leaflet.dto.RouteDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Service
public class SpatialAnalysisService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    // Radio de cobertura en metros (1 km por defecto)
    private static final double COVERAGE_RADIUS = 1000.0;
    
    public SpatialAnalysisService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Calcula la distancia entre dos puntos usando la fórmula de Haversine
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radio de la Tierra en km
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c * 1000; // Convertir a metros
    }

    /**
     * Analiza la cobertura médica en una zona específica
     */
    public CoverageAnalysisDTO analyzeCoverage(double centerLat, double centerLon, 
                                             List<ClinicaComunal> clinicas, 
                                             List<UnidadMedica> unidades) {
        
        CoverageAnalysisDTO analysis = new CoverageAnalysisDTO();
        analysis.setCenterLat(centerLat);
        analysis.setCenterLon(centerLon);
        
        List<Map<String, Object>> coverageAreas = new ArrayList<>();
        List<Map<String, Object>> uncoveredAreas = new ArrayList<>();
        
        // Analizar cobertura de clínicas
        for (ClinicaComunal clinica : clinicas) {
            Map<String, Object> coverage = new HashMap<>();
            coverage.put("id", clinica.getId());
            coverage.put("name", clinica.getNombre());
            coverage.put("type", "clinica");
            coverage.put("lat", clinica.getCoorY());
            coverage.put("lon", clinica.getCoorX());
            coverage.put("radius", COVERAGE_RADIUS);
            
            double distance = calculateDistance(centerLat, centerLon, 
                                              clinica.getCoorY(), clinica.getCoorX());
            coverage.put("distanceFromCenter", distance);
            coverage.put("coversCenter", distance <= COVERAGE_RADIUS);
            
            coverageAreas.add(coverage);
        }
        
        // Analizar cobertura de unidades médicas
        for (UnidadMedica unidad : unidades) {
            Map<String, Object> coverage = new HashMap<>();
            coverage.put("id", unidad.getId());
            coverage.put("name", unidad.getNombre());
            coverage.put("type", "unidad");
            coverage.put("lat", unidad.getCoorY());
            coverage.put("lon", unidad.getCoorX());
            coverage.put("radius", COVERAGE_RADIUS);
            
            double distance = calculateDistance(centerLat, centerLon, 
                                              unidad.getCoorY(), unidad.getCoorX());
            coverage.put("distanceFromCenter", distance);
            coverage.put("coversCenter", distance <= COVERAGE_RADIUS);
            
            coverageAreas.add(coverage);
        }
        
        // Determinar si el punto central está cubierto
        boolean isCovered = coverageAreas.stream()
            .anyMatch(area -> (Boolean) area.get("coversCenter"));
        
        analysis.setCoverageAreas(coverageAreas);
        analysis.setUncoveredAreas(uncoveredAreas);
        analysis.setCovered(isCovered);
        
        return analysis;
    }

    /**
     * Obtiene la ruta desde un punto origen a un destino usando OpenRouteService
     */
    public RouteDTO getRoute(double startLat, double startLon, double endLat, double endLon) {
        try {
            String url = String.format(
                "https://api.openrouteservice.org/v2/directions/driving-car?start=%f,%f&end=%f,%f",
                startLon, startLat, endLon, endLat
            );
            
            HttpHeaders headers = new HttpHeaders();
            // Nota: En producción, deberías usar una API key real
            headers.set("Authorization", "Bearer YOUR_API_KEY_HERE");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // Para desarrollo, usaremos una ruta simulada
            RouteDTO route = new RouteDTO();
            route.setStartLat(startLat);
            route.setStartLon(startLon);
            route.setEndLat(endLat);
            route.setEndLon(endLon);
            
            // Calcular distancia directa como aproximación
            double distance = calculateDistance(startLat, startLon, endLat, endLon);
            route.setDistance(distance);
            route.setDuration(distance / 50 * 60); // Aproximación: 50 km/h promedio
            
            // Crear una ruta simple (línea recta)
            List<List<Double>> coordinates = new ArrayList<>();
            coordinates.add(List.of(startLon, startLat));
            coordinates.add(List.of(endLon, endLat));
            route.setCoordinates(coordinates);
            
            route.setInstructions(List.of(
                "Dirigirse hacia el destino",
                String.format("Llegar al destino (%.2f km)", distance / 1000)
            ));
            
            return route;
            
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener la ruta: " + e.getMessage());
        }
    }

    /**
     * Geocodificación inversa - obtiene la dirección de unas coordenadas
     */
    public String reverseGeocode(double lat, double lon) {
        try {
            String url = String.format(
                "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f&zoom=18&addressdetails=1",
                lat, lon
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Geoportal-App/1.0");
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, String.class
            );
            
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            if (jsonNode.has("display_name")) {
                return jsonNode.get("display_name").asText();
            } else {
                return String.format("Coordenadas: %.6f, %.6f", lat, lon);
            }
            
        } catch (Exception e) {
            return String.format("Coordenadas: %.6f, %.6f", lat, lon);
        }
    }

    /**
     * Encuentra zonas sin cobertura médica en un área determinada
     */
    public List<Map<String, Object>> findUncoveredAreas(double minLat, double minLon, 
                                                       double maxLat, double maxLon,
                                                       List<ClinicaComunal> clinicas, 
                                                       List<UnidadMedica> unidades) {
        
        List<Map<String, Object>> uncoveredAreas = new ArrayList<>();
        
        // Crear una grilla de puntos para analizar
        double latStep = (maxLat - minLat) / 20; // 20x20 grid
        double lonStep = (maxLon - minLon) / 20;
        
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                double testLat = minLat + (i * latStep);
                double testLon = minLon + (j * lonStep);
                
                boolean covered = false;
                
                // Verificar si está cubierto por alguna clínica
                for (ClinicaComunal clinica : clinicas) {
                    double distance = calculateDistance(testLat, testLon, 
                                                      clinica.getCoorY(), clinica.getCoorX());
                    if (distance <= COVERAGE_RADIUS) {
                        covered = true;
                        break;
                    }
                }
                
                // Verificar si está cubierto por alguna unidad médica
                if (!covered) {
                    for (UnidadMedica unidad : unidades) {
                        double distance = calculateDistance(testLat, testLon, 
                                                          unidad.getCoorY(), unidad.getCoorX());
                        if (distance <= COVERAGE_RADIUS) {
                            covered = true;
                            break;
                        }
                    }
                }
                
                // Si no está cubierto, agregarlo a las áreas sin cobertura
                if (!covered) {
                    Map<String, Object> uncoveredArea = new HashMap<>();
                    uncoveredArea.put("lat", testLat);
                    uncoveredArea.put("lon", testLon);
                    uncoveredArea.put("severity", "high"); // Podría calcularse basado en densidad poblacional
                    uncoveredAreas.add(uncoveredArea);
                }
            }
        }
        
        return uncoveredAreas;
    }
}