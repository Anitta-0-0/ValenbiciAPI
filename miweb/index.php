<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Disponibilidad de ValenBisi</title>
    <link rel="stylesheet" href="estilos.css">
</head>
<body>

<h1>Disponibilidad de ValenBisi</h1>

<div class="contenedor-boton">
    <a href="mapearbicis.php">
        <button class="btn-mapa">Ver mapa de estaciones</button>
    </a>
</div>

<?php

// ==========================
// 1. CONEXIÓN A AWS
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
    // 2. BORRAR DATOS ANTIGUOS
    // ==========================
    $pdo->query("TRUNCATE TABLE estaciones");

    // ==========================
    // 3. API VALENBISI
    // ==========================
    $baseUrl = "https://geoportal.valencia.es/server/rest/services/OPENDATA/Trafico/MapServer/228/query?where=1=1&outFields=*&returnGeometry=true&outSR=4326&f=json";

    $response = file_get_contents($baseUrl);
    $data = json_decode($response, true);

    // ==========================
    // 4. PREPARAR INSERT
    // ==========================
    $stmt = $pdo->prepare("
        INSERT INTO estaciones 
        (numero, direccion, bicis_disponibles, bornes_libres, latitud, longitud)
        VALUES (?, ?, ?, ?, ?, ?)
    ");

    // ==========================
    // 5. RECORRER ESTACIONES
    // ==========================
    foreach ($data["features"] as $station) {

        $numero = $station['attributes']['number'];
        $direccion = $station['attributes']['address'];
        $bicis = $station['attributes']['available'];
        $bornes = $station['attributes']['free'];

        $x = $station['geometry']['x'];
        $y = $station['geometry']['y'];

        // Si ya vienen en WGS84 no convertimos raro
        $lat = $y;
        $lon = $x;

        $stmt->execute([
            $numero,
            $direccion,
            $bicis,
            $bornes,
            $lat,
            $lon
        ]);
    }

    echo "<p>Datos guardados en AWS correctamente 🚀</p>";

} catch (PDOException $e) {
    echo "Error de conexión: " . $e->getMessage();
}

?>

</body>
</html>