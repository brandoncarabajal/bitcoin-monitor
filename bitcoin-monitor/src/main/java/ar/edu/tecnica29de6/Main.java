package ar.edu.tecnica29de6;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {

	public static void main(String[] args) {
		String url = "https://api.coindesk.com/v1/bpi/currentprice/BTC.json";
		double maxPrice = 0;
		double minPrice = Double.MAX_VALUE;

		// Cargar maxPrice y minPrice desde el archivo CSV
		try {
			double[] prices = loadMaxMinFromCSV("btc_prices.csv");
			maxPrice = prices[0];
			minPrice = prices[1];
		} catch (IOException e) {
			System.out.println("Error al cargar los precios máximos y mínimos: " + e.getMessage());
		}

		while (true) {
			try {
				// Realizar la solicitud HTTP
				CloseableHttpClient httpClient = HttpClients.createDefault();
				HttpGet request = new HttpGet(url);
				HttpResponse response = httpClient.execute(request);
				ObjectMapper mapper = new ObjectMapper();

				JsonNode rootNode = mapper.readTree(response.getEntity().getContent());
				double btcPrice = rootNode.path("bpi").path("USD").path("rate_float").asDouble();
				String timestamp = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
						.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

				// TODO Imprimir el precio del Bitcon
				System.out.println("el precio del bitcon es" + btcPrice);
				
				// Verificar si el precio es mayor al máximo o menor al mínimo
				if (btcPrice > maxPrice) {
					// TODO si el precio supera al maximo registrado
					// actualizar el maximo
					// e imprimir por pantalla una alerta
					maxPrice = btcPrice;
					System.out.println("el maximo esta actualizado");
				}

				if (btcPrice < minPrice) {
					// TODO si el precio es inferior al minimo registrado
					// actualizar el minimo
					// e imprimir por pantalla una alerta
					minPrice = btcPrice;
					System.out.println("el minimo esta actualizado");
				}

				// Actualizar el archivo CSV con el nuevo precio
				updateCSV(btcPrice, timestamp);

				// Esperar 1 minuto antes de la próxima solicitud
				Thread.sleep(60000);
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}

	// Cargar los valores máximo y mínimo desde el archivo CSV
	private static double[] loadMaxMinFromCSV(String fileName) throws IOException {
		File file = new File(fileName);
		if (!file.exists()) {
			return new double[] { 0, Double.MAX_VALUE }; // Si el archivo no existe, devolver valores por defecto
		}

		double maxPrice = 0;
		double minPrice = Double.MAX_VALUE;
		try (CSVReader reader = new CSVReader(new FileReader(file))) {
			List<String[]> records;
			try {
				records = reader.readAll();

				for (String[] record : records) {
					if (record.length < 2 || record[1].equals("price"))
						continue; // Omitir encabezados
					double price = Double.parseDouble(record[1]);
					if (price > maxPrice)
						maxPrice = price;
					if (price < minPrice)
						minPrice = price;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CsvException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return new double[] { maxPrice, minPrice };
		}
	}

	// Actualizar el archivo CSV con el nuevo precio
	private static void updateCSV(double price, String timestamp) throws IOException {
		String fileName = "btc_prices.csv";
		File file = new File(fileName);

		// Si el archivo no existe, crear y escribir el encabezado
		if (!file.exists()) {
			try (CSVWriter writer = new CSVWriter(new FileWriter(fileName))) {
				writer.writeNext(new String[] { "timestamp", "price" });
			}
		}

		// Escribir la nueva fila con el timestamp y el precio
		try (CSVWriter writer = new CSVWriter(new FileWriter(fileName, true))) {
			DecimalFormat df = new DecimalFormat("0.0000");
			writer.writeNext(new String[] { timestamp, df.format(price) });
		}
	}
}
