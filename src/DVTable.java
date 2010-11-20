import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Tabela que armazena o vetor distância atual e os vetores distância dos
 * roteadores vizinhos
 * 
 * @author Lucas Medeiros
 * @author Otacilio Lacerda
 * @author Pedro Yossis
 * 
 */
public class DVTable {

	static final Integer UNREACHABLE = 9999; // Diâmetro da rede.
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
		System.out.println("Init distance vector: " + selfDV.toString());
	}

	/**
	 * Atualiza o valor do vetor distância atual quando o custo ate algum
	 * caminho muda.
	 * 
	 * @param dv
	 *            Id do vetor distância
	 * @param isDown
	 *            Indica se a atualização acontece a partir da queda do link
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
				// Verifica se o custo pelo vizinho é menor
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
	private void calculateDistances(String log) {
		if (log != null) System.out.print("[" + new Timestamp(new Date().getTime()) + "] " + log	+ " ");
		DistanceVector vectorBeforeChange = selfDV.clone();

		calculateDistances();

		if (!compareVectorsAreEquals(vectorBeforeChange, selfDV)) {
			System.out.println("Cost changed to: " + selfDV.toString());
			// Notifica todos os roteadores vizinhos;
			router.sendMessage();
		} else {
			// System.out.println("No change.");
		}
	}

	/**
	 * Compara se dois vetores distância são iguais. Esse método auxiliar ajuda
	 * para identificar (atualizações) mudanças no vetor distância recebido do
	 * vizinho.
	 * 
	 * @param atual
	 *            Vetor distância atual.
	 * @param novo
	 *            Novo vetor distância recebido
	 * @return True se forem iguais, False se tiverem qualquer atualização
	 *         (diferença).
	 */
	private boolean compareVectorsAreEquals(DistanceVector atual,
			DistanceVector novo) {
		if (atual.getId() == novo.getId()) {
			HashMap<Integer, Integer> vAtual = atual.getDistances();
			HashMap<Integer, Integer> vNovo = novo.getDistances();

			// compara nas duas direções pois um vetor pode ter mais chaves do
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
	 * Recupera vetor distância do roteador
	 * 
	 * @return Vetor Distância
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
		calculateDistances(log);
	}

	/**
	 * Troca informações sobre novos roteadores com o vetor distância do vetor
	 * recebido e atualiza todos os vetores-distância da tabela de vetores.
	 * 
	 * @param dVector
	 *            Vetor distância recebido
	 */
	private void routerDiscovery(DistanceVector dVector) {
		// Se o vetor recebido tiver algum router novo, desconhecido
		// por este, no seu vetor distância, então ele adiciona essa chave
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
	 * Reinicia o próprio vetor para recalcular os custos
	 */
	private void resetSelfDV() {
		for (Integer key : selfDV.getDistances().keySet()) {
			selfDV.putDistance(key, UNREACHABLE);
		}
		selfDV.putDistance(selfDV.getId(), 0);
	}

	/**
	 * Atualiza o vetor distância atual apos o recebimento do vetor distância do
	 * vizinho
	 * 
	 * @param dVetor
	 *            Vetor do vizinho
	 */
	public void vectorRecievedUpdate(DistanceVector dVetor) {
		Integer vectorID = dVetor.getId();

		// Descobre novos roteadores
		routerDiscovery(dVetor);

		// Se não encontrar o ID do router no mapa de vetores então
		// adiciona como novo e marca o enlace com UP
		if (!vectorsRecieved.containsKey(vectorID)) {
			String log = "Link " + selfDV.getId() + "-" + vectorID + " up!";
			vectorsRecieved.put(vectorID, dVetor);
			calculateDistances(log);
			router.getLink(dVetor.getId()).setRecovery(false);
			return;
		}

		// Apenas para indicar se o link se reconectou.
		// Condição: Indicador de recuperação de queda && id já esta na tabela.
		Link link = router.getLink(dVetor.getId());
		if (link.getRecovery() && vectorsRecieved.containsKey(vectorID)) {
			link.setRecovery(false);
			String log = "Link " + selfDV.getId() + "-" + vectorID + " re-up!";
			vectorsRecieved.put(vectorID, dVetor);
			calculateDistances(log);
			return;
		}

//		 String log = "[" + new Timestamp(new Date().getTime())
//		 + "] Recieved vector: " + dVetor.toString() + " by router "
//		 + vectorID;
		vectorsRecieved.put(vectorID, dVetor);
		calculateDistances(null);
	}

}
