package parser.LR1;

/**
 * ·ÇÖÕ½á·û
 * @author xjy
 *
 */
public class Nonterminal extends Symbol {

	public Nonterminal(String text) {
		super(text);
	}
	
//	@Override
//	public boolean equals(Object obj) {
//		if (!(obj instanceof Nonterminal)) {
//			return false;
//		}
//		Nonterminal nt = (Nonterminal) obj;
//		return nt.getText().equals(this.getText());
//	}
//	
//	@Override
//	public int hashCode() {
//		return this.getText().hashCode();
//	}
}
