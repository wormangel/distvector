import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Representa o roteador, cada processo levantado eh considerado um roteador
 * independente
 * 
 * @author otacilio
 * 
 */
public class Router {

	private final long TIMEOUT = 1000; // Um segundo
	private int id;
	private String port;
	private String address;
	private DatagramSocket communicationSocket;
	private ArrayList<Link> links;
	private DVTable dvTable;
	private Receiver receiver;
	private Sender sender;

	/**
	 * Constroi o Roteador
	 * 
	 * @param routerConfiguration
	 *            Configuracao do roteador
	 * @param links
	 *            Lista com todos os enlaces do roteador
	 */
	public Router(RouterConfiguration routerConfiguration, ArrayList<Link> links) {
		this.id = routerConfiguration.getId();
		this.port = routerConfiguration.getPort();
		this.address = routerConfiguration.getAddress();
		this.links = links;

		dvTable = new DVTable(this);
		createSocket();

		// Resposaveis por enviar e receber as mensagens
		receiver = new Receiver(this);
		sender = new Sender(this);

		new Thread(sender).start();
		new Thread(receiver).start();
	}

	/**
	 * Formata a mensagem de acordo com o protocolo estabelecido para enviar seu
	 * vetor dist�ncia para os roteadores vizinhos.
	 * 
	 * Formato da mensagem: ID ROUTER | ID , Custo | ID , Custo | ... Ex:
	 * "2|1,2|2,0|3,1" Considerando que o roteador tem o ID 2 e que tem tres
	 * vizinho ativos (1, 2 e 3), tendo a comunica��o com cada um custo
	 * respectivo 2, 0 e 1
	 * 
	 * @param dest
	 *            Router para o qual a mensagem ser� enviada
	 * @return String com mensagem formatada de acordo com o protocolo
	 *         estabelecido
	 */
	public String buildMessageToSend(Integer dest) {
		StringBuilder messageBuilder = new StringBuilder();
		DistanceVector distanceVector = dvTable.getDistanceVector();

		messageBuilder.append(this.id);

		for (Integer router : distanceVector.getDistances().keySet()) {
			// "|ID,Custo"
			messageBuilder.append("|" + router + "-"
					+ distanceVector.getDistances().get(router));
		}
		String menssage = messageBuilder.toString().trim();
		// System.out.println("[" + new Timestamp(new Date().getTime())
		// + "] Send: " + menssage + " to router " + dest);
		return menssage;
	}

	/**
	 * Verifica quais enlaces superaram o timeout de recebimeto e os marca como
	 * down. Ou seja, qualquer caminho por este enlace tem custo infinito.
	 */
	public void checkActiveLinks() {
		for (Link link : links) {
			if (System.currentTimeMillis() - link.getLastActivity() > TIMEOUT) {
				if (link.isLinkUp()) {
					setLinkDown(link);
					link.setLastActivity(System.currentTimeMillis());
				}
			}
		}
	}

	/**
	 * Socket usado para comunica��o com os vizinhos
	 */
	private void createSocket() {
		try {
			this.communicationSocket = new DatagramSocket(
					Integer.parseInt(port), InetAddress.getByName(address));
			communicationSocket.setSoTimeout(300);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Verifica se o enlace ate o roteador vizinho esta up
	 * 
	 * @param id
	 *            Id do roteador vizinho
	 * @return True or False
	 */
	public boolean isLinkUp(Integer id) {
		for (Link link : links) {
			if (link.getRouterConnected().getId() == id) {
				return link.isLinkUp();
			}
		}
		return false;
	}

	/**
	 * Pega o custo para um determinado roteador.
	 * 
	 * @param id
	 *            Id do roteador
	 * @return Custo do enlace ate o roteador passado.
	 */
	public Integer getCostToRouter(Integer id) {
		for (Link link : getLinks()) {
			if (link.getRouterConnected().getId() == id)
				return link.getCost();
		}
		return null;
	}

	/**
	 * Pega o enlace pelo valor do ID do roteador da otura ponta.
	 * 
	 * @param routerID
	 *            ID do roteador da outra ponta
	 * @return Enlace
	 */
	public Link getLink(Integer routerID) {
		for (Link link : links) {
			if (link.getRouterConnected().getId() == routerID) {
				return link;
			}
		}
		return null;
	}

	/**
	 * Lista de enlaces do roteador
	 * 
	 * @return the links
	 */
	public ArrayList<Link> getLinks() {
		return links;
	}

	/**
	 * Configura��o do Roteador
	 * 
	 * @return Configuracao do roteador
	 */
	public RouterConfiguration getRouterConfiguration() {
		return new RouterConfiguration(id, port, address);
	}

	/**
	 * Retorna Socket de comunica��o
	 * 
	 * @return Socket
	 */
	public DatagramSocket getSocket() {
		return this.communicationSocket;
	}

	/**
	 * Envia o vetor dist�ncia para todos os nos vizinhos
	 */
	public void sendMessage() {
		sender.sendVector();
	}

	/**
	 * Marca o enlace como desativado
	 * 
	 * @param link
	 *            Enlace que caiu.
	 */
	public void setLinkDown(Link link) {
		if (link.isLinkUp()) {
			link.setLinkUp(false);
			dvTable.linkDownUpdate(link.getRouterConnected().getId());
		}
	}

	/**
	 * Marca o enlace como ativo
	 * 
	 * @param id
	 *            Id do roteador
	 */
	public void setLinkUp(Integer id) {
		for (Link link : getLinks()) {
			if (link.getRouterConnected().getId() == id) {
				// A linha abaixo serve para identificar que o enlace
				// se recuperou de uma queda.
				if (!link.isLinkUp())
					link.setRecovery(true);
				link.setLinkUp(true);

			}
		}
	}

	/**
	 * Repassa para a tabela de vetores com o novo vetor recebido
	 * 
	 * @param dv
	 *            Vetor dist�ncia recebido
	 */
	public void updateDVTable(DistanceVector dv) {
		// Marca o enlace como ativo e zera o timeout pois recebeu novo vetor
		// dele
		Link link = getLink(dv.getId());
		if (link != null) {
			link.setLastActivity(System.currentTimeMillis());
		}
		// Repassa para a tabela de vetores ser atualizada
		dvTable.vectorRecievedUpdate(dv);

	}

}