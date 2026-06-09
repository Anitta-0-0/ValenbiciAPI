package es.gva.edu.iesjuandegaray.bicis;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import javax.net.ssl.SSLContext;

public class ValenbisiAPI26T1v2_1 {

    private static final String API_URL =
            "https://geoportal.valencia.es/server/rest/services/OPENDATA/Trafico/MapServer/228/query"
            + "?where=1%3D1"
            + "&outFields=*"
            + "&returnGeometry=true"
            + "&f=json";

    public static void main(String[] args) {

        // Agrupamos todo dentro del bloque try-catch principal para gestionar las excepciones de SSL y de Red
        try {
            // Configuración del contexto SSL para ignorar certificados autofirmados
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, (chain, authType) -> true)
                    .build();

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslContext, NoopHostnameVerifier.INSTANCE);

            try (CloseableHttpClient httpClient = HttpClients.custom()
                    .setSSLSocketFactory(sslsf)
                    .build()) {

                HttpGet request = new HttpGet(API_URL);
                HttpResponse response = httpClient.execute(request);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    String result = EntityUtils.toString(entity);

                    // Convertimos a JSON
                    JSONObject jsonObject = new JSONObject(result);

                    // Obtenemos el array "features"
                    JSONArray features = jsonObject.getJSONArray("features");

                    System.out.println("Número de estaciones: " + features.length());
                    System.out.println();

                    // BUCLE QUE RECORRE LAS ESTACIONES
                    for (int i = 0; i < features.length(); i++) {
                        JSONObject feature = features.getJSONObject(i);
                        JSONObject atributos = feature.getJSONObject("attributes");
                        
                        String adreça = atributos.optString("address", "Dirección no disponible");
                        int disponibles = atributos.optInt("available", 0);
                        int libres = atributos.optInt("free", 0);
                        int totales = atributos.optInt("total", 0);

                        System.out.println("Estación: " + adreça);
                        System.out.println("Bicis disponibles: " + disponibles);
                        System.out.println("Espacios libres: " + libres);
                        System.out.println("Total plazas: " + totales);
                        System.out.println("--------------------------------------------------------------------------------------------------------");
                        System.out.println("--------------------------------------------------------------------------------------------------------");
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error en la petición HTTP / Conexión:");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error de configuración SSL o procesando el JSON:");
            e.printStackTrace();
        }
    }
}