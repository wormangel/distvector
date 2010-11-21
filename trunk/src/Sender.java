import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Responsável por enviar mensagens para os roteadores vizinhos. Isso pode
 * ocorrer de dois modos, periodicamente por um tempo pre-definido ou quando
 * quando o roteador precisa informar imediatamente os roteadores vizinhos
 * devido a mudança no custo do vetor distância, nesse caso utiliza-se o método
 * sendVector()
 * 
 * @author Lucas Medeiros
 * @author Otacilio Lacerda
 * @author Pedro Yossis
 * 
 */
public class Sender implements Runnable {

	private Router router;
	private long sendTime; // tempo para reenviar o DV

	/**
	 * Envia mensagens periodicas aos roteadores vizinhos. Utiliza outra thread.
	 * 
	 * @param router
	 *            Roteador responsável por enviar as mensagens.
	 */
	public Sender(Router router, long sendDVTimeout) {
		this.router = router;
		this.sendTime = sendDVTimeout;
	}

	public void run() {
		while (true) {
			// Envia nova mensagem a cada sendDVTimeout milisegundos.
			try {
				sendVector();
				TimeUnit.MILLISECONDS.sleep(this.sendTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}

		}
	}

	/**
	 * Envia o vetor para todos os nós vizinhos
	 */
	public synchronized void sendVector() {
		String vector = router.buildMessageToSend();

		for (Link link : router.getLinks()) {
			RouterConfiguration routerConfig = link.getRouterConnected();
			InetSocketAddress destIp = new InetSocketAddress(
					routerConfig.getAddress(), Integer.parseInt(routerConfig
							.getPort()));

			byte[] buffer = vector.getBytes();
			DatagramPacket sendDataPacket = null;

			try {
				sendDataPacket = new DatagramPacket(buffer, buffer.length,
						destIp);
				router.getSocket().send(sendDataPacket);
			} catch (Exception e) {
				router.setLinkDown(link);
			}
		}
		// TODO Log
		if (router.getLogLevel() == LogLevel.FULL_SEND
				|| router.getLogLevel() == LogLevel.LOG_FULL) {
			System.out.println("[" + new Timestamp(new Date().getTime())
					+ "] Send: " + vector + " to all neighbors.");
		}
	}

}
