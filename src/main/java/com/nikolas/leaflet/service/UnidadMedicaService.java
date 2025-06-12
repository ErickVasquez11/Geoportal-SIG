package com.nikolas.leaflet.service;

import com.nikolas.leaflet.domain.UnidadMedica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface UnidadMedicaService extends Serializable {
    Optional<UnidadMedica> getUnidadMedicaById(Integer id);

    List<UnidadMedica> getAllEntidadesMedicas();

    List<String> getDistinctMunicipios();

    List<UnidadMedica> buscarPorNombre(String nombre);

    List<UnidadMedica> buscarPorMunicipio(String municipio);

    Page<UnidadMedica> buscarPorMunicipio(String municipio, Pageable pageable);

    Page<UnidadMedica> getAllEntidadesMedicas(Pageable pageable);

    UnidadMedica addUnidadMedica(UnidadMedica UnidadMedica);

    void deleteUnidadMedica(Integer id);

    UnidadMedica updateUnidadMedica(Integer id, UnidadMedica UnidadMedica);
}