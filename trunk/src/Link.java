
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
	private long lastActivity;

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
		this.lastActivity = 0;
		this.recovery = false;
	}

	/**
	 * Marca o tempo em que o enlace conseguiu ser utilizado a �ltima vez.
	 * @param Momento da �ltima atividade que ocorreu no enlace
	 */
	public void setLastActivity(long last) {
		this.lastActivity = last;
	}

	/**
	 * Recupera quando o enlace foi utilizado a �ltima vez
	 */
	public long getLastActivity() {
		return this.lastActivity;
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
