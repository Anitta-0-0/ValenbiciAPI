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
    // Solicitud de datos reales al Geoportal en formato JSON
    $baseUrl = "https://geoportal.valencia.es/server/rest/services/OPENDATA/Trafico/MapServer/228/query?where=1=1&outFields=*&returnGeometry=true&outSR=4326&f=json";

    // Función para convertir coordenadas de Valencia si hiciera falta
    function epsg25830Towgs84(float $easting, float $northing): array {
        $a = 6378137.0; $f = 1/298.257222101; $k0 = 0.9996; $zone = 30;
        $falseEasting = 500000.0; $falseNorthing = 0.0;
        $e = sqrt($f*(2-$f)); $e1sq = ($e*$e)/(1-$e*$e);
        $x = $easting - $falseEasting; $y = $northing - $falseNorthing;
        $m = $y / $k0;
        $mu = $m / ($a * (1 - pow($e, 2)/4 - 3*pow($e, 4)/64 - 5*pow($e, 6)/256));
        $e1 = (1 - sqrt(1 - $e*$e)) / (1 + sqrt(1 - $e*$e));
        $j1 = (3*$e1/2 - 27*pow($e1, 3)/32); $j2 = (21*pow($e1, 2)/16 - 55*pow($e1, 4)/32);
        $j3 = (151*pow($e1, 3)/96); $j4 = (1097*pow($e1, 4)/512);
        $fp = $mu + $j1*sin(2*$mu) + $j2*sin(4*$mu) + $j3*sin(6*$mu) + $j4*sin(8*$mu);
        $cosFp = cos($fp); $sinFp = sin($fp); $tanFp = tan($fp);
        $c1 = $e1sq * $cosFp * $cosFp; $t1 = $tanFp * $tanFp;
        $r1 = $a * (1 - $e*$e) / pow(1 - ($e*$e*$sinFp*$sinFp), 1.5);
        $n1 = $a / sqrt(1 - ($e*$e*$sinFp*$sinFp)); $d = $x / ($n1 * $k0);
        $latRad = $fp - ($n1 * $tanFp / $r1) * (pow($d, 2)/2 - (5 + 3*$t1 + 10*$c1 - 4*$c1*$c1 - 9*$e1sq)*pow($d, 4)/24 + (61 + 90*$t1 + 298*$c1 + 45*$t1*$t1 - 252*$e1sq)*pow($d, 6)/720);
        $lonOrigin = deg2rad(($zone - 1)*6 - 180 + 3);
        $lonRad = $lonOrigin + ($d - (1 + 2*$t1 + $c1)*pow($d, 3)/6 + (5 - 2*$c1 + 28*$t1 - 3*$c1*$c1 + 8*$e1sq + 24*$t1)*pow($d, 5)/120) / $cosFp;
        return ['latitude' => rad2deg($latRad), 'longitude' => rad2deg($lonRad)];
    }

    function normalizeValenbisiGeometry(array $geometry): array {
        $x = isset($geometry['x']) ? (float)$geometry['x'] : 0.0;
        $y = isset($geometry['y']) ? (float)$geometry['y'] : 0.0;
        if ($x >= -180 && $x <= 180 && $y >= -90 && $y <= 90) {
            return ['latitude' => $y, 'longitude' => $x, 'source_x' => $x, 'source_y' => $y];
        }
        $converted = epsg25830Towgs84($x, $y);
        return ['latitude' => $converted['latitude'], 'longitude' => $converted['longitude'], 'source_x' => $x, 'source_y' => $y];
    }

    $allStations = [];
    $errorOccurred = false;
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $baseUrl);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_HTTPHEADER, ["Accept: application/json"]);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    $response = curl_exec($ch);

    if ($response === false) {
        $errorOccurred = true;
    } else {
        $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
        if ($httpCode != 200) { $errorOccurred = true; }
    }
    curl_close($ch);

    if (!$errorOccurred) {
        $data = json_decode($response, true);
        if ($data !== null && isset($data["features"]) && is_array($data["features"])) {
            foreach ($data["features"] as $station) {
                $geometry = normalizeValenbisiGeometry($station['geometry']);
                $allStations[$station['attributes']['number']] = [
                    'address' => $station['attributes']['address'],
                    'open' => ($station['attributes']['open'] == "T"),
                    'available' => (int)$station['attributes']['available'],
                    'free' => (int)$station['attributes']['free'],
                    'total' => (int)$station['attributes']['total'],
                    'updated_at' => $station['attributes']['updated_at'],
                    'latitude' => round($geometry['latitude'], 7),
                    'longitude' => round($geometry['longitude'], 7)
                ];
            }
        }
    }

    if (!empty($allStations)) {
        // IMPORTANTE: Esto guarda los datos dinámicos que leerá el mapa de la Tarea 7
        $filePath = getcwd() . '/data.json';
        file_put_contents($filePath, json_encode($allStations));

        // Pintamos la tabla HTML idéntica a tu segunda captura
        echo "<table>";
        echo "<tr><th>Dirección</th><th>Número</th><th>Abierto</th><th>Disponibles</th><th>Libres</th><th>Total</th><th>Actualizado</th><th>Latitud</th><th>Longitud</th></tr>";
        foreach ($allStations as $number => $station) {
            echo "<tr>";
            echo "<td>" . htmlspecialchars($station['address']) . "</td>";
            echo "<td>" . $number . "</td>";
            echo "<td>" . ($station['open'] ? "<span class='estado-si'>Sí</span>" : "<span class='estado-no'>No</span>") . "</td>";
            echo "<td><strong>" . $station['available'] . "</strong></td>";
            echo "<td>" . $station['free'] . "</td>";
            echo "<td>" . $station['total'] . "</td>";
            echo "<td>" . $station['updated_at'] . "</td>";
            echo "<td>" . $station['latitude'] . "</td>";
            echo "<td>" . $station['longitude'] . "</td>";
            echo "</tr>";
        }
        echo "</table>";
    } else {
        echo "<p class='warn-msg'>No se encontraron datos.</p>";
    }
    ?>
</body>
</html>