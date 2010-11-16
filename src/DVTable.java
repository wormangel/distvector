import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
	 * Atualiza o vetor distância atual apos o recebimento do vetor distância do
	 * vizinho
	 * 
	 * @param dVetor
	 *            Vetor do vizinho
	 */
	public void updateNewVectorRecieved(DistanceVector dVetor) {
		Integer vectorID = dVetor.getId();

		// Descobre novos roteadores
		routerDiscovery(dVetor);

		// Se não encontrar o ID do router no mapa de vetores então
		// adiciona como novo e marca o enlace com UP
		if (!vectorsRecieved.containsKey(vectorID)) {
			System.out.println("[" + new Timestamp(new Date().getTime())
					+ "] Link " + selfDV.getId() + "-" + vectorID + " up! ");
			vectorsRecieved.put(vectorID, dVetor);
			updateWhenCostChange(vectorID, false);
			router.getLink(dVetor.getId()).setRecovery(false);
			return;
		}

		// Apenas para indicar se o link se reconectou.
		// Condição: Indicador de recuperação de queda && id já esta na tabela.
		Link link = router.getLink(dVetor.getId());
		if (link.getRecovery() && vectorsRecieved.containsKey(vectorID)) {
			link.setRecovery(false);
			System.out.println("Link " + selfDV.getId() + "-" + vectorID
					+ " up! ");
			vectorsRecieved.put(vectorID, dVetor);
			updateWhenCostChange(vectorID, false);
			return;
		}

		// Se o vetor recebido foi modificado, atualiza a tabela de vetores.
		// if (!vectorsRecieved.get(vectorID).equals(dVetor)) {
		if (!compareVectorsAreEquals(vectorsRecieved.get(vectorID), dVetor)) {
			System.out.print("[" + new Timestamp(new Date().getTime())
					+ "] Recieved vector '" + vectorID + "'. ");
			vectorsRecieved.put(vectorID, dVetor);
			updateWhenCostChange(vectorID, false);
			return;
		}
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
					selfDV.putDistance(key, UNREACHABLE);
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
	 * Atualiza o valor do vetor distância atual quando o custo ate algum
	 * caminho muda.
	 * 
	 * @param id
	 *            Id do vetor distância
	 * @param isDown
	 *            Indica se a atualização acontece a partir da queda do link
	 */
	private void updateWhenCostChange(int id, boolean isDown) {
		// Pega vetor recebido do vizinho
		DistanceVector vectorBeforeChange = selfDV.clone();
		DistanceVector vDist = vectorsRecieved.get(id);
		Integer linkCost = router.getLink(id).getCost();

		// Esse parâmetro e o if estão bem seboso mesmo, mas depois de 14h
		// programando não estou com coragem de pensar mais em uma solução
		// melhor. Quem sabe um dia eu arrumo isso. xD
		if (isDown) {
			selfDV.putDistance(id, UNREACHABLE);
		}

		for (Integer key : selfDV.getDistances().keySet()) {
			Integer cost = selfDV.getDistances().get(key);
			if (cost > linkCost + vDist.getDistances().get(key)) {
				selfDV.putDistance(key, linkCost
						+ vDist.getDistances().get(key));

				// Caso ultrapasse o diâmetro da rede, avisa e para
				if (linkCost + vDist.getDistances().get(key) >= UNREACHABLE) {
					System.out.println("["
							+ new Timestamp(new Date().getTime())
							+ "] LOOP DETECTED: Router down.");
					System.exit(1);
				}
			}
		}
		if (!compareVectorsAreEquals(vectorBeforeChange, selfDV)) {
			System.out.println("Cost changed to: " + selfDV.toString());
		} else {
			System.out.println("No change.");
		}
	}

	/**
	 * Atualiza a tabela caso algum enalace caia. No caso ele marca o roteador
	 * vizinho com UNREACHABLE e atualiza o vetor distância do roteador a partir
	 * dos outros vetores já armazenados.
	 * 
	 * @param id
	 *            Id do roteador na outra ponta do enlace.
	 */
	public void updateWhenLinkDown(int id) {
		System.out.print("[" + new Timestamp(new Date().getTime())
				+ "] Link down: " + selfDV.getId() + "-" + id + ". ");

		for (Integer key : vectorsRecieved.keySet()) {
			if (key != selfDV.getId()) {
				updateWhenCostChange(id, true);
			}
		}
	}
}
