package com.nikolas.leaflet.service;

import java.util.Optional;
import com.nikolas.leaflet.domain.ClinicaComunal;
import com.nikolas.leaflet.repository.ClinicaComunalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ClinicaComunalServiceImpl implements ClinicaComunalService {
    @Autowired
    ClinicaComunalRepository ClinicaComunalRepository;

    @Override
    public List<ClinicaComunal> clinicaComunalGetAll() {
        return ClinicaComunalRepository.findAll();
    }

    @Override
    public List<ClinicaComunal> buscarPorMunicipio(String municipio) {
        return ClinicaComunalRepository.findByMunicipio(municipio);
    }

    @Override
    public Optional<ClinicaComunal> clinicaComunalGetOne(Integer id) {

        return ClinicaComunalRepository.findById(id);
    }

    @Override
    public List<ClinicaComunal> findByNombreContaining(String nombre) {
        return ClinicaComunalRepository.findByNombreContaining(nombre);
    }

    public List<String> getDistinctMunicipios() {
        return ClinicaComunalRepository.findDistinctMunicipios();
    }

    @Override
    public List<ClinicaComunal> buscarPorMunicipios(List<String> municipios) {
        return ClinicaComunalRepository.findByMunicipios(municipios);
    }

    // Implementación del nuevo método de paginación
    @Override
    public Page<ClinicaComunal> ClinicaComunalGetAll(Pageable pageable) {
        return ClinicaComunalRepository.findAll(pageable);
    }

    @Override
    public Page<ClinicaComunal> buscarPorMunicipio(String municipio, Pageable pageable) {
        return ClinicaComunalRepository.findByMunicipio(municipio, pageable);
    }

    @Override
    public ClinicaComunal addClinicaComunal(ClinicaComunal clinicaComunal) {
        return ClinicaComunalRepository.save(clinicaComunal);

    }

    @Override
    public void deleteClinicaComunal(Integer id) {
        ClinicaComunalRepository.deleteById(id);
    }

    @Override
    public ClinicaComunal updateClinicaComunal(Integer id, ClinicaComunal clinicaComunal) {
        Optional<ClinicaComunal> existingClinicaComunalOpt = ClinicaComunalRepository.findById(id);
        if (existingClinicaComunalOpt.isPresent()) {
            ClinicaComunal existingClinicaComunal = existingClinicaComunalOpt.get();
            existingClinicaComunal.setNombre(clinicaComunal.getNombre());
            existingClinicaComunal.setDireccion(clinicaComunal.getDireccion());
            existingClinicaComunal.setMunicipio(clinicaComunal.getMunicipio());
            existingClinicaComunal.setZona(clinicaComunal.getZona());
            existingClinicaComunal.setCoorX(clinicaComunal.getCoorX());
            existingClinicaComunal.setCoorY(clinicaComunal.getCoorY());
            existingClinicaComunal.setHorarioInicioSemana(clinicaComunal.getHorarioInicioSemana());
            existingClinicaComunal.setHorarioFinSemana(clinicaComunal.getHorarioFinSemana());
            existingClinicaComunal.setHorarioInicioFinde(clinicaComunal.getHorarioInicioFinde());
            existingClinicaComunal.setHorarioFinFinde(clinicaComunal.getHorarioFinFinde());
            return ClinicaComunalRepository.save(existingClinicaComunal);
        } else {
            throw new RuntimeException("ClinicaComunal no encontrada con ID " + id);
        }
    }

}
