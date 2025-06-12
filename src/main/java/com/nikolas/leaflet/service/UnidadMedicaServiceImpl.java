package com.nikolas.leaflet.service;

import com.nikolas.leaflet.domain.UnidadMedica;
import com.nikolas.leaflet.repository.UnidadMedicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UnidadMedicaServiceImpl implements UnidadMedicaService {
    @Autowired
    private UnidadMedicaRepository UnidadMedicaRepository;

    @Override
    public Optional<UnidadMedica> getUnidadMedicaById(Integer id) {
        return UnidadMedicaRepository.findById(id);
    }

    @Override
    public List<UnidadMedica> getAllEntidadesMedicas() {
        return UnidadMedicaRepository.findAll();
    }

    @Override
    public List<UnidadMedica> buscarPorNombre(String nombre) {
        return UnidadMedicaRepository.findByNombreContaining(nombre);
    }

    @Override
    public List<UnidadMedica> buscarPorMunicipio(String municipio) {
        return UnidadMedicaRepository.findByMunicipio(municipio);
    }

    @Override
    public Page<UnidadMedica> buscarPorMunicipio(String municipio, Pageable pageable) {
        return UnidadMedicaRepository.findByMunicipio(municipio, pageable);
    }

    @Override
    public Page<UnidadMedica> getAllEntidadesMedicas(Pageable pageable) {
        return UnidadMedicaRepository.findAll(pageable);
    }

    @Override
    public UnidadMedica addUnidadMedica(UnidadMedica UnidadMedica) {
        return UnidadMedicaRepository.save(UnidadMedica);
    }

    @Override
    public void deleteUnidadMedica(Integer id) {
        UnidadMedicaRepository.deleteById(id);
    }

    @Override
    public UnidadMedica updateUnidadMedica(Integer id, UnidadMedica UnidadMedica) {
        Optional<UnidadMedica> existingUnidadMedicaOpt = UnidadMedicaRepository.findById(id);
        if (existingUnidadMedicaOpt.isPresent()) {
            UnidadMedica existingUnidadMedica = existingUnidadMedicaOpt.get();
            existingUnidadMedica.setNombre(UnidadMedica.getNombre());
            existingUnidadMedica.setDireccion(UnidadMedica.getDireccion());
            existingUnidadMedica.setMunicipio(UnidadMedica.getMunicipio());
            existingUnidadMedica.setZona(UnidadMedica.getZona());
            existingUnidadMedica.setCoorX(UnidadMedica.getCoorX());
            existingUnidadMedica.setCoorY(UnidadMedica.getCoorY());
            existingUnidadMedica.setHorarioInicioSemana(UnidadMedica.getHorarioInicioSemana());
            existingUnidadMedica.setHorarioFinSemana(UnidadMedica.getHorarioFinSemana());
            existingUnidadMedica.setHorarioInicioFinde(UnidadMedica.getHorarioInicioFinde());
            existingUnidadMedica.setHorarioFinFinde(UnidadMedica.getHorarioFinFinde());
            return UnidadMedicaRepository.save(existingUnidadMedica);
        } else {
            throw new RuntimeException("UnidadMedica no encontrada con ID " + id);
        }
    }

    @Override
    public List<String> getDistinctMunicipios() {
        return UnidadMedicaRepository.findDistinctMunicipios();
    }
}