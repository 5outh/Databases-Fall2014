import java.io.Serializable;
import java.util.function.Predicate;


public interface TableInterface {
	public TableInterface project(String s);
	
	public TableInterface select(Predicate<Comparable[]> p);
	
	public TableInterface select(KeyType k);
	public TableInterface union(Table t);
	public TableInterface minus(Table t);
	public TableInterface join(String s, String e, Table x);
	public boolean insert(Comparable[] t);
}
