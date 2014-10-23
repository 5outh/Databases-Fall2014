
/****************************************************************************************
 * @file  Table.java
 *
 * @author   John Miller
 */

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.Arrays;
import java.util.Collection;

import static java.lang.Boolean.*;
import static java.lang.System.out;

/****************************************************************************************
 * This class implements relational database tables (including attribute names, domains
 * and a list of tuples.  Five basic relational algebra operators are provided: project,
 * select, union, minus join.  The insert data manipulation operator is also provided.
 * Missing are update and delete data manipulation operators.
 */
public class Table
       implements Serializable
{
    /** Relative path for storage directory
     */
    private static final String DIR = "store" + File.separator;

    /** Filename extension for database files
     */
    private static final String EXT = ".dbf";

    /** Counter for naming temporary tables.
     */
    private static int count = 0;

    /** Table name.
     */
    private final String name;

    /** Array of attribute names.
     */
    private final String [] attribute;

    /** Array of attribute domains: a domain may be
     *  integer types: Long, Integer, Short, Byte
     *  real types: Double, Float
     *  string types: Character, String
     */
    private final Class [] domain;

    /** Collection of tuples (data storage).
     */
    private final List <Comparable []> tuples;

    /** Primary key. 
     */
    private final String [] key;

    /** Index into tuples (maps key to tuple number).
     */
    private final Map <KeyType, Comparable []> index;

    /** Number of buckets to init the ExtHashMap with
    */
    private final int DEFAULT_BUCKETS = 25;

    //----------------------------------------------------------------------------------
    // Constructors
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Construct an empty table from the meta-data specifications.
     *
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types)
     * @param _key        the primary key
     */  
    public Table (String _name, String [] _attribute, Class [] _domain, String [] _key)
    {
    	out.println("table0 constructor");
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = new ArrayList <> ();
        index     = new BpTreeMap <> (KeyType.class, Comparable [].class);       // also try BPTreeMap, LinHashMap or ExtHashMap
    } // constructor

    /************************************************************************************
     * Construct a table from the meta-data specifications and data in _tuples list.
     *
     * @param _name       the name of the relation
     * @param _attribute  the string containing attributes names
     * @param _domain     the string containing attribute domains (data types)
     * @param _key        the primary key
     * @param _tuple      the list of tuples containing the data
     */  
    public Table (String _name, String [] _attribute, Class [] _domain, String [] _key,
                  List <Comparable []> _tuples)
    {
    	out.println("table1 constructor");
        name      = _name;
        attribute = _attribute;
        domain    = _domain;
        key       = _key;
        tuples    = _tuples;
        index     = new BpTreeMap <> (KeyType.class, Comparable [].class);       // also try BPTreeMap, LinHashMap or ExtHashMap
    } // constructor

    /************************************************************************************
     * Construct an empty table from the raw string specifications.
     *
     * @param name        the name of the relation
     * @param attributes  the string containing attributes names
     * @param domains     the string containing attribute domains (data types)
     */
    public Table (String name, String attributes, String domains, String _key)
    {

        this (name, attributes.split (" "), findClass (domains.split (" ")), _key.split(" "));
        out.println("table2 constructor");
        out.println ("DDL> create table " + name + " (" + attributes + ")");
    } // constructor

    //----------------------------------------------------------------------------------
    // Public Methods
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Project the tuples onto a lower dimension by keeping only the given attributes.
     * Check whether the original key is included in the projection.
     *
     * #usage movie.project ("title year studioNo")
     * @author William Pickard
     * @param attributes  the attributes to project onto
     * @return  a table of projected tuples
     */
    public Table project (String attributes)
    {
        out.println ("RA> " + name + ".project (" + attributes + ")");
        String [] attrs     = attributes.split (" "); //col headers
        Class []  colDomain = extractDom (match (attrs), domain); //col headers' classes
        //if the current primary key(s) is in attrs, then use them, 
        //otherwise make the new temp table's prim key all of attrs
        String [] newKey    = (Arrays.asList (attrs).containsAll (Arrays.asList (key))) ? key : attrs; 
           
        List <Comparable []> rows = new ArrayList<> ();
        //populate the rows with the requested tuples
        //the requested tuples are members of the column attrs[i]
        //we need to reference this tables static attribute string array for these values
        String[] tempAttrs = new String[attrs.length];
        
        for(int i=0; i<tuples.size(); i++){
        	Comparable tempRow[] = new Comparable[attrs.length];
        	for(int j=0; j<attrs.length; j++){
        		tempRow[j] = tuples.get(i)[ArrayUtil.indexOf(attribute, attrs[j])];
        	}
        	rows.add(tempRow);
        }
      
        //pass table a tempName, column headers, the class of each of the column headers, a primary key, and tuples
        return new Table (name + count++, attrs, colDomain, newKey, rows);
    } // project

    /************************************************************************************
     * Select the tuples satisfying the given predicate (Boolean function).
     *
     * #usage movie.select (t -> t[movie.col("year")].equals (1977))
     *
     * @author Benjamin Kovach
     * @param predicate  the check condition for tuples
     * @return  a table with tuples satisfying the predicate
     */
    public Table select (Predicate <Comparable []> predicate)
    {
        // NB. Keep the headers the same, but select the ones satisfying the predicate(s)
        out.println ("RA> " + name + ".select (" + predicate + ")");
        List <Comparable []> rows = tuples.stream().filter(predicate).collect(Collectors.toList());

        return new Table (name + count++, attribute, domain, key, rows);
    } // select

    /************************************************************************************
     * Select the tuples satisfying the given key predicate (key = value).  Use an index
     * (Map) to retrieve the tuple with the given key value.
     *
     * @author Benjamin Kovach
     * @param keyVal  the given key value
     * @return  a table with the tuple satisfying the key predicate
     */
    public Table select (KeyType keyVal)
    {
        out.println ("RA> " + name + ".select (" + keyVal + ")");

        List<Comparable[]> rows = new ArrayList<>();
        Comparable[] row = index.get(keyVal);
        if(row != null) rows.add(row);

        return new Table (name + count++, attribute, domain, key, rows);
    } // select

    /************************************************************************************
     * Union this table and table2.  Check that the two tables are compatible.
     *
     * #usage movie.union (show)
     * @author Deborah Brown
     * @param table2  the rhs table in the union operation
     * @return  a table representing the union
     */
   public Table union (Table table2)
    {
        out.println ("RA> " + name + ".union (" + table2.name + ")");
        if (! compatible (table2)) return null;

        List <Comparable []> rows = compareOperation(table2, "union");

        return new Table (name + count++, attribute, domain, key, rows);
    } // union

    /************************************************************************************
     * Take the difference of this table and table2.  Check that the two tables are
     * compatible.
     *
     * #usage movie.minus (show)
     * @author Deborah Brown
     * @param table2  The rhs table in the minus operation
     * @return  a table representing the difference
     */
    public Table minus (Table table2)
    {
        out.println ("RA> " + name + ".minus (" + table2.name + ")");
        if (! compatible (table2)) return null;

        List <Comparable []> rows = compareOperation(table2, "minus");

        //  T O   B E   I M P L E M E N T E D 

        return new Table (name + count++, attribute, domain, key, rows);
    } // minus

    /************************************************************************************
     * Compare the tables to each other, if the operation is minus, complete the relational algebra minus operation. 
     * If the operation is union, complete the relational algebra union operation.
     *
     * #usage movie.compareOperation (show)
     * @author Deborah Brown
     * @param table2  The rhs table in the minus operation
     * @param operation The string containing either the word 'union' or 'minus' specifiying which operation needs to be completed
     * @return  a List<Comparable[]> representing either the difference or the union appropriately
     */
       public List<Comparable[]> compareOperation(Table table2, String operation){
        List <Comparable[]> rows = new ArrayList<Comparable[]>();
        
        List <Comparable[]> outer = this.tuples;
        List <Comparable[]> inner = table2.tuples;
        if(operation == "union"){
        	rows.addAll(this.tuples);
        	inner = this.tuples;
        	outer = table2.tuples;
        }        
        
        for(int i = 0; i < outer.size(); i++){
        	   boolean found = false;
               Comparable[] outerTuple = outer.get(i);
               for(int j = 0; j < inner.size(); j++){
                      Comparable[] innerTuple = inner.get(j);              
                      if(Arrays.equals(outerTuple, innerTuple)){
                             found = true;
                             break;
                      }              
               }
               
               if(found && operation == "intersection"){
                   rows.add(outerTuple);
               }
               else if(!found && (operation == "minus" || operation == "union" )){
            	 rows.add(outerTuple);
               }
               
        }
        return rows;
    }

    /************************************************************************************
     * Join this table and table2 by performing an equijoin.  Tuples from both tables
     * are compared requiring attributes1 to equal attributes2.  Disambiguate attribute
     * names by append "2" to the end of any duplicate attribute name.
     *
     * #usage movie.join ("studioNo", "name", studio)
     * #usage movieStar.join ("name == s.name", starsIn)
     *
     * @author William Speegle, Benjamin Kovach
     * @param attributes1  the attributes of this table to be compared (Foreign Key)
     * @param attributes2  the attributes of table2 to be compared (Primary Key)
     * @param table2      the rhs table in the join operation
     * @return  a table with tuples satisfying the equality predicate
     */
    public Table join (String attributes1, String attributes2, Table table2)
    {
        out.println ("RA> " + name + ".join (" + attributes1 + ", " + attributes2 + ", "
                                               + table2.name + ")");

        String [] t_attrs = attributes1.split (" ");
        String [] u_attrs = attributes2.split (" ");

        // holds indices of attributes to be compared in both tables 
        int[] t_attr_indices = new int[t_attrs.length];
        int[] u_attr_indices = new int[u_attrs.length];

        // fail if attributes1.length != attributes2.length
        try
        { 
            for(int i = 0; i < t_attrs.length; i++)
            {
                t_attr_indices[i] = ArrayUtil.indexOf(this.attribute, t_attrs[i]);
                u_attr_indices[i] = ArrayUtil.indexOf(table2.attribute, u_attrs[i]);
            } 
        } 
        catch(ArrayIndexOutOfBoundsException e) 
        { 
            out.println("Join failed! Attempted to compare attribute strings of non-equivalent length.");
            throw e;
        }

        List <Comparable []> rows = new ArrayList<>();
        List <Comparable[]> joins = new ArrayList<>();

        for(int i = 0; i < this.tuples.size(); i++)
        {
            // current tuple
            Comparable[] t = this.tuples.get(i);

            // tuples to join t with in the resulting table.
            joins = new ArrayList<>();

            // iterate over tuples and figure out which to join with
            for(int j = 0; j < table2.tuples.size(); j++)
            {
                Comparable[] u = table2.tuples.get(j);
                boolean add = false;
                for(int idx = 0; idx < t_attr_indices.length; idx++) 
                {
                    boolean failed = false;
                    if(!t[t_attr_indices[idx]].equals(u[u_attr_indices[idx]]))
                    {
                        // don't populate with the join
                        break;
                    }
                    add = true;
                }
                // t == u by the comparison
                if(add) joins.add(u);
            }

            // join current tuple with all joinable tuples and add them to rows
            for(Comparable[] c : joins) 
            {
                rows.add(ArrayUtil.concat(t, c));
            }
        }

        String[] attrs = ArrayUtil.concat(attribute, table2.attribute);
        ArrayList<String> attrList = new ArrayList<String>(Arrays.asList( ArrayUtil.concat(attribute, table2.attribute)));
        
        Class[] dom = ArrayUtil.concat( domain, table2.domain );
        ArrayList<Class> domList = new ArrayList<Class>( Arrays.asList( ArrayUtil.concat( domain, table2.domain ) ) );

        // remove attributes2 columns from final thing
        for(String attr : u_attrs)
        {
            // index of things to remove from the list (duplicates)
            int idx = ArrayUtil.indexOf(attrs, attr);

            // remove things at index in attributes list, domain, and each tuple
            attrList.remove(idx);
            domList.remove(idx);
            for(int i = 0; i < rows.size(); i++) 
            {
                ArrayList<Comparable> tupleList = new ArrayList<Comparable>( Arrays.asList( rows.get(i) ) );
                tupleList.remove(idx);
                Comparable[] tuple = new Comparable[tupleList.size()];
                rows.set(i, tupleList.toArray(tuple) );
            }

            attrs = new String[attrList.size()];
            attrs = attrList.toArray(attrs);

            dom = new Class[domList.size()];
            dom = domList.toArray(dom);
        }

        // rename duplicate attributes that aren't joinable
        ArrayList<String> seenAttrs = new ArrayList();

        for(int i = 0; i < attrs.length; i++)
        {
            if(seenAttrs.contains(attrs[i]))
            {
                attrs[i] = attrs[i] + "2";
            }
            else
            {
                seenAttrs.add(attrs[i]);
            }
        }

        return new Table (name + count++, attrs, dom, key, rows);
    } // join

    /************************************************************************************
     * Return the column position for the given attribute name.
     *
     * @param attr  the given attribute name
     * @return  a column position
     */
    public int col (String attr)
    {
        for (int i = 0; i < attribute.length; i++) {
           if (attr.equals (attribute [i])) return i;
        } // for

        return -1;  // not found
    } // col

    /************************************************************************************
     * Insert a tuple to the table.
     *
     * #usage movie.insert ("'Star_Wars'", 1977, 124, "T", "Fox", 12345)
     *
     * @param tup  the array of attribute values forming the tuple
     * @return  whether insertion was successful
     */
    public boolean insert (Comparable [] tup)
    {
        out.println ("DML> insert into " + name + " values ( " + Arrays.toString (tup) + " )");

        if (typeCheck (tup)) {
            tuples.add (tup);
            Comparable [] keyVal = new Comparable [key.length];
            int []        cols   = match (key);
            for (int j = 0; j < keyVal.length; j++) keyVal [j] = tup [cols [j]];
            index.put (new KeyType (keyVal), tup);
            return true;
        } else {
            return false;
        } // if
    } // insert

    /************************************************************************************
     * Get the name of the table.
     *
     * @return  the table's name
     */
    public String getName ()
    {
        return name;
    } // getName

    /************************************************************************************
     * Print this table.
     */
    public void print ()
    {
        out.println ("\n Table " + name);
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
        out.print ("| ");
        for (String a : attribute) out.printf ("%15s", a);
        out.println (" |");
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
        for (Comparable [] tup : tuples) {
            out.print ("| ");
            for (Comparable attr : tup) out.printf ("%15s", attr);
            out.println (" |");
        } // for
        out.print ("|-");
        for (int i = 0; i < attribute.length; i++) out.print ("---------------");
        out.println ("-|");
    } // print

    /************************************************************************************
     * Print this table's index (Map).
     */
    public void printIndex ()
    {
        out.println ("\n Index for " + name);
        out.println ("-------------------");
        for (Map.Entry <KeyType, Comparable []> e : index.entrySet ()) {
            out.println (e.getKey () + " -> " + Arrays.toString (e.getValue ()));
        } // for
        out.println ("-------------------");
    } // printIndex

    /************************************************************************************
     * Load the table with the given name into memory. 
     *
     * @param name  the name of the table to load
     */
    public static Table load (String name)
    {
        Table tab = null;
        try {
            ObjectInputStream ois = new ObjectInputStream (new FileInputStream (DIR + name + EXT));
            tab = (Table) ois.readObject ();
            ois.close ();
        } catch (IOException ex) {
            out.println ("load: IO Exception");
            ex.printStackTrace ();
        } catch (ClassNotFoundException ex) {
            out.println ("load: Class Not Found Exception");
            ex.printStackTrace ();
        } // try
        return tab;
    } // load

    /************************************************************************************
     * Save this table in a file.
     */
    public void save ()
    {
        try {
            ObjectOutputStream oos = new ObjectOutputStream (new FileOutputStream (DIR + name + EXT));
            oos.writeObject (this);
            oos.close ();
        } catch (IOException ex) {
            out.println ("save: IO Exception");
            ex.printStackTrace ();
        } // try
    } // save

    //----------------------------------------------------------------------------------
    // Private Methods
    //----------------------------------------------------------------------------------

    /************************************************************************************
     * Determine whether the two tables (this and table2) are compatible, i.e., have
     * the same number of attributes each with the same corresponding domain.
     *
     * @param table2  the rhs table
     * @return  whether the two tables are compatible
     */
    private boolean compatible (Table table2)
    {
        if (domain.length != table2.domain.length) {
            out.println ("compatible ERROR: table have different arity");
            return false;
        } // if
        for (int j = 0; j < domain.length; j++) {
            if (domain [j] != table2.domain [j]) {
                out.println ("compatible ERROR: tables disagree on domain " + j);
                return false;
            } // if
        } // for
        return true;
    } // compatible

    /************************************************************************************
     * Match the column and attribute names to determine the domains.
     *
     * @param column  the array of column names
     * @return  an array of column index positions
     */
    private int [] match (String [] column)
    {
        int [] colPos = new int [column.length];

        for (int j = 0; j < column.length; j++) {
            boolean matched = false;
            for (int k = 0; k < attribute.length; k++) {
                if (column [j].equals (attribute [k])) {
                    matched = true;
                    colPos [j] = k;
                } // for
            } // for
            if ( ! matched) {
                out.println ("match: domain not found for " + column [j]);
            } // if
        } // for

        return colPos;
    } // match

    /************************************************************************************
     * Extract the attributes specified by the column array from tuple t.
     *
     * @param t       the tuple to extract from
     * @param column  the array of column names
     * @return  a smaller tuple extracted from tuple t 
     */
    private Comparable [] extract (Comparable [] t, String [] column)
    {
        Comparable [] tup = new Comparable [column.length];
        int [] colPos = match (column);
        for (int j = 0; j < column.length; j++) tup [j] = t [colPos [j]];
        return tup;
    } // extract

    /************************************************************************************
     * Check the size of the tuple (number of elements in list) as well as the type of
     * each value to ensure it is from the right domain. 
     *
     * @param t  the tuple as a list of attribute values
     * @return  whether the tuple has the right size and values that comply
     *          with the given domains
     */
    private boolean typeCheck (Comparable [] t)
    { 
        // for (Comparable [] cs : tuples){
        //     for(int i = 0; i < cs.length; i++){
        //         if(!domain[i].isAssignableFrom(cs[i].getClass())){
        //             return false;
        //         }
        //     }
        // }
        return true;
    } // typeCheck

    /************************************************************************************
     * Find the classes in the "java.lang" package with given names.
     *
     * @param className  the array of class name (e.g., {"Integer", "String"})
     * @return  an array of Java classes
     */
    private static Class [] findClass (String [] className)
    {
        Class [] classArray = new Class [className.length];

        for (int i = 0; i < className.length; i++) {
            try {
                classArray [i] = Class.forName ("java.lang." + className [i]);
            } catch (ClassNotFoundException ex) {
                out.println ("findClass: " + ex);
            } // try
        } // for

        return classArray;
    } // findClass

    /************************************************************************************
     * Extract the corresponding domains.
     *
     * @param colPos the column positions to extract.
     * @param group  where to extract from
     * @return  the extracted domains
     */
    private Class [] extractDom (int [] colPos, Class [] group)
    {
        Class [] obj = new Class [colPos.length];

        for (int j = 0; j < colPos.length; j++) {
            obj [j] = group [colPos [j]];
        } // for

        return obj;
    } // extractDom

} // Table class

