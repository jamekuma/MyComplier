package parser.LR1;

import java.util.List;
import java.util.Objects;

/**
 * ����һ������ʽ
 * @author xjy
 *
 */
public class Production {
	private Nonterminal left;   // �󲿷��ս��
	private List<Symbol> right;  // �Ҳ��ķ����Ŵ�
	
	public Production(Nonterminal left, List<Symbol> right) {
		this.left = left;
		this.right = right;
	}
	
	public int getRightLength() {
		return right.size();
	}
	
	public Nonterminal getLeft() {
		return left;
	}

	public Symbol getRightAt(int pos) {
		return right.get(pos);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Production)) {
			return false;
		}
		Production p = (Production)obj;
		return this.left.equals(p.left) && this.right.equals(p.right);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(left.hashCode(), right.hashCode());
	}
	
}
