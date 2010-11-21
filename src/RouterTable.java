import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Tabela de roteamento. Guarda o custo e próximo salto para todos destinos.
 * 
 * @author Lucas Medeiros
 * @author Otacilio Lacerda
 * @author Pedro Yossis
 * 
 */
public class RouterTable {

	/**
	 * Casse interna para ajudar na representação da linha da tabela de
	 * roteamento. Contem o ID do roteador que será o próximo salto e o custo
	 * total ate o destino.
	 */
	public class TableLine {
		public Integer nextRouter;
		public Integer cost;

		public TableLine(Integer next, Integer cost) {
			this.nextRouter = next;
			this.cost = cost;
		}
	}

	private HashMap<Integer, TableLine> routerTable;

	public RouterTable() {
		this.routerTable = new HashMap<Integer, TableLine>();
	}

	/**
	 * Adiciona uma linha a tabela de roteamento
	 * 
	 * @param destination
	 *            Destino
	 * @param nextRouter
	 *            Roteador do próximo salto ate o destino
	 * @param cost
	 *            Custo total para o destino
	 */
	public void addLine(Integer destination, Integer nextRouter, Integer cost) {
		routerTable.put(destination, new TableLine(nextRouter, cost));
	}

	/**
	 * Limpa a tabela de roteamento
	 */
	public void clearRouterTable() {
		routerTable.clear();
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("ROUTER TABLE\n");
		result.append("--------- --------- ---------\n");
		result.append("   Dest      Next      Cost  \n");
		result.append("--------- --------- ---------\n");
		if (routerTable.isEmpty()) {
			result.append("            EMPTY\n");
		} else {
			for (Integer destination : routerTable.keySet()) {
				TableLine line = routerTable.get(destination);
				result.append("|  "
						+ new DecimalFormat("0000").format(destination) + "  ");
				result.append("|  "
						+ new DecimalFormat("0000").format(line.nextRouter)
						+ "   ");
				result.append("|  "
						+ new DecimalFormat("0000").format(line.cost) + "  |\n");
			}
		}
		result.append("--------- --------- ---------\n");
		return result.toString();
	}

}
