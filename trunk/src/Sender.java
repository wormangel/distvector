import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

public class Sender implements Runnable {

	private Router router;

	/**
	 * Envia mensagens periodicas aos roteadores vizinhos. Utiliza outra thread.
	 * 
	 * @param router
	 *            Roteador respons�vel por enviar as mensagens.
	 */
	public Sender(Router router) {
		this.router = router;
	}

	public void run() {
		while (true) {
			// Envia nova mensagem a cada 300 milisegundos.
			try {
				sendVector();
				TimeUnit.MILLISECONDS.sleep(300);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}

		}
	}

	/**
	 * Envia o vetor para todos os n�s vizinhos
	 */
	public synchronized void sendVector() {
		for (Link link : router.getLinks()) {
			RouterConfiguration routerConfig = link.getRouterConnected();
			InetSocketAddress destIp = new InetSocketAddress(
					routerConfig.getAddress(), Integer.parseInt(routerConfig
							.getPort()));
			byte[] buffer = router.buildMessageToSend(
					link.getRouterConnected().getId()).getBytes();
			DatagramPacket sendDataPacket = null;

			try {
				sendDataPacket = new DatagramPacket(buffer, buffer.length,
						destIp);
				router.getSocket().send(sendDataPacket);
			} catch (Exception e) {
				router.setLinkDown(link);
			}
		}
	}

}
