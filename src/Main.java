import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.json.JSONObject;

public class Main {
	
	public static void main (String [] args) 
	{
		
		List<Producto> l1, l2;
		l1 = Arrays.asList(new Producto("Zapatillas", 100, 1000, 15000), new Producto("Remeras", 50, 500, 7000), new Producto("Pantalones", 25, 750, 18000), new Producto("Cinturones", 10, 500, 5000));
		l2 = Arrays.asList(new Producto("Zapatillas", 100, 1000, 15000), new Producto("Remeras", 50, 500, 7000), new Producto("Pantalones", 25, 750, 18000), new Producto("Cinturones", 10, 500, 5000), new Producto("Camisas", 100, 10000, 15000), new Producto("Blusas", 5, 500, 7000), new Producto("Cardigan", 2, 750, 18000), new Producto("Chaqueta", 10, 500, 5000));

		Programa p1, p2;
		p1 = new Programa(new Ciudad("Madrid", "https://www.el-tiempo.net/api/json/v2/provincias/28", l1));
		p2 = new Programa(new Ciudad("Barcelona", "https://www.el-tiempo.net/api/json/v2/provincias/08", l2));
		
		JSONObject j1, j2;
		try {
			j1 = p1.leer(p1.getLink());
			List<String> l = p1.procesarInformacion(j1);
			l.add(p1.calcularGanancias());
			p1.escribirArchivo(l);
			for (String string : l) {
				System.out.println(string);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println();
		
		try {
			j2 = p2.leer(p2.getLink());
			List<String> l = p2.procesarInformacion(j2);
			l.add(p2.calcularGanancias());
			p2.escribirArchivo(l);
			for (String string : l) {
				System.out.println(string);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class Producto
{
	String nombre;
	int cantidad;
	int costo;
	int ganancia;
	
	public Producto(String nombre, int cantidad, int costo, int ganancia) {
		this.nombre = nombre;
		this.cantidad = cantidad;
		this.costo = costo;
		this.ganancia = ganancia;
	}
	
}

class Ciudad
{
	
	String link;
	String nombre;
	List<Producto> productos;
	
	
	public Ciudad(String nombre, String link, List<Producto> productos) {
		this.nombre = nombre;
		this.link = link;
		this.productos = productos;
	}
	
}

class Programa implements Api
{
	
	Ciudad ciudad;

	public Programa(Ciudad ciudad) {
		this.ciudad = ciudad;
	}
	
	public void escribirArchivo(List<String> l) throws IOException {
		String nombreArchivo = ciudad.nombre + LocalDate.now() + ".txt";
		Path file = Paths.get(nombreArchivo);
		Files.write(file, l, StandardCharsets.UTF_8);
	}

	public String calcularGanancias() {
		int n = 0;
		for (Producto producto : ciudad.productos) {
			n += producto.ganancia - producto.costo;
		}
		return "Ganancias esperadas: " + n + "€";
	}

	public List<String> procesarInformacion(JSONObject j) {
		List<String> lineas = new ArrayList<String>();
		lineas.add(ciudad.nombre);
		lineas.add("---------------------");
		int temperatura = 0;
		for (Object jsonObject2: j.getJSONArray("ciudades")) {
			if (((JSONObject) jsonObject2).getString("name").equals(ciudad.nombre)) {
				JSONObject jsonObject3 = (JSONObject) ((JSONObject) jsonObject2).get("temperatures");
				temperatura = jsonObject3.getInt("max");
				lineas.add("- Temperatura de hoy: " + temperatura);
			}
		}
		//Madrid
		if (temperatura > 20 && ciudad.nombre.equals("Madrid")) {
			lineas.add("Por condiciones climaticas no se ha podido trabajar el día dia de hoy.");
		} else if (ciudad.nombre.equals("Madrid")) {
			lineas.add("- Se trabaja en la fábrica.");
		}
		//Barcelona
		if (temperatura > 10 && ciudad.nombre.equals("Barcelona")) {
			lineas.add("- Se trabaja en la fábrica.");
		} else if (ciudad.nombre.equals("Barcelona")) {
			lineas.add("Por condiciones climaticas no se ha podido trabajar el día dia de hoy.");
		}
		//Tareas administrativas
		if (temperatura > 6 && ciudad.nombre.equals("Madrid")) {
			lineas.add("Se realizaron tareas administrativas.");
		}
		return lineas;
	}

	public String getLink() {
		return ciudad.link;
	}
	
	@Override
	public JSONObject leer(String link) throws MalformedURLException, IOException {
		URL url = new URL (link); //Declaro la URL
		HttpURLConnection conn = (HttpURLConnection) url.openConnection(); //Declaro la conexion
		conn.connect(); //Abro la conexion

		int tiempoRespuesta = conn.getResponseCode(); //Para manejo de 
													  //Situaciones
		
		if(tiempoRespuesta != 200)
		{
			throw new RuntimeException("HttpResponse" + tiempoRespuesta);
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			Scanner sc = new Scanner(url.openStream());
			while(sc.hasNext())
			{
				sb.append(sc.nextLine());
			}
			sc.close();
			return new JSONObject(sb.toString());
		}
	}
	
}

interface Api
{
	public JSONObject leer(String link) throws MalformedURLException, IOException;
}




