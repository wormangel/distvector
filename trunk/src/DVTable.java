import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Tabela que armazena o vetor dist�ncia atual e os vetores dist�ncia dos
 * roteadores vizinhos
 * 
 * @author Lucas Medeiros
 * @author Otacilio Lacerda
 * @author Pedro Yossis
 * 
 */
public class DVTable {

	static final Integer UNREACHABLE = 9999; // Di�metro da rede.
	private DistanceVector selfDV;
	private HashMap<Integer, DistanceVector> vectorsRecieved;
	private Router router;

	/**
	 * Inicializa a tabela que guarda todos os vetores-distancia, ou seja cria
	 * seu proprio vetor e propaga
	 * 
	 * @param router
	 *            Roteador a qual a tabela pertence
	 */
	public DVTable(Router router) {
		this.router = router;
		this.selfDV = new DistanceVector(router.getRouterConfiguration()
				.getId());
		this.vectorsRecieved = new HashMap<Integer, DistanceVector>();

		ArrayList<Link> links = router.getLinks();

		// Coloca a distancia 0 para si proprio dentro do vetor
		selfDV.putDistance(router.getRouterConfiguration().getId(), 0);

		for (Link link : links) {
			if (link.isLinkUp()) {
				selfDV.putDistance(link.getRouterConnected().getId(),
						link.getCost());
			} else {
				selfDV.putDistance(link.getRouterConnected().getId(),
						UNREACHABLE);
			}
		}
		// TODO Log
		if (router.getLogLevel() != LogLevel.ROUTER_TABLE) {
			System.out.println("Init distance vector: " + selfDV.toString());
		}
	}

	/**
	 * Atualiza o valor do vetor dist�ncia atual quando o custo ate algum
	 * caminho muda.
	 * 
	 * @param dv
	 *            Id do vetor dist�ncia
	 * @param isDown
	 *            Indica se a atualiza��o acontece a partir da queda do link
	 */
	private void calculateDistances() {
		resetSelfDV();

		Integer linkCost;
		Integer currentCost;
		Integer neighborCost;

		for (Integer vectorID : vectorsRecieved.keySet()) {
			linkCost = router.getLink(vectorID).getCost();
			for (Integer distanceTo : selfDV.getDistances().keySet()) {
				currentCost = selfDV.getDistances().get(distanceTo);
				neighborCost = linkCost
						+ vectorsRecieved.get(vectorID).getDistances()
								.get(distanceTo);
				// Verifica se o custo pelo vizinho � menor
				if (currentCost > neighborCost) {
					selfDV.putDistance(distanceTo, neighborCost);
				}
			}
		}
	}

	/**
	 * Metodo criado para tratar as mensagens de log.
	 * 
	 * @param log
	 *            String de log
	 */
	private void calculateDistances(String log, DistanceVector dVector) {
		// TODO
		if (log != null)
			System.out.println("[" + new Timestamp(new Date().getTime()) + "] "
					+ log + " ");
		DistanceVector vectorBeforeChange = selfDV.clone();

		calculateDistances();

		String recieved = (dVector != null && (router.getLogLevel() == LogLevel.UPDATE_ONLY
				|| router.getLogLevel() == LogLevel.LOG_FULL || router
				.getLogLevel() == LogLevel.FULL_RECIEVE)) ? "["
				+ new Timestamp(new Date().getTime()) + "] Recieved vector: "
				+ dVector.toString() + " by router " + dVector.getId() + ". " : "";

		if (!compareVectorsAreEquals(vectorBeforeChange, selfDV)) {
			// TODO Log
			if (router.getLogLevel() == LogLevel.UPDATE_ONLY
					|| router.getLogLevel() == LogLevel.LOG_FULL
					|| router.getLogLevel() == LogLevel.FULL_RECIEVE) {
				System.out.println(recieved + "Cost changed to: "
						+ selfDV.toString());
			}
			// Notifica todos os roteadores vizinhos;
			router.sendMessage();
		} else {
			// TODO
			if (router.getLogLevel() == LogLevel.LOG_FULL || router.getLogLevel() == LogLevel.FULL_RECIEVE) {
				System.out.println(recieved + "No change.");
			}
		}
	}

	/**
	 * Compara se dois vetores dist�ncia s�o iguais. Esse m�todo auxiliar ajuda
	 * para identificar (atualiza��es) mudan�as no vetor dist�ncia recebido do
	 * vizinho.
	 * 
	 * @param atual
	 *            Vetor dist�ncia atual.
	 * @param novo
	 *            Novo vetor dist�ncia recebido
	 * @return True se forem iguais, False se tiverem qualquer atualiza��o
	 *         (diferen�a).
	 */
	private boolean compareVectorsAreEquals(DistanceVector atual,
			DistanceVector novo) {
		if (atual.getId() == novo.getId()) {
			HashMap<Integer, Integer> vAtual = atual.getDistances();
			HashMap<Integer, Integer> vNovo = novo.getDistances();

			// compara nas duas dire��es pois um vetor pode ter mais chaves do
			// que outro.
			for (Integer key : vAtual.keySet()) {
				if (!vAtual.get(key).equals(vNovo.get(key))) {
					return false;
				}
			}
			for (Integer key : vNovo.keySet()) {
				if (!vNovo.get(key).equals(vAtual.get(key))) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Recupera vetor dist�ncia do roteador
	 * 
	 * @return Vetor Dist�ncia
	 */
	public DistanceVector getDistanceVector() {
		return selfDV;
	}

	/**
	 * Atualiza a tabela caso algum enalace caia.
	 * 
	 * @param id
	 *            Id do roteador na outra ponta do enlace.
	 */
	public void linkDownUpdate(int id) {
		String log = "Link down: " + selfDV.getId() + "-" + id + ". ";
		calculateDistances(log, null);
	}

	/**
	 * Troca informa��es sobre novos roteadores com o vetor dist�ncia do vetor
	 * recebido e atualiza todos os vetores-dist�ncia da tabela de vetores.
	 * 
	 * @param dVector
	 *            Vetor dist�ncia recebido
	 */
	private void routerDiscovery(DistanceVector dVector) {
		// Se o vetor recebido tiver algum router novo, desconhecido
		// por este, no seu vetor dist�ncia, ent�o ele adiciona essa chave
		// no vetor deste.
		for (Integer key : dVector.getDistances().keySet()) {
			if (!selfDV.getDistances().containsKey(key)) {
				selfDV.putDistance(key, UNREACHABLE);
			}
			for (Integer k : vectorsRecieved.keySet()) {
				if (!vectorsRecieved.get(k).getDistances().containsKey(key)) {
					vectorsRecieved.get(k).putDistance(key, UNREACHABLE);
				}
			}
		}
		for (Integer key : selfDV.getDistances().keySet()) {
			if (!dVector.getDistances().containsKey(key)) {
				dVector.putDistance(key, UNREACHABLE);
			}
		}
	}

	/**
	 * Reinicia o pr�prio vetor para recalcular os custos
	 */
	private void resetSelfDV() {
		for (Integer key : selfDV.getDistances().keySet()) {
			selfDV.putDistance(key, UNREACHABLE);
		}
		selfDV.putDistance(selfDV.getId(), 0);
	}

	/**
	 * Atualiza o vetor dist�ncia atual apos o recebimento do vetor dist�ncia do
	 * vizinho
	 * 
	 * @param dVector
	 *            Vetor do vizinho
	 */
	public void vectorRecievedUpdate(DistanceVector dVector) {
		Integer vectorID = dVector.getId();

		// Descobre novos roteadores
		routerDiscovery(dVector);

		// Se n�o encontrar o ID do router no mapa de vetores ent�o
		// adiciona como novo e marca o enlace com UP
		if (!vectorsRecieved.containsKey(vectorID)) {
			String log = (router.getLogLevel() != LogLevel.ROUTER_TABLE) ? "Link "
					+ selfDV.getId() + "-" + vectorID + " up!"
					: null;
			vectorsRecieved.put(vectorID, dVector);
			calculateDistances(log, dVector);
			router.getLink(dVector.getId()).setRecovery(false);
			return;
		}

		// Apenas para indicar se o link se reconectou.
		// Condi��o: Indicador de recupera��o de queda && id j� esta na tabela.
		Link link = router.getLink(dVector.getId());
		if (link.getRecovery() && vectorsRecieved.containsKey(vectorID)) {
			link.setRecovery(false);
			String log = (router.getLogLevel() != LogLevel.ROUTER_TABLE) ? "Link "
					+ selfDV.getId() + "-" + vectorID + " re-up!"
					: null;
			vectorsRecieved.put(vectorID, dVector);
			calculateDistances(log, dVector);
			return;
		}
		vectorsRecieved.put(vectorID, dVector);
		calculateDistances(null, dVector);
	}

}
