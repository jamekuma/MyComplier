package parser.LR1;

import java.util.Arrays;
import java.util.Objects;

/**
 * ����ĳ����ʽ��һ����Ŀ
 * @author xjy
 *
 */
public class Item {
	// ��Ŀ = ����ʽ + "��"��λ��
	private Production production;
	private final int position;  // �� ��λ��
	private Terminal next; // չ��������
	
	public Item(Production production, int position, Terminal next) {
		this.production = production;
		this.position = position;
		this.next = next;
	}
	
	public Nonterminal getLeft() {
		return production.getLeft();
	}
	
	public Production getProduction() {
		return production;
	}
	
	/**
	 * ��ȡչ����
	 * @return չ����
	 */
	public Terminal getNext() {  
		return next;
	}
	
	public int getRightLength() {
		return production.getRightLength();
	}
	
	public Symbol getDotSymbol() {
		if (position < production.getRightLength()) {
			return production.getRightAt(position);
		}
		else {
			return null;
		}
	}
	
	public int getDotPosition() {
		return position;
	}
	
	public Symbol getSymbolAt(int pos) {
		if (pos < production.getRightLength()) {
			return production.getRightAt(pos);
		}
		else {
			return null;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Item)) {
			return false;
		}
		Item i = (Item)obj;
		return i.production.equals(this.production) && i.position == this.position && i.next.equals(this.next);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.production.hashCode(), this.position, this.next.hashCode());
	}
	
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("[");
		res.append(production.getLeft().toString());
		res.append(" -> ");
		int i;
		for (i = 0; i < production.getRightLength(); i++) {
			if(i == position) {
				res.append("�� ");
			}
			res.append(production.getRightAt(i) + " ");
		}
		if(i == position) res.append("��");
		res.append("  չ����: " + next.toString());
		res.append("]");
		return res.toString();
	}
	
}
