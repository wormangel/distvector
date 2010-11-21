/**
 * Representação das configurações do roteador que serao lidas do arquivo.
 * 
 * @author Lucas Medeiros
 * @author Otacilio Lacerda
 * @author Pedro Yossis
 * 
 */
public class RouterConfiguration {
	Integer id;
	String port;
	String address;

	/**
	 * Cria nova configuração
	 * 
	 * @param id
	 *            Identificador
	 * @param port
	 *            Porta de escuta
	 * @param address
	 *            Endereço
	 */
	public RouterConfiguration(int id, String port, String address) {
		this.id = id;
		this.port = port;
		this.address = address;
	}

	/**
	 * Id do roteador
	 * 
	 * @return id
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * Porta que o roteador escuta
	 * 
	 * @return port
	 */
	public String getPort() {
		return this.port;
	}

	/**
	 * Endereço que o roteador escuta
	 * 
	 * @return address
	 */
	public String getAddress() {
		return this.address;
	}
}
