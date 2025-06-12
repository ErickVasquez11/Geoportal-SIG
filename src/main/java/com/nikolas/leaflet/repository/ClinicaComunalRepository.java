package com.nikolas.leaflet.repository;

import com.nikolas.leaflet.domain.ClinicaComunal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClinicaComunalRepository extends JpaRepository<ClinicaComunal, Integer> {

    Optional<ClinicaComunal> findById(Integer id);

    List<ClinicaComunal> findAll();

    List<ClinicaComunal> findByMunicipio(String municipio);

    List<ClinicaComunal> findByNombreContaining(String nombre);

    @Query("SELECT DISTINCT c.municipio FROM ClinicaComunal c")
    List<String> findDistinctMunicipios();

    @Query("SELECT c FROM ClinicaComunal c WHERE c.municipio IN :municipios")
    List<ClinicaComunal> findByMunicipios(List<String> municipios);

    // Método con paginación
    Page<ClinicaComunal> findByMunicipio(String municipio, Pageable pageable);

    Page<ClinicaComunal> findAll(Pageable pageable);

    @Modifying
    @Query("UPDATE ClinicaComunal c SET c.nombre = :nombre, c.direccion = :direccion, c.municipio = :municipio, c.zona = :zona, c.coorX = :coorX, c.coorY = :coorY, c.horarioInicioSemana = :horarioInicioSemana, c.horarioFinSemana = :horarioFinSemana, c.horarioInicioFinde = :horarioInicioFinde, c.horarioFinFinde = :horarioFinFinde WHERE c.id = :id")
    void updateClinicaComunal(@Param("id") Integer id,
            @Param("nombre") String nombre,
            @Param("direccion") String direccion,
            @Param("municipio") String municipio,
            @Param("zona") String zona,
            @Param("coorX") Double coorX,
            @Param("coorY") Double coorY,
            @Param("horarioInicioSemana") String horarioInicioSemana,
            @Param("horarioFinSemana") String horarioFinSemana,
            @Param("horarioInicioFinde") String horarioInicioFinde,
            @Param("horarioFinFinde") String horarioFinFinde);

}
