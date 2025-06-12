package com.nikolas.leaflet.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nikolas.leaflet.domain.ClinicaComunal;
import com.nikolas.leaflet.domain.UnidadMedica;
import com.nikolas.leaflet.dto.DialogFlowRequest;
import com.nikolas.leaflet.service.ClinicaComunalService;
import com.nikolas.leaflet.service.UnidadMedicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dialogflow")
public class DialogflowWebhookController {

    @Autowired
    private ClinicaComunalService clinicaComunalService;

    @Autowired
    private UnidadMedicaService unidadMedicaService;

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
