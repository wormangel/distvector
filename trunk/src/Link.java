/**
 * Representa o enlace
 * 
 * @author otacilio
 * 
 */
public class Link {

	// True ou false para representar se o enlace esta funcionando ou nao
	private boolean linkUp;
	private RouterConfiguration routerConnected;
	private Integer linkCost;

	// Essa vari�vel serve como timeout, caso a contagem fique maior que o
	// n�mero de roteadores presentes, ent�o ele considera que o enlace
	// caiu. A cada timeout do socket ela � acrescentada a todos os
	// enlaces vivos.
	private Integer timeout;

	// Essa vari�vel serve para indicar se o enalce se recupera de uma falha ou
	// n�o. Ex: Se o enlace cai e depois volta essa vari�vel � utilizada para
	// indicar isso. Serve apenas para facilitar o debug, indicando na saida
	// se ele foi religado ou n�o.
	private boolean recovery;

	/**
	 * Cria um novo enlace
	 * 
	 * @param routerConnected
	 *            Configuracao do router na outra ponta do enlace
	 * @param cost
	 *            Custo do enlace
	 */
	public Link(RouterConfiguration routerConnected, int cost) {
		this.routerConnected = routerConnected;
		this.linkCost = cost;
		this.linkUp = false;
		this.timeout = 0;
		this.recovery = false;
	}

	/**
	 * Incrementa a contagem de espera do enlace
	 */
	public void addTimeout() {
		timeout++;
	}

	/**
	 * Zera o timeout
	 */
	public void clearTimeout() {
		timeout = 0;
	}

	/**
	 * @return Verifica se o enlace esta funcionando
	 */
	public boolean isLinkUp() {
		return linkUp;
	}

	/**
	 * @return the linkCost
	 */
	public Integer getCost() {
		return isLinkUp() ? linkCost : DVTable.UNREACHABLE;
	}

	/**
	 * Retorna se o enlace se recuperou de uma queda ou n�o.
	 * 
	 * @return
	 */
	public boolean getRecovery() {
		return recovery;
	}

	/**
	 * @return the routerConnected
	 */
	public RouterConfiguration getRouterConnected() {
		return routerConnected;
	}

	/**
	 * Recupera a contagem para o timeout para considerar o enlace down.
	 * 
	 * @return Contagem do timeout
	 */
	public Integer getTimeout() {
		return timeout;
	}

	/**
	 * @param linkState
	 *            the linkState to set
	 */
	public void setLinkUp(boolean linkState) {
		this.linkUp = linkState;
	}

	/**
	 * Marca o enlace para recupera��o de queda ou recupera��o da mesma
	 * 
	 * @param bool
	 *            Estado do enlace
	 */
	public void setRecovery(boolean bool) {
		recovery = bool;
	}

}
