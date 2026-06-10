<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mapa de Estaciones Valenbisi</title>
    
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>

    <style>
        body {
            margin: 0;
            font-family: Arial, sans-serif;
            text-align: center;
            background-color: #f9f9f9;
        }
        h1 {
            color: #2c3e50;
            font-size: 24px;
            margin-top: 20px;
        }
        .boton-volver {
            display: inline-block;
            background-color: #007bff;
            color: white;
            text-decoration: none;
            padding: 8px 15px;
            border-radius: 4px;
            font-weight: bold;
            margin-bottom: 15px;
        }
        #map {
            height: 600px; 
            width: 90%; 
            margin: 0 auto 30px auto;
            border: 2px solid #ddd;
            border-radius: 8px;
        }
    </style>
</head>
<body>

    <h1>Mapeo de Bicicletas en Valencia (Tarea 7)</h1>
    
    <a href="index.php" class="boton-volver">← Volver al Listado</a>
    
    <div id="map"></div>

    <script>
        // 1. Centrar el mapa en Valencia
        var map = L.map('map').setView([39.47, -0.37], 13);

        // 2. Cargar el fondo de calles público
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap'
        }).addTo(map);

        // 3. Lógica de colores DAW: azul si quedan pocas, verde si hay bastantes
        function getMarkerColor(available) {
            if (available === 0) {
                return 'red'; 
            } else if (available < 10) {
                return '#3498db'; // Azul claro moderno
            } else {
                return '#2ecc71'; // Verde claro moderno
            }
        }

        // 4. Carga dinámica de los datos almacenados
        fetch('data.json')
            .then(response => response.json())
            .then(data => {
                Object.values(data).forEach(station => {
                    const lat = station.latitude;  
                    const lon = station.longitude; 
                    const address = station.address;
                    const available = station.available;
                    const free = station.free;
                    const total = station.total;

                    if (lat && lon) {
                        L.circleMarker([lat, lon], {
                            color: getMarkerColor(available),
                            fillColor: getMarkerColor(available),
                            radius: 7,
                            fillOpacity: 0.7
                        })
                        .addTo(map)
                        .bindPopup(`
                            <strong>${address}</strong><br>
                            <b>Disponibles:</b> ${available}<br>
                            <b>Libres:</b> ${free}<br>
                            <b>Total:</b> ${total}
                        `);
                    }
                });
            })
            .catch(error => console.error('Error:', error));
    </script>
</body>
</html>