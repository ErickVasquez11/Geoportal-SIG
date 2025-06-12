package com.nikolas.leaflet.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "clinicas_comunales")
public class ClinicaComunal implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Assuming using auto-increment column
    private Integer id;

    @Column(name = "coor_x")
    private Double coorX;

    @Column(name = "coor_y")
    private Double coorY;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "municipio")
    private String municipio;

    @Column(name = "zona")
    private String zona;

    @Column(name = "horario_inicio_semana")
    private String horarioInicioSemana;

    @Column(name = "horario_fin_semana")
    private String horarioFinSemana;

    @Column(name = "horario_inicio_finde")
    private String horarioInicioFinde;

    @Column(name = "horario_fin_finde")
    private String horarioFinFinde;

    // Getters and setters

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getCoorX() {
        return coorX;
    }

    public void setCoorX(Double coorX) {
        this.coorX = coorX;
    }

    public Double getCoorY() {
        return coorY;
    }

    public void setCoorY(Double coorY) {
        this.coorY = coorY;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getZona() {
        return zona;
    }

    public void setZona(String zona) {
        this.zona = zona;
    }

    public void setHorarioInicioSemana(String horarioInicioSemana) {
        this.horarioInicioSemana = horarioInicioSemana;
    }
    public String getHorarioInicioSemana() {
        return horarioInicioSemana;
    }
    public String getHorarioFinSemana() {
        return horarioFinSemana;
    }

    public void setHorarioFinSemana(String horarioFinSemana) {
        this.horarioFinSemana = horarioFinSemana;
    }

    public String getHorarioInicioFinde() {
        return horarioInicioFinde;
    }

    public void setHorarioInicioFinde(String horarioInicioFinde) {
        this.horarioInicioFinde = horarioInicioFinde;
    }

    public String getHorarioFinFinde() {
        return horarioFinFinde;
    }

    public void setHorarioFinFinde(String horarioFinFinde) {
        this.horarioFinFinde = horarioFinFinde;
    }
}
