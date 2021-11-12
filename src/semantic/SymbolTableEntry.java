package semantic;

/**
 * 符号表的表项
 * @author xjy
 *
 */
public class SymbolTableEntry {
	private String name;  // 符号名
	private String type;  // 符号类型
	private int offset = -1;  // 相对地址
	private SymbolTable table = null;  // 指向新的符号表的指针(比如 一个过程)
	
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
