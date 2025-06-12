package com.nikolas.leaflet.repository;

import com.nikolas.leaflet.domain.UnidadMedica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface UnidadMedicaRepository extends JpaRepository<UnidadMedica, Integer> {
     Optional<UnidadMedica> findById(Integer id);

    List<UnidadMedica> findAll();

    List<UnidadMedica> findByMunicipio(String municipio);

    List<UnidadMedica> findByNombreContaining(String nombre);

    @Query("SELECT DISTINCT u.municipio FROM UnidadMedica u")
    List<String> findDistinctMunicipios();

    @Query("SELECT u FROM UnidadMedica u WHERE u.municipio IN :municipios")
    List<UnidadMedica> findByMunicipios(@Param("municipios") List<String> municipios);

    // Método con paginación
    Page<UnidadMedica> findByMunicipio(String municipio, Pageable pageable);

    Page<UnidadMedica> findAll(Pageable pageable);

    @Modifying
    @Query("UPDATE UnidadMedica u SET u.nombre = :nombre, u.direccion = :direccion, u.municipio = :municipio, u.zona = :zona, u.coorX = :coorX, u.coorY = :coorY, u.horarioInicioSemana = :horarioInicioSemana, u.horarioFinSemana = :horarioFinSemana, u.horarioInicioFinde = :horarioInicioFinde, u.horarioFinFinde = :horarioFinFinde WHERE u.id = :id")
    void updateUnidadMedica(@Param("nombre") String nombre, 
                            @Param("direccion") String direccion, 
                            @Param("municipio") String municipio, 
                            @Param("zona") String zona, 
                            @Param("coorX") Double coorX, 
                            @Param("coorY") Double coorY, 
                            @Param("horarioInicioSemana") String horarioInicioSemana, 
                            @Param("horarioFinSemana") String horarioFinSemana, 
                            @Param("horarioInicioFinde") String horarioInicioFinde, 
                            @Param("horarioFinFinde") String horarioFinFinde, 
                            @Param("id") Integer id);
}