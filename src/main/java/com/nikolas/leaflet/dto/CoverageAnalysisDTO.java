package com.nikolas.leaflet.dto;

import java.util.List;
import java.util.Map;

public class CoverageAnalysisDTO {
    private double centerLat;
    private double centerLon;
    private boolean covered;
    private List<Map<String, Object>> coverageAreas;
    private List<Map<String, Object>> uncoveredAreas;
    private double totalCoverageRadius;

    // Constructors
    public CoverageAnalysisDTO() {}

    public CoverageAnalysisDTO(double centerLat, double centerLon, boolean covered, 
                              List<Map<String, Object>> coverageAreas, 
                              List<Map<String, Object>> uncoveredAreas) {
        this.centerLat = centerLat;
        this.centerLon = centerLon;
        this.covered = covered;
        this.coverageAreas = coverageAreas;
        this.uncoveredAreas = uncoveredAreas;
    }

    // Getters and Setters
    public double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(double centerLat) {
        this.centerLat = centerLat;
    }

    public double getCenterLon() {
        return centerLon;
    }

    public void setCenterLon(double centerLon) {
        this.centerLon = centerLon;
    }

    public boolean isCovered() {
        return covered;
    }

    public void setCovered(boolean covered) {
        this.covered = covered;
    }

    public List<Map<String, Object>> getCoverageAreas() {
        return coverageAreas;
    }

    public void setCoverageAreas(List<Map<String, Object>> coverageAreas) {
        this.coverageAreas = coverageAreas;
    }

    public List<Map<String, Object>> getUncoveredAreas() {
        return uncoveredAreas;
    }

    public void setUncoveredAreas(List<Map<String, Object>> uncoveredAreas) {
        this.uncoveredAreas = uncoveredAreas;
    }

    public double getTotalCoverageRadius() {
        return totalCoverageRadius;
    }

    public void setTotalCoverageRadius(double totalCoverageRadius) {
        this.totalCoverageRadius = totalCoverageRadius;
    }
}