// Funciones para análisis espacial y ruteo

class SpatialAnalysis {
    constructor(map) {
        this.map = map;
        this.coverageLayer = null;
        this.routeLayer = null;
        this.uncoveredAreasLayer = null;
        this.userLocation = null;
    }

    // Analizar cobertura médica en una ubicación
    async analyzeCoverage(lat, lon) {
        try {
            const response = await fetch('/api/spatial/coverage-analysis', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ lat: lat, lon: lon })
            });

            if (!response.ok) {
                throw new Error('Error al analizar cobertura');
            }

            const analysis = await response.json();
            this.displayCoverageAnalysis(analysis);
            return analysis;

        } catch (error) {
            console.error('Error en análisis de cobertura:', error);
            alert('Error al analizar la cobertura médica');
        }
    }

    // Mostrar análisis de cobertura en el mapa
    displayCoverageAnalysis(analysis) {
        // Limpiar capas anteriores
        if (this.coverageLayer) {
            this.map.removeLayer(this.coverageLayer);
        }

        this.coverageLayer = L.layerGroup();

        // Mostrar áreas de cobertura
        analysis.coverageAreas.forEach(area => {
            const circle = L.circle([area.lat, area.lon], {
                radius: area.radius,
                color: area.coversCenter ? '#4CAF50' : '#FFC107',
                fillColor: area.coversCenter ? '#4CAF50' : '#FFC107',
                fillOpacity: 0.2,
                weight: 2
            }).bindPopup(`
                <strong>${area.name}</strong><br>
                Tipo: ${area.type}<br>
                Distancia: ${(area.distanceFromCenter / 1000).toFixed(2)} km<br>
                Cobertura: ${area.coversCenter ? 'Sí' : 'No'}
            `);

            this.coverageLayer.addLayer(circle);
        });

        // Marcar el punto de análisis
        const analysisMarker = L.marker([analysis.centerLat, analysis.centerLon], {
            icon: L.icon({
                iconUrl: '/images/analysis-point.png',
                iconSize: [32, 32],
                iconAnchor: [16, 32],
                popupAnchor: [0, -32]
            })
        }).bindPopup(`
            <strong>Punto de Análisis</strong><br>
            Cobertura: ${analysis.covered ? 'Cubierto' : 'No cubierto'}<br>
            Coordenadas: ${analysis.centerLat.toFixed(6)}, ${analysis.centerLon.toFixed(6)}
        `);

        this.coverageLayer.addLayer(analysisMarker);
        this.coverageLayer.addTo(this.map);

        // Mostrar información en la interfaz
        this.showCoverageInfo(analysis);
    }

    // Obtener ruta a un centro médico
    async getRoute(startLat, startLon, endLat, endLon) {
        try {
            const response = await fetch('/api/spatial/route', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    startLat: startLat,
                    startLon: startLon,
                    endLat: endLat,
                    endLon: endLon
                })
            });

            if (!response.ok) {
                throw new Error('Error al obtener ruta');
            }

            const route = await response.json();
            this.displayRoute(route);
            return route;

        } catch (error) {
            console.error('Error al obtener ruta:', error);
            alert('Error al calcular la ruta');
        }
    }

    // Mostrar ruta en el mapa
    displayRoute(route) {
        // Limpiar ruta anterior
        if (this.routeLayer) {
            this.map.removeLayer(this.routeLayer);
        }

        this.routeLayer = L.layerGroup();

        // Crear línea de ruta
        const routeLine = L.polyline(
            route.coordinates.map(coord => [coord[1], coord[0]]), // Convertir [lon, lat] a [lat, lon]
            {
                color: '#2196F3',
                weight: 4,
                opacity: 0.8
            }
        ).bindPopup(`
            <strong>Ruta</strong><br>
            Distancia: ${(route.distance / 1000).toFixed(2)} km<br>
            Tiempo estimado: ${Math.round(route.duration)} minutos
        `);

        this.routeLayer.addLayer(routeLine);

        // Marcadores de inicio y fin
        const startMarker = L.marker([route.startLat, route.startLon], {
            icon: L.icon({
                iconUrl: '/images/start-marker.png',
                iconSize: [32, 32],
                iconAnchor: [16, 32]
            })
        }).bindPopup(`<strong>Inicio</strong><br>${route.startAddress || 'Ubicación de inicio'}`);

        const endMarker = L.marker([route.endLat, route.endLon], {
            icon: L.icon({
                iconUrl: '/images/end-marker.png',
                iconSize: [32, 32],
                iconAnchor: [16, 32]
            })
        }).bindPopup(`<strong>Destino</strong><br>${route.endAddress || 'Centro médico'}`);

        this.routeLayer.addLayer(startMarker);
        this.routeLayer.addLayer(endMarker);
        this.routeLayer.addTo(this.map);

        // Ajustar vista del mapa para mostrar toda la ruta
        const bounds = L.latLngBounds([
            [route.startLat, route.startLon],
            [route.endLat, route.endLon]
        ]);
        this.map.fitBounds(bounds, { padding: [20, 20] });

        // Mostrar instrucciones
        this.showRouteInstructions(route);
    }

    // Obtener centro médico más cercano y ruta
    async getNearestCenterRoute(lat, lon) {
        try {
            const response = await fetch('/api/spatial/nearest-center-route', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ lat: lat, lon: lon })
            });

            if (!response.ok) {
                throw new Error('Error al obtener centro más cercano');
            }

            const data = await response.json();
            this.displayRoute(data.route);
            this.showNearestCenterInfo(data.nearestCenter);
            return data;

        } catch (error) {
            console.error('Error al obtener centro más cercano:', error);
            alert('Error al buscar el centro médico más cercano');
        }
    }

    // Encontrar áreas sin cobertura
    async findUncoveredAreas(bounds) {
        try {
            const response = await fetch('/api/spatial/uncovered-areas', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    minLat: bounds.getSouth(),
                    minLon: bounds.getWest(),
                    maxLat: bounds.getNorth(),
                    maxLon: bounds.getEast()
                })
            });

            if (!response.ok) {
                throw new Error('Error al obtener áreas sin cobertura');
            }

            const uncoveredAreas = await response.json();
            this.displayUncoveredAreas(uncoveredAreas);
            return uncoveredAreas;

        } catch (error) {
            console.error('Error al obtener áreas sin cobertura:', error);
            alert('Error al analizar áreas sin cobertura');
        }
    }

    // Mostrar áreas sin cobertura
    displayUncoveredAreas(uncoveredAreas) {
        // Limpiar capas anteriores
        if (this.uncoveredAreasLayer) {
            this.map.removeLayer(this.uncoveredAreasLayer);
        }

        this.uncoveredAreasLayer = L.layerGroup();

        uncoveredAreas.forEach(area => {
            const marker = L.circleMarker([area.lat, area.lon], {
                radius: 8,
                color: '#F44336',
                fillColor: '#F44336',
                fillOpacity: 0.6,
                weight: 2
            }).bindPopup(`
                <strong>Área sin cobertura</strong><br>
                Coordenadas: ${area.lat.toFixed(6)}, ${area.lon.toFixed(6)}<br>
                Severidad: ${area.severity}
            `);

            this.uncoveredAreasLayer.addLayer(marker);
        });

        this.uncoveredAreasLayer.addTo(this.map);
    }

    // Geocodificación inversa
    async reverseGeocode(lat, lon) {
        try {
            const response = await fetch(`/api/spatial/reverse-geocode?lat=${lat}&lon=${lon}`);
            
            if (!response.ok) {
                throw new Error('Error en geocodificación inversa');
            }

            const data = await response.json();
            return data.address;

        } catch (error) {
            console.error('Error en geocodificación inversa:', error);
            return `Coordenadas: ${lat.toFixed(6)}, ${lon.toFixed(6)}`;
        }
    }

    // Mostrar información de cobertura
    showCoverageInfo(analysis) {
        const infoDiv = document.getElementById('coverage-info') || this.createInfoDiv('coverage-info');
        
        infoDiv.innerHTML = `
            <div class="bg-white p-4 rounded-lg shadow-md">
                <h3 class="text-lg font-bold mb-2">Análisis de Cobertura Médica</h3>
                <p><strong>Estado:</strong> ${analysis.covered ? 
                    '<span class="text-green-600">Área cubierta</span>' : 
                    '<span class="text-red-600">Área sin cobertura</span>'}</p>
                <p><strong>Centros médicos en el área:</strong> ${analysis.coverageAreas.length}</p>
                <p><strong>Coordenadas:</strong> ${analysis.centerLat.toFixed(6)}, ${analysis.centerLon.toFixed(6)}</p>
                <button onclick="spatialAnalysis.clearCoverageAnalysis()" 
                        class="mt-2 px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600">
                    Limpiar análisis
                </button>
            </div>
        `;
    }

    // Mostrar instrucciones de ruta
    showRouteInstructions(route) {
        const infoDiv = document.getElementById('route-info') || this.createInfoDiv('route-info');
        
        let instructionsHtml = route.instructions.map(instruction => 
            `<li class="mb-1">${instruction}</li>`
        ).join('');

        infoDiv.innerHTML = `
            <div class="bg-white p-4 rounded-lg shadow-md">
                <h3 class="text-lg font-bold mb-2">Instrucciones de Ruta</h3>
                <p><strong>Distancia:</strong> ${(route.distance / 1000).toFixed(2)} km</p>
                <p><strong>Tiempo estimado:</strong> ${Math.round(route.duration)} minutos</p>
                <div class="mt-2">
                    <strong>Instrucciones:</strong>
                    <ol class="list-decimal list-inside mt-1">
                        ${instructionsHtml}
                    </ol>
                </div>
                <button onclick="spatialAnalysis.clearRoute()" 
                        class="mt-2 px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600">
                    Limpiar ruta
                </button>
            </div>
        `;
    }

    // Mostrar información del centro más cercano
    showNearestCenterInfo(center) {
        const infoDiv = document.getElementById('nearest-center-info') || this.createInfoDiv('nearest-center-info');
        
        infoDiv.innerHTML = `
            <div class="bg-white p-4 rounded-lg shadow-md">
                <h3 class="text-lg font-bold mb-2">Centro Médico Más Cercano</h3>
                <p><strong>Nombre:</strong> ${center.name}</p>
                <p><strong>Tipo:</strong> ${center.type}</p>
                <p><strong>Dirección:</strong> ${center.address}</p>
                <p><strong>Distancia:</strong> ${(center.distance / 1000).toFixed(2)} km</p>
            </div>
        `;
    }

    // Crear div de información
    createInfoDiv(id) {
        const div = document.createElement('div');
        div.id = id;
        div.className = 'fixed top-20 right-4 z-50 max-w-sm';
        document.body.appendChild(div);
        return div;
    }

    // Limpiar análisis de cobertura
    clearCoverageAnalysis() {
        if (this.coverageLayer) {
            this.map.removeLayer(this.coverageLayer);
            this.coverageLayer = null;
        }
        const infoDiv = document.getElementById('coverage-info');
        if (infoDiv) {
            infoDiv.remove();
        }
    }

    // Limpiar ruta
    clearRoute() {
        if (this.routeLayer) {
            this.map.removeLayer(this.routeLayer);
            this.routeLayer = null;
        }
        const infoDiv = document.getElementById('route-info');
        if (infoDiv) {
            infoDiv.remove();
        }
        const nearestInfoDiv = document.getElementById('nearest-center-info');
        if (nearestInfoDiv) {
            nearestInfoDiv.remove();
        }
    }

    // Limpiar áreas sin cobertura
    clearUncoveredAreas() {
        if (this.uncoveredAreasLayer) {
            this.map.removeLayer(this.uncoveredAreasLayer);
            this.uncoveredAreasLayer = null;
        }
    }

    // Limpiar todas las capas
    clearAll() {
        this.clearCoverageAnalysis();
        this.clearRoute();
        this.clearUncoveredAreas();
    }

    // Establecer ubicación del usuario
    setUserLocation(lat, lon) {
        this.userLocation = { lat: lat, lon: lon };
    }

    // Obtener ubicación del usuario
    getUserLocation() {
        return this.userLocation;
    }
}

// Función para inicializar el análisis espacial
function initSpatialAnalysis(map) {
    window.spatialAnalysis = new SpatialAnalysis(map);
    return window.spatialAnalysis;
}