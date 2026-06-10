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

<h1>Mapa de Bicicletas en Valencia</h1>

<a href="index.php" class="boton-volver">← Volver al Listado</a>

<div id="map"></div>

<script>
    var map = L.map('map').setView([39.47, -0.37], 13);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap'
    }).addTo(map);

    function getColor(bicis) {
        if (bicis === 0) return 'red';
        if (bicis < 10) return '#3498db';
        return '#2ecc71';
    }
</script>

<?php

// ==========================
// 1. CONEXIÓN AWS
// ==========================
$host = 'databasedmp.c1xutfuzej6d.us-east-1.rds.amazonaws.com';
$db   = 'valenbiciJDG';
$user = 'admin';
$pass = '123456789';
$charset = 'utf8mb4';

$dsn = "mysql:host=$host;dbname=$db;charset=$charset";

try {
    $pdo = new PDO($dsn, $user, $pass);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    // ==========================
    // 2. LEER ESTACIONES
    // ==========================
    $stmt = $pdo->query("SELECT * FROM estaciones");
    $stations = $stmt->fetchAll(PDO::FETCH_ASSOC);

    // ==========================
    // 3. GENERAR MARCADORES
    // ==========================
    foreach ($stations as $s) {

        echo "
        <script>
            L.circleMarker([" . $s['latitud'] . ", " . $s['longitud'] . "], {
                color: getColor(" . $s['bicis_disponibles'] . "),
                fillColor: getColor(" . $s['bicis_disponibles'] . "),
                radius: 7,
                fillOpacity: 0.7
            })
            .addTo(map)
            .bindPopup(`
                <strong>" . addslashes($s['direccion']) . "</strong><br>
                <b>Bicis:</b> " . $s['bicis_disponibles'] . "<br>
                <b>Bornes libres:</b> " . $s['bornes_libres'] . "
            `);
        </script>
        ";
    }

} catch (PDOException $e) {
    echo "Error: " . $e->getMessage();
}

?>

</body>
</html>