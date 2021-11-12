package parser.LR1;

import java.util.HashMap;
import java.util.Map;

/**
 * ��������ʽ�еķ���
 * @author xjy
 *
 */
public abstract class Symbol {
	private String text;
	private Map<String, Object> attribute = new HashMap<>();    // �ķ����ŵ�����
	
	protected Symbol(String text) {
		this.text = text;
	}
	
	public void setAttribute(String key, Object value) {
		attribute.put(key, value);
	}
	
	public Object getAttribute(String key) {
		return attribute.get(key);
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Symbol)) {
			return false;
		}
		Symbol nt = (Symbol) obj;
		return nt.getText().equals(this.getText());
	}
	
	@Override
	public int hashCode() {
		return this.getText().hashCode();
	}
	
}
