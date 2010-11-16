import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Inicializa o roteador (power on)
 * 
 * @author otacilio
 * 
 */
public class InitRouter {

	static final String ROUTERS_FILE = "roteador.config";
	static final String LINKS_FILE = "enlaces.config";

	static final int ID = 0;
	static final int PORT = 1;
	static final int ADDRESS = 2;
	static final int ROUTER1 = 0;
	static final int ROUTER2 = 1;
	static final int COST = 2;

	/**
	 * Faz a leitura do arquivo de configuracao dos enlaces
	 * 
	 * @param routerID
	 *            ID do roteador que vai verificar os enlaces
	 * @return Uma lista com todos os enlaces do roteador
	 */
	private static ArrayList<Link> readLinksFile(Integer routerID,
			HashMap<Integer, RouterConfiguration> routersConfig) {
		String input;
		String[] tokens;
		ArrayList<Link> links = new ArrayList<Link>();
		try {
			BufferedReader bf = new BufferedReader(new FileReader(new File(
					LINKS_FILE)));
			while ((input = bf.readLine()) != null) {
				tokens = input.split(" ");

				int idRouter1 = Integer.parseInt(tokens[ROUTER1]);
				int idRouter2 = Integer.parseInt(tokens[ROUTER2]);
				int cost = Integer.parseInt(tokens[COST]);

				// cria o novo link com o router do final do link
				// lembrar que quando criado o status eh down
				if (idRouter1 == routerID) {
					links.add(new Link(routersConfig.get(idRouter2), cost));
				} else if (idRouter2 == routerID) {
					links.add(new Link(routersConfig.get(idRouter1), cost));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return links;
	}

	/**
	 * Faz a leitura do arquivo de configuracao dos roteadores
	 * 
	 * @param routerID
	 *            ID do roteador
	 * @return
	 * @return Um mapa com o RouterConfiguration de todos os roteadores
	 *         mapeamento int -> RouterConfiguration
	 */
	private static HashMap<Integer, RouterConfiguration> readRoutersFile(
			Integer routerID) {
		String input;
		String[] tokens;
		HashMap<Integer, RouterConfiguration> routersConfig = new HashMap<Integer, RouterConfiguration>();
		try {
			BufferedReader bf = new BufferedReader(new FileReader(new File(
					ROUTERS_FILE)));
			while ((input = bf.readLine()) != null) {
				tokens = input.split(" ");

				int id = Integer.parseInt(tokens[ID]);
				String port = tokens[PORT];
				String address = tokens[ADDRESS];

				routersConfig.put(id,
						new RouterConfiguration(id, port, address));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return routersConfig;
	}

	/**
	 * Inicializa o roteador
	 * 
	 * @param args
	 *            Id do roteador
	 */
	public static void main(String[] args) {
		int routerID = Integer.parseInt(args[0]);

		HashMap<Integer, RouterConfiguration> routersConfig = readRoutersFile(routerID);
		ArrayList<Link> links = readLinksFile(routerID, routersConfig);

		new Router(routersConfig.get(routerID), links);
	}

}
