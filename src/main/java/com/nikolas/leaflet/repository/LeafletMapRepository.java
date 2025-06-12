package com.nikolas.leaflet.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.nikolas.leaflet.domain.LeafletMap;
import java.util.Optional;
public interface LeafletMapRepository extends  JpaRepository<LeafletMap, Integer> {

	Optional<LeafletMap> findById(Integer id);
	
//	LeafletMap saveAndFlush(LeafletMap leafletMap);
}
