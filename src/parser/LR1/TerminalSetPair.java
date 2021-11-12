package parser.LR1;

import java.util.Objects;
import java.util.Set;

/*
	 * 给终结符集新增标注是否含ε
	 */
public class TerminalSetPair {
	public Set<Terminal> set;
	public boolean emptySymbol;

	TerminalSetPair(Set<Terminal> set, boolean emptySymbol) {
		this.set = set;
		this.emptySymbol = emptySymbol;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TerminalSetPair)) {
			return false;
		}
		TerminalSetPair tsp = (TerminalSetPair) obj;
		return this.set.equals(tsp.set) && this.emptySymbol == tsp.emptySymbol;
	}

	@Override
	public int hashCode() {
		return Objects.hash(set.hashCode(), emptySymbol);
	}
}
