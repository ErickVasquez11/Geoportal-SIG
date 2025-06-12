package com.nikolas.leaflet.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.nikolas.leaflet.domain.ClinicaComunal;
import com.nikolas.leaflet.domain.UnidadMedica;
import com.nikolas.leaflet.service.ClinicaComunalService;
import com.nikolas.leaflet.service.UnidadMedicaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.nikolas.leaflet.domain.LeafletMap;
import com.nikolas.leaflet.service.LeafletMapService;
import com.nikolas.leaflet.util.GenericResponse;

@Controller
@RequestMapping("/map")
public class LeafletMapController {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    LeafletMapService leafletMapService;

    @Autowired
    ClinicaComunalService ClinicaComunalService;

    @Autowired
    UnidadMedicaService UnidadMedicaService;

    // Vista principal que muestra tabla de clinicas y mapa
    @RequestMapping(value = "/index")
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size)
            throws ServletException, IOException {

        ModelAndView mav = new ModelAndView();
        try {
            Map<String, Object> myModel = new HashMap<>();
            final LeafletMap leafletMap = this.leafletMapService.leafletMap(2);
            myModel.put("map", leafletMap);

            // Obtener todas las clínicas comunales para el mapa
            List<ClinicaComunal> cvList = ClinicaComunalService.clinicaComunalGetAll();

            // Obtener clínicas comunales paginadas para la tabla
            Pageable pageable = PageRequest.of(page, size);
            Page<ClinicaComunal> pagedClinicas = ClinicaComunalService.ClinicaComunalGetAll(pageable);

            List<String> municipios = ClinicaComunalService.getDistinctMunicipios();

            mav.addObject("centrosMapa", cvList); // Datos completos para el mapa
            mav.addObject("centros", pagedClinicas.getContent()); // Datos paginados para la tabla
            mav.addObject("model", myModel);
            mav.addObject("municipios", municipios);
            mav.addObject("currentPage", page);
            mav.addObject("totalPages", pagedClinicas.getTotalPages());
            mav.addObject("totalItems", pagedClinicas.getTotalElements());
            mav.addObject("size", size);

            mav.setViewName("/map/index");
        } catch (Exception e) {
            mav.setViewName("error");
            mav.addObject("message", e.getMessage());
        }
        return mav;
    }

    // Ruta para actualizar mapa
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public GenericResponse registerUserAccount(@Valid final LeafletMap leafletMap, final HttpServletRequest request) {
        logger.debug("Registering user account with information: {}", leafletMap);
        leafletMapService.updateLeafletMap(leafletMap);
        return new GenericResponse("success");
    }

    // Vista que muestra tabla de unidades y mapa
    @GetMapping("/unidades")
    public ModelAndView ingresarPersona(@RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        ModelAndView mav = new ModelAndView();
        try {
            Map<String, Object> myModel = new HashMap<>();
            final LeafletMap leafletMap = this.leafletMapService.leafletMap(2);
            myModel.put("map", leafletMap);

            // Obtener todas las unidades médicas para el mapa
            List<UnidadMedica> todasUnidades = UnidadMedicaService.getAllEntidadesMedicas();
            System.out.println("Total de unidades médicas para el mapa: " + todasUnidades.size());

            // Obtener unidades médicas paginadas para la tabla
            Pageable pageable = PageRequest.of(page, size);
            Page<UnidadMedica> centrosPage = UnidadMedicaService.getAllEntidadesMedicas(pageable);
            System.out.println("Total de unidades médicas paginadas: " + centrosPage.getTotalElements());

            // Obtener lista de municipios
            List<String> municipios = UnidadMedicaService.getDistinctMunicipios();
            System.out.println("Total de municipios: " + municipios.size());
            System.out.println("Lista de municipios: " + municipios);

            mav.addObject("centrosMapa", todasUnidades); // Datos completos para el mapa
            mav.addObject("centros", centrosPage.getContent()); // Datos paginados para la tabla
            mav.addObject("model", myModel);
            mav.addObject("currentPage", page);
            mav.addObject("totalPages", centrosPage.getTotalPages());
            mav.addObject("totalItems", centrosPage.getTotalElements());
            mav.addObject("size", size);
            mav.addObject("municipios", municipios);
            mav.setViewName("/map/unidades");
        } catch (Exception e) {
            System.err.println("Error al procesar la solicitud: " + e.getMessage());
            e.printStackTrace();
            mav.setViewName("error");
            mav.addObject("message", e.getMessage());
        }
        return mav;
    }

    // Vista principal que muestra información detallada del centro de asistencia
    @GetMapping("/informacion")
    public ModelAndView mostrarInformacion(@RequestParam("id") int id) {
        List<String> debugMessages = new ArrayList<>();
        debugMessages.add("Handling request to /informacion with id: " + id);

        ModelAndView mav = new ModelAndView();

        try {
            Optional<ClinicaComunal> clinicaOpt = ClinicaComunalService.clinicaComunalGetOne(id);
            if (!clinicaOpt.isPresent()) {
                debugMessages.add("Clinica no encontrada con id: " + id);
                mav.addObject("error", "Clinica no encontrada");
                mav.addObject("debugMessages", debugMessages);
                mav.setViewName("error");
                return mav;
            }

            ClinicaComunal clinica = clinicaOpt.get();
            mav.addObject("clinica", clinica);
            debugMessages.add("Returning ModelAndView for /informacion with clinica: " + clinica);
            mav.setViewName("map/informacion");
        } catch (Exception e) {
            debugMessages.add("Error al manejar la solicitud a /informacion");
            debugMessages.add(e.getMessage());
            mav.addObject("error", e.getMessage());
            mav.setViewName("error");
        }

        mav.addObject("debugMessages", debugMessages);
        return mav;
    }

}
