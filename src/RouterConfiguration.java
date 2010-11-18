/**
 * Representacao das configuracoes do roteador que serao lidas do arquivo.
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
	 * Cria nova configuracao
	 * 
	 * @param id
	 *            Identificador
	 * @param port
	 *            Porta de escuta
	 * @param address
	 *            Endereco
	 */
	public RouterConfiguration(int id, String port, String address) {
		this.id = id;
		this.port = port;
		this.address = address;
	}

	public Integer getId() {
		return this.id;
	}

	public String getPort() {
		return this.port;
	}

	public String getAddress() {
		return this.address;
	}
}
