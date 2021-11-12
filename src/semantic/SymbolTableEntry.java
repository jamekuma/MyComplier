package semantic;

/**
 * ���ű�ı���
 * @author xjy
 *
 */
public class SymbolTableEntry {
	private String name;  // ������
	private String type;  // ��������
	private int offset = -1;  // ��Ե�ַ
	private SymbolTable table = null;  // ָ���µķ��ű��ָ��(���� һ������)
	
	public SymbolTableEntry(String name, String type, int offset) {
		this.name = name;
		this.type = type;
		this.offset = offset;
	}
	public SymbolTableEntry(String name, String type, SymbolTable table) {
		this.name = name;
		this.type = type;
		this.table = table;
	}
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}
	public int getOffset() {
		return offset;
	}
	public SymbolTable getTable() {
		return table;
	}
	
	@Override
	public String toString() {
		return "<" + this.name + ", " + this.type + ", " + (this.offset == -1 ? "-" : this.offset) + ">";
	}
	
}
