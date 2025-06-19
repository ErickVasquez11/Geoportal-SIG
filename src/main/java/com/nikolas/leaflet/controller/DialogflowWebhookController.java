package com.nikolas.leaflet.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nikolas.leaflet.domain.ClinicaComunal;
import com.nikolas.leaflet.domain.UnidadMedica;
import com.nikolas.leaflet.dto.DialogFlowRequest;
import com.nikolas.leaflet.dto.RouteDTO;
import com.nikolas.leaflet.service.ClinicaComunalService;
import com.nikolas.leaflet.service.UnidadMedicaService;
import com.nikolas.leaflet.service.SpatialAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/dialogflow")
public class DialogflowWebhookController {

    @Autowired
    private ClinicaComunalService clinicaComunalService;

    @Autowired
    private UnidadMedicaService unidadMedicaService;

    @Autowired
    private SpatialAnalysisService spatialAnalysisService;

    @PostMapping("/webhook")
    public ResponseEntity<JsonObject> handleDialogflowRequest(@RequestBody DialogFlowRequest request) {
        try {
            String intentName = request.getQueryResult().getIntent().getDisplayName();
            switch (intentName) {
                case "buscarClinicasPorMunicipio":
                    return buscarClinicasPorMunicipio(request);
                case "buscarHorarioClinica":
                    return buscarHorarioClinica(request);
                case "buscarUnidadesMedicasPorMunicipio":
                    return buscarUnidadesMedicas(request);
                case "buscarHorarioUnidadMedica":
                    return buscarHorarioUnidadMedica(request);
                case "ServiciosOfrecidos":
                    return serviciosOfrecidos();
                case "buscarRutaCentroMasCercano":
                    return buscarRutaCentroMasCercano(request);
                case "analizarCoberturaMedica":
                    return analizarCoberturaMedica(request);
                case "obtenerDireccionCoordenadas":
                    return obtenerDireccionCoordenadas(request);
                default:
                    return intentNoReconocido();
            }
        } catch (Exception e) {
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("error", "Internal Server Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorJson);
        }
    }

    private ResponseEntity<JsonObject> buscarClinicasPorMunicipio(DialogFlowRequest request) {
        String municipio = extractLocation(request);
        List<ClinicaComunal> clinicas = clinicaComunalService.buscarPorMunicipio(municipio);
        return ResponseEntity.ok(createClinicasJsonResponse(municipio, clinicas));
    }

    private ResponseEntity<JsonObject> buscarHorarioClinica(DialogFlowRequest request) {
        String municipio = extractLocation(request);
        List<ClinicaComunal> clinicas = clinicaComunalService.findByNombreContaining(municipio);
        return ResponseEntity.ok(createHorariosJsonResponse(municipio, clinicas));
    }

    private ResponseEntity<JsonObject> buscarUnidadesMedicas(DialogFlowRequest request) {
        String municipio = extractLocation(request);
        List<UnidadMedica> unidades = unidadMedicaService.buscarPorMunicipio(municipio);
        return ResponseEntity.ok(createUnidadesJsonResponse(municipio, unidades));
    }

    private ResponseEntity<JsonObject> buscarHorarioUnidadMedica(DialogFlowRequest request) {
        String municipio = extractLocation(request);
        List<UnidadMedica> unidades = unidadMedicaService.buscarPorMunicipio(municipio);
        return ResponseEntity.ok(createHorariosUnidadJsonResponse(municipio, unidades));
    }

    private ResponseEntity<JsonObject> serviciosOfrecidos() {
        return ResponseEntity.ok(createServiciosJsonResponse());
    }

    private ResponseEntity<JsonObject> buscarRutaCentroMasCercano(DialogFlowRequest request) {
        try {
            Map<String, Object> params = request.getQueryResult().getParameters();
            
            // Extraer coordenadas del usuario (esto debería venir del contexto o parámetros)
            double userLat = 13.693399179677684; // Coordenadas por defecto de San Salvador
            double userLon = -89.21802884256014;
            
            // Si hay coordenadas en los parámetros, usarlas
            if (params.containsKey("lat") && params.containsKey("lon")) {
                userLat = Double.parseDouble(params.get("lat").toString());
                userLon = Double.parseDouble(params.get("lon").toString());
            }

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
                    nearestCenter.put("name", clinica.getNombre());
                    nearestCenter.put("type", "clínica");
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
                    nearestCenter.put("name", unidad.getNombre());
                    nearestCenter.put("type", "unidad médica");
                    nearestCenter.put("address", unidad.getDireccion());
                    nearestCenter.put("distance", distance);
                }
            }

            JsonObject response = new JsonObject();
            if (nearestCenter != null) {
                String message = String.format(
                    "El centro médico más cercano es %s (%s), ubicado en %s. " +
                    "Se encuentra a aproximadamente %.2f kilómetros de tu ubicación. " +
                    "Para obtener direcciones detalladas, puedes usar el mapa interactivo.",
                    nearestCenter.get("name"),
                    nearestCenter.get("type"),
                    nearestCenter.get("address"),
                    (Double) nearestCenter.get("distance") / 1000
                );
                response.addProperty("fulfillmentText", message);
            } else {
                response.addProperty("fulfillmentText", "No se encontraron centros médicos cercanos.");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("fulfillmentText", "Hubo un error al buscar el centro médico más cercano.");
            return ResponseEntity.ok(response);
        }
    }

    private ResponseEntity<JsonObject> analizarCoberturaMedica(DialogFlowRequest request) {
        try {
            Map<String, Object> params = request.getQueryResult().getParameters();
            
            // Coordenadas por defecto de San Salvador
            double lat = 13.693399179677684;
            double lon = -89.21802884256014;
            
            // Si hay coordenadas en los parámetros, usarlas
            if (params.containsKey("lat") && params.containsKey("lon")) {
                lat = Double.parseDouble(params.get("lat").toString());
                lon = Double.parseDouble(params.get("lon").toString());
            }

            List<ClinicaComunal> clinicas = clinicaComunalService.clinicaComunalGetAll();
            List<UnidadMedica> unidades = unidadMedicaService.getAllEntidadesMedicas();

            // Contar centros médicos en un radio de 1km
            int centrosCercanos = 0;
            for (ClinicaComunal clinica : clinicas) {
                double distance = spatialAnalysisService.calculateDistance(
                    lat, lon, clinica.getCoorY(), clinica.getCoorX()
                );
                if (distance <= 1000) { // 1km
                    centrosCercanos++;
                }
            }

            for (UnidadMedica unidad : unidades) {
                double distance = spatialAnalysisService.calculateDistance(
                    lat, lon, unidad.getCoorY(), unidad.getCoorX()
                );
                if (distance <= 1000) { // 1km
                    centrosCercanos++;
                }
            }

            JsonObject response = new JsonObject();
            String message;
            
            if (centrosCercanos == 0) {
                message = "Esta área no tiene cobertura médica en un radio de 1 kilómetro. " +
                         "Se recomienda buscar centros médicos en áreas cercanas.";
            } else if (centrosCercanos == 1) {
                message = "Esta área tiene cobertura médica limitada con 1 centro médico en un radio de 1 kilómetro.";
            } else {
                message = String.format(
                    "Esta área tiene buena cobertura médica con %d centros médicos en un radio de 1 kilómetro.",
                    centrosCercanos
                );
            }

            response.addProperty("fulfillmentText", message);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("fulfillmentText", "Hubo un error al analizar la cobertura médica.");
            return ResponseEntity.ok(response);
        }
    }

    private ResponseEntity<JsonObject> obtenerDireccionCoordenadas(DialogFlowRequest request) {
        try {
            Map<String, Object> params = request.getQueryResult().getParameters();
            
            if (params.containsKey("lat") && params.containsKey("lon")) {
                double lat = Double.parseDouble(params.get("lat").toString());
                double lon = Double.parseDouble(params.get("lon").toString());
                
                String address = spatialAnalysisService.reverseGeocode(lat, lon);
                
                JsonObject response = new JsonObject();
                response.addProperty("fulfillmentText", 
                    String.format("Las coordenadas %.6f, %.6f corresponden a: %s", lat, lon, address));
                return ResponseEntity.ok(response);
            } else {
                JsonObject response = new JsonObject();
                response.addProperty("fulfillmentText", 
                    "Por favor proporciona las coordenadas (latitud y longitud) para obtener la dirección.");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            JsonObject response = new JsonObject();
            response.addProperty("fulfillmentText", "Hubo un error al obtener la dirección de las coordenadas.");
            return ResponseEntity.ok(response);
        }
    }

    private ResponseEntity<JsonObject> intentNoReconocido() {
        JsonObject response = new JsonObject();
        response.addProperty("fulfillmentText", "No valid intent matched.");
        return ResponseEntity.ok(response);
    }

    private String extractLocation(DialogFlowRequest request) {
        Map<String, Object> params = request.getQueryResult().getParameters();
        Object locationObj = params.get("location");

        if (locationObj instanceof List) {
            List<Map<String, String>> locationList = (List<Map<String, String>>) locationObj;
            if (!locationList.isEmpty() && locationList.get(0).containsKey("city")) {
                return locationList.get(0).get("city");
            }
        }
        return "Unknown";
    }

    private JsonObject createClinicasJsonResponse(String municipio, List<ClinicaComunal> clinicas) {
        JsonObject response = new JsonObject();
        JsonArray messages = new JsonArray();
        JsonObject textMessage = new JsonObject();

        if (clinicas.isEmpty()) {
            textMessage.addProperty("fulfillmentText", "No se encontraron clínicas en el municipio especificado.");
        } else {
            textMessage.addProperty("fulfillmentText", "Aquí tienes las clínicas en " + municipio + ":");
        }

        messages.add(textMessage);
        response.add("fulfillmentMessages", messages);
        return response;
    }

    private JsonObject createHorariosJsonResponse(String municipio, List<ClinicaComunal> clinicas) {
        JsonObject response = new JsonObject();
        JsonArray messages = new JsonArray();
        JsonObject textMessage = new JsonObject();

        if (clinicas.isEmpty()) {
            textMessage.addProperty("fulfillmentText", "No se encontraron horarios para las clínicas en " + municipio + ".");
        } else {
            JsonArray horarios = new JsonArray();
            clinicas.forEach(clinica -> horarios.add(clinica.getNombre() + ": " +
                    clinica.getHorarioInicioSemana() + " - " + clinica.getHorarioFinSemana()));
            textMessage.add("fulfillmentText", horarios);
        }

        messages.add(textMessage);
        response.add("fulfillmentMessages", messages);
        return response;
    }

    private JsonObject createUnidadesJsonResponse(String municipio, List<UnidadMedica> unidades) {
        JsonObject response = new JsonObject();
        JsonArray messages = new JsonArray();
        JsonObject textMessage = new JsonObject();

        if (unidades.isEmpty()) {
            textMessage.addProperty("fulfillmentText", "No se encontraron unidades médicas en " + municipio + ".");
        } else {
            textMessage.addProperty("fulfillmentText", "Aquí tienes las unidades médicas en " + municipio + ":");
        }

        messages.add(textMessage);
        response.add("fulfillmentMessages", messages);
        return response;
    }

    private JsonObject createHorariosUnidadJsonResponse(String municipio, List<UnidadMedica> unidades) {
        JsonObject response = new JsonObject();
        JsonArray messages = new JsonArray();
        JsonObject textMessage = new JsonObject();

        if (unidades.isEmpty()) {
            textMessage.addProperty("fulfillmentText", "No se encontraron horarios para las unidades médicas en " + municipio + ".");
        } else {
            JsonArray horarios = new JsonArray();
            unidades.forEach(unidad -> horarios.add(unidad.getNombre() + ": " +
                    unidad.getHorarioInicioSemana() + " - " + unidad.getHorarioFinSemana()));
            textMessage.add("fulfillmentText", horarios);
        }

        messages.add(textMessage);
        response.add("fulfillmentMessages", messages);
        return response;
    }

    private JsonObject createServiciosJsonResponse() {
        JsonObject response = new JsonObject();
        JsonArray messages = new JsonArray();
        JsonObject textMessage = new JsonObject();

        textMessage.addProperty("fulfillmentText", "Aquí tienes los servicios ofrecidos.");
        messages.add(textMessage);
        response.add("fulfillmentMessages", messages);

        return response;
    }
}