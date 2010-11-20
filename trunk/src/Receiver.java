import java.io.IOException;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * Responsável por ficar escutando os enlaces, receber as mensagens e repassar
 * para o roteador
 * 
 * @author Lucas Medeiros
 * @author Otacilio Lacerda
 * @author Pedro Yossis
 * 
 */
public class Receiver implements Runnable {

	private Router router;
	private HashMap<String, RouterConfiguration> configs;

	/**
	 * Recebe a mensagem, faz o parser e manda para o router
	 * 
	 * @param msg
	 *            Mensagem recebida
	 */
	private void digestMessage(String msg) {
		// Formato da mensagem:
		// ID ROUTER | ID - Custo | ID - Custo | ...
		// Ex:
		// "2|1,2|2,0|3,1"
		String[] listInfo = msg.split("\\|");

		int routerID = Integer.parseInt(listInfo[0]);
		DistanceVector vector = new DistanceVector(routerID);

		for (int i = 1; i < listInfo.length; i++) {
			String[] entry = listInfo[i].split("-");
			Integer id = Integer.parseInt(entry[0]);
			Integer cost = Integer.parseInt(entry[1]);
			vector.putDistance(id, cost);
		}

		router.setLinkUp(routerID);
		router.updateDVTable(vector);

	}

	/**
	 * Fica escutando mensagens de outros roteadores. Utiliza outra thread.
	 * 
	 * @param router
	 *            Roteador ao qual são direcionadas as mensagens recebidas.
	 */
	public Receiver(Router router) {
		this.router = router;
		this.configs = new HashMap<String, RouterConfiguration>();
		for (Link link : this.router.getLinks()) {
			String address = link.getRouterConnected().getAddress();
			String port = link.getRouterConnected().getPort();
			configs.put(address + port, link.getRouterConnected());
		}
	}

	@Override
	public void run() {
		while (true) {
			// Creio que 1024 seja um tamanho grande o suficiente para qualquer
			// teste. Com muitos roteadores conectados.
			byte[] receiveData = new byte[1024];

			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);

			try {
				router.getSocket().receive(receivePacket);
				String msg = new String(receivePacket.getData());
				digestMessage(msg.trim());
			} catch (IOException e) {
			}

			// Verifica quais enlaces atingiram o timeout e os desativa.
			router.checkActiveLinks();

			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}
