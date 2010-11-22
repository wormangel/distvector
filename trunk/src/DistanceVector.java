import java.util.HashMap;

/**
 * Representa um vetor distância
 * 
 * @author Lucas Medeiros
 * @author Otacilio Lacerda
 * @author Pedro Yossis
 * 
 */
public class DistanceVector {

	private Integer id;
	private HashMap<Integer, Integer> distances;

	public DistanceVector(Integer id) {
		this.id = id;
		this.distances = new HashMap<Integer, Integer>();
	}

	/**
	 * @return the peers
	 */
	public HashMap<Integer, Integer> getDistances() {
		return distances;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	public void putDistance(Integer id, Integer cost) {
		distances.put(id, cost);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.getId() + ":[");

		for (Integer key : distances.keySet()) {
			builder.append(key + "=");
			builder.append((distances.get(key).equals(Router.getNetworkSize())) ? "INF, "
					: distances.get(key) + ", ");
		}
		builder = builder.reverse().delete(0, 2).reverse();
		builder.append("]");
		return builder.toString();
	}

	public DistanceVector clone() {
		DistanceVector clone = new DistanceVector(getId());
		for (Integer key : getDistances().keySet()) {
			clone.putDistance(key, getDistances().get(key));
		}
		return clone;
	}

}
