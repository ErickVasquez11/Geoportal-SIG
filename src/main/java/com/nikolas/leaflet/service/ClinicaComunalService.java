package com.nikolas.leaflet.service;

import com.nikolas.leaflet.domain.ClinicaComunal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.io.Serializable;
import java.util.List;

public interface ClinicaComunalService extends Serializable {
    Optional<ClinicaComunal>  clinicaComunalGetOne(Integer id);
    List<ClinicaComunal> clinicaComunalGetAll();
    List<ClinicaComunal> buscarPorMunicipio(String municipio);
    List<ClinicaComunal> findByNombreContaining(String nombre);
    List<String> getDistinctMunicipios(); 
    List<ClinicaComunal> buscarPorMunicipios(List<String> municipio);
       // Nuevo método para paginación
    Page<ClinicaComunal> buscarPorMunicipio(String municipio, Pageable pageable);
    Page<ClinicaComunal> ClinicaComunalGetAll(Pageable pageable);
    ClinicaComunal addClinicaComunal(ClinicaComunal clinicaComunal);
    void deleteClinicaComunal(Integer id);
    ClinicaComunal updateClinicaComunal(Integer id, ClinicaComunal clinicaComunal);
}
