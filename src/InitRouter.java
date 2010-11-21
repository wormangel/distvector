import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Inicializa o roteador (power on)
 * 
 * @author Lucas Medeiros
 * @author Otacilio Lacerda
 * @author Pedro Yossis
 * 
 */
public class InitRouter {

	static final String ROUTERS_FILE = "roteador.config";
	static final String LINKS_FILE = "enlaces.config";

	static final HashMap<String, LogLevel> logs = new HashMap<String, LogLevel>() {
		private static final long serialVersionUID = 1L;
		{
			put("uo", LogLevel.UPDATE_ONLY);
			put("fr", LogLevel.FULL_RECIEVE);
			put("fs", LogLevel.FULL_SEND);
			put("lf", LogLevel.LOG_FULL);
			put("rt", LogLevel.ROUTER_TABLE);
		}
	};

	static final int ID = 0;
	static final int PORT = 1;
	static final int ADDRESS = 2;
	static final int ROUTER1 = 0;
	static final int ROUTER2 = 1;
	static final int COST = 2;

	/**
	 * Pega o argumento de linha de comando e adapta a logica do roteador.
	 * 
	 * @param argsValue
	 *            Argumento passado pela linha de comando.
	 * @return Nivel de log representado pelo Enum LogLevel
	 */
	private static LogLevel getLogLevel(String argsValue) {
		if (argsValue.equals("uo"))
			return LogLevel.UPDATE_ONLY;
		if (argsValue.equals("fr"))
			return LogLevel.FULL_RECIEVE;
		if (argsValue.equals("fs"))
			return LogLevel.FULL_SEND;
		if (argsValue.equals("lf"))
			return LogLevel.LOG_FULL;
		return LogLevel.ROUTER_TABLE;
	}

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
		Integer routerID;
		long timeout; // Tempo para identificar que um enlace caiu
		long sendTime; // Tempo para enviar vetores (periódico)
		LogLevel logLevel;

		try {
			Options opt = new Options();
			opt.addOption(new Option("h", "help", false, "Help"));
			opt.addOption(new Option("i", "id", true, "Router id. (REQUIRED)"));
			opt.addOption(new Option("t", "timeout", true,
					"Timeout to detect link down (in milliseconds). Default = 1000ms"));
			opt.addOption(new Option("s", "sendtime", true,
					"Time to sent new vector (periodic time). Default = 300ms"));

			String logMessage = "Choose router log level. Possible values:"
					+ "\n(uo) Update only: logs only when the vector changes."
					+ "\n(fr) Full recieve: logs everything that gets."
					+ "\n(fs) Full send: logs everything that sent."
					+ "\n(lf) Log full: logs everything."
					+ "\n(rt) Router table: logs only router table."
					+ "\nDefault = uo (Update only.)";
			opt.addOption(new Option("l", "loglevel", true, logMessage));

			BasicParser parser = new BasicParser();
			CommandLine cl = parser.parse(opt, args);

			if (cl.hasOption('h')) {
				HelpFormatter f = new HelpFormatter();
				f.printHelp("java -jar router.jar", opt, true);
				System.exit(1);
			}
			if (cl.getOptionValue("id") == null)
				throw new ParseException("Router id can't be null");
			routerID = Integer.parseInt(cl.getOptionValue("id"));
			timeout = Integer.parseInt(cl.getOptionValue("timeout", "5000"));
			sendTime = Integer.parseInt(cl.getOptionValue("sendtime", "1000"));

			logLevel = getLogLevel(cl.getOptionValue("loglevel", "uo"));

			//
			// Lê as configurações
			//
			HashMap<Integer, RouterConfiguration> routersConfig = readRoutersFile(routerID);
			ArrayList<Link> links = readLinksFile(routerID, routersConfig);

			System.out.print("[" + new Timestamp(new Date().getTime())
					+ "] Starting router '" + routerID + "'.  ");
			try {
				new Router(routersConfig.get(routerID), links, sendTime,
						timeout, logLevel);
			} catch (Exception e) {
				System.out.println("Incorrect router ID.");
			}
		} catch (ParseException e) {
			System.out
					.println("Invalid option(s)\nTry 'java -jar router.jar --help' for more information.");
			System.exit(1);
		}
	}

}
