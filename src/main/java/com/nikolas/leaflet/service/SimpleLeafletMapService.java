package com.nikolas.leaflet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import com.nikolas.leaflet.domain.LeafletMap;
import com.nikolas.leaflet.repository.LeafletMapRepository;

@Component
public class SimpleLeafletMapService implements LeafletMapService {
	@Autowired
	LeafletMapRepository leafletMapRepository;

	private static final long serialVersionUID = 1L;

	@Override
    public LeafletMap leafletMap(Integer id) {
        Optional<LeafletMap> leafletMap = leafletMapRepository.findById(id);
        return leafletMap.orElse(null); // O cualquier lógica que desees implementar cuando no se encuentra el objeto
    }



	@Override
	public LeafletMap updateLeafletMap(LeafletMap leafletMap) {
		leafletMap = leafletMapRepository.saveAndFlush(leafletMap);
		return leafletMap;
	}



}
