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

/**
 * @author Eva G.
 * Ajustado para procesar JSON y generar sentencias SQL.
 */
public class DatosJSon {
	
	private static String API_URL;
	private String datos = ""; // para mostrar en el jTextArea
	private String[] values;   //para añadir a la BDD
	private int numEst;

	public DatosJSon(int nE) {
		this.numEst = nE;
		this.datos = "";
		API_URL = "https://geoportal.valencia.es/server/rest/services/OPENDATA/Trafico/MapServer/228/query"
				+ "?where=1%3D1"
				+ "&outFields=*"
				+ "&returnGeometry=true"
				+ "&f=json";
		this.values = new String[numEst];
		for (int i = 0; i < numEst; i++) {
			values[i] = "";
		}
	}

	public void mostrarDatos(int nE) {
		this.numEst = nE;
		this.datos = "";
		
		this.values = new String[numEst];
		for (int i = 0; i < numEst; i++) {
			values[i] = "";
		}

		if (API_URL.isEmpty()) {
			setDatos("La URL de la API no está especificada.");
			return;
		}

		// Configuración SSL tolerante para evitar errores de certificado del Geoportal
		try {
			SSLContext sslContext = SSLContexts.custom()
					.loadTrustMaterial(null, (chain, authType) -> true)
					.build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

			try (CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build()) {
				HttpGet request = new HttpGet(API_URL);
				HttpResponse response = httpClient.execute(request);
				HttpEntity entity = response.getEntity();

				if (entity != null) {
					String result = EntityUtils.toString(entity);

					try {
						JSONObject jsonObject = new JSONObject(result);
						JSONArray features = jsonObject.getJSONArray("features");

						// Controlamos no pasarnos del tamaño del JSON ni del solicitado por el usuario
						int totalAProcesar = Math.min(features.length(), numEst);
						
						StringBuilder sbDatos = new StringBuilder();

						// CÓDIGO DEL BUCLE PARA VALORES Y TEXTAREA
						for (int i = 0; i < totalAProcesar; i++) {
							JSONObject feature = features.getJSONObject(i);
							JSONObject atributos = feature.getJSONObject("attributes");
							JSONObject geometry = feature.getJSONObject("geometry");

							// Extraemos los campos correspondientes a las variables del enunciado
							int number = atributos.optInt("number", 0);
							String nombre = atributos.optString("address", "Sin dirección").replace("'", "''"); // Evita fallos con comillas en SQL
							int bicis = atributos.optInt("available", 0);
							int anclajes = atributos.optInt("free", 0);
							double x = geometry.optDouble("x", 0.0);
							double y = geometry.optDouble("y", 0.0);
							
							// Codigo de conversión de cordenadas a GPS 
							String coords = (String) ConversionGeoLongLat.conversion(x, y);
						    String[] partes = coords.split(",");
						    String lat = partes[0].trim();
						    String lon = partes[1].trim();
						    
						    double latitud = Double.parseDouble(lat);
						    double longitud = Double.parseDouble(lon);

							// Acumulamos el texto amigable para el JTextArea de la interfaz
							sbDatos.append("Estación Nº: ").append(number).append("\n")
							       .append("Dirección: ").append(nombre).append("\n")
							       .append("Bicis Disponibles: ").append(bicis).append("\n")
							       .append("Anclajes Libres: ").append(anclajes).append("\n")
							       .append("Coordenadas: (").append(x).append(", ").append(y).append(")\n")
							       .append("-------------------------------------------\n");

							// Preparamos la sentencia SQL INSERT correspondiente para este registro
							// Asumiendo que tu tabla tiene columnas parecidas a: (numero, direccion, bicis_disponibles, anclajes_libres, x, y)
							values[i] = "INSERT INTO estaciones (numero, direccion, bicis_disponibles, anclajes_libres, coord_x, coord_y) "
									+ "VALUES (" + number + ", '" + nombre + "', " + bicis + ", " + anclajes + ", " + x + ", " + y + ");";
						}
						
						// Guardamos los datos formateados en la variable de clase
						this.datos = sbDatos.toString();

					} catch (org.json.JSONException e) {
						setDatos("Error al procesar los datos JSON: " + e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			setDatos("Error de conexión o SSL: " + e.getMessage());
			e.printStackTrace();
		}
	}

	// Getters y Setters (Se mantienen exactamente igual)
	public String getDatos() {
		return datos;
	}

	public void setDatos(String datos) {
		this.datos = datos;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public int getNumEst() {
		return numEst;
	}

	public void setNumEst(int numEst) {
		this.numEst = numEst;
	}
}