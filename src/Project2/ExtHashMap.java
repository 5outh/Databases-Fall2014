
/************************************************************************************
 * @file ExtHashMap.java
 *
 * @author  John Miller
 */

import java.io.*;
import java.lang.reflect.Array;
import static java.lang.System.out;
import java.util.*;

/************************************************************************************
 * This class provides hash maps that use the Extendable Hashing algorithm.  Buckets
 * are allocated and stored in a hash table and are referenced using directory dir.
 */
public class ExtHashMap <K, V>
       extends AbstractMap <K, V>
       implements Serializable, Cloneable, Map <K, V>
{
    /** The number of slots (for key-value pairs) per bucket.
     */
    private static final int SLOTS = 4;

    /** The class for type K.
     */
    private final Class <K> classK;

    /** The class for type V.
     */
    private final Class <V> classV;

    /********************************************************************************
     * This inner class defines buckets that are stored in the hash table.
     */
    private class Bucket
    {
        int  nKeys;
        K [] key;
        V [] value;
        @SuppressWarnings("unchecked")
        Bucket ()
        {
            nKeys = 0;
            key   = (K []) Array.newInstance (classK, SLOTS);
            value = (V []) Array.newInstance (classV, SLOTS);
        } // constructor
        
        public void add(K key, V value){
        	this.key[nKeys] = key;
        	this.value[nKeys] = value;
        	nKeys++;
        }
        
        public void print(){
        	String top ="\n", bottom = "";	
        	
        	for(int i=0; i<SLOTS; i++){
        		top += "|\t" + this.key[i] + "\t|";
        		bottom += "|\t" + this.value[i] + "\t|";		
        	}
        	top += "\n";
        	bottom += "\n";
        	out.println(top + bottom);
        }

		public int keyIndexOf(K key) {
			for(int i=0; i<this.nKeys; i++){
				if(key == this.key[i])
					return i;
			}
			return -1;
		}
		
		public V removeKey(int index){
			V value = this.value[index];
			//now slide everything to the right of index to the left
			for(int i=index; i<nKeys-1; i++){
				this.key[i] = this.key[i+1];
				this.value[i] = this.value[i+1];
			}
			
			this.key[nKeys-1] = null;
			this.value[nKeys-1] = null;
			this.nKeys--;
			return value;
		}
    } // Bucket inner class

    /** The hash table storing the buckets (buckets in physical order)
     */
    private List <Bucket> hTable;

    /** The directory providing access paths to the buckets (buckets in logical oder)
     */
    private List <Bucket> dir;

    /** The modulus for hashing (= 2^D) where D is the global depth
     */
    private int mod;

    /** The number of buckets
     */
    private int nBuckets;

    /** Counter for the number buckets accessed (for performance testing).
     */
    private int count = 0;

    /********************************************************************************
     * Construct a hash table that uses Extendable Hashing.
     * @param classK    the class for keys (K)
     * @param classV    the class for keys (V)
     * @param initSize  the initial number of buckets (a power of 2, e.g., 4)
     */
    public ExtHashMap (Class <K> _classK, Class <V> _classV, int initSize)
    {
        classK = _classK;
        classV = _classV;
        hTable = initList(initSize);   // for bucket storage
        dir    = hTable;   // for bucket access
        mod    = nBuckets = initSize;
    } // constructor
    
    /**********************************************************************************
     * Helper function to make a list of a bunch of buckets
     * @param size	the initial size of the list to make
     * 
     */
    public ArrayList<Bucket> initList(int size){
    	ArrayList<Bucket> list = new ArrayList<Bucket>(size);
    	
    	for(int i=0; i<size; i++){
    		Bucket b = new Bucket();
    		list.add(b);
    	}
    	
    	return list;
    }
    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     * @author Ben Kovach
     */
    public Set <Map.Entry <K, V>> entrySet ()
    {
        Set <Map.Entry <K, V>> enSet = new HashSet <> ();
            
        for(Bucket b : dir) {
            for(int i = 0; i < b.nKeys; i++) {
                Map.Entry<K, V> entry = new AbstractMap.SimpleEntry<K, V>(b.key[i], b.value[i]);
                enSet.add(entry);
            }
        }

        return enSet;
    } // entrySet

    /********************************************************************************
     * Given the key, look up the value in the hash table.
     * @param key  the key used for look up
     * @return  the value associated with the key
     * @author Ben Kovach
     */
    public V get (Object key)
    {
        int    i = h (key);
        Bucket b = dir.get (i);
        
        for(int j = 0; j < b.nKeys; j++) {
            if(b.key[j].equals(key)) {
                return b.value[j];
            }
        }

        return null;
    } // get

    /********************************************************************************
     * Put the key-value pair in the hash table.
     * @param key    the key to insert ... this is the 
     * @param value  the value to insert
     * @return  null (not the previous value)
     * @author Will Pickard + Will Speegle
     */
    public V put (K key, V value)
    {
        int    i = h (key);
        Bucket b = dir.get(i);
        //If there are as many keys as slots we need to split the bucket
        if(b.nKeys >= SLOTS) {	
            split(b, i);
            this.put(key, value);     
        }
        
        else{//necessary for recursion	
        	b.add(key, value);
                count++;
        }
        return null;
    } // put
    
    /******************
     * *Handles a full bucket by changing the mod value, then calling the 
     * doubleDir() function. Then rehashes all the values to the correct buckets
     * @param b the bucket that is full
     * @param index the index of the full bucket
     * @author Will Pickard + Will Speegle
     */
    public void split(Bucket b, int index){
        //Increase the mod function and Htable, then call doubleDir to double the directory size
        mod *= 2;
        doubleDir();
        Bucket bucket = incrementHTable();
        int newBucketIndex = hTable.indexOf(bucket);
        dir.add(newBucketIndex, this.hTable.get(newBucketIndex));
        
        //Re-hash all the values in the bucket
        for(int i=0; i<b.nKeys; i++){
        	K key = b.key[i];
        	int j = h (key);
        	if(dir.get(j) != b){
        		V value = b.removeKey(i);
        		put(key, value);
        	}
        }
    }
    /******************************
     * Add one empty bucket to the hash table
     * @author Will Pickard
     */
    public Bucket incrementHTable(){
    	ArrayList<Bucket> l = new ArrayList<Bucket>(hTable.size() + 1);
    	for(int i=0; i<hTable.size(); i++){
    		l.add(i, hTable.get(i));
    	}
        Bucket bucket = new Bucket();
    	l.add(bucket);
    	hTable = l;
        nBuckets++;
        return bucket;
    }
    /*******************************
     * Double the dir structure, creating n many empty buckets, where n is the previous size
     * @author Will Pickard + Will Speegle
     */
    public void doubleDir(){
    	//create a new list with new mod size  
        List<Bucket> list = new ArrayList<Bucket>(mod);
    	  
    	  int i=0, size;
          //Add previous entries to list
    	  for(i = 0; i<dir.size(); i++){//copy 
    		  list.add(i, dir.get(i));
          }
          //Map entries to the end of the new list
          for(size = dir.size(); i<mod; i++){
        	  list.add(i, dir.get( (i - dir.size() )) );
          }
          
          dir = list; //double the dir
    }
    /********************************************************************************
     * Return the size (SLOTS * number of buckets) of the hash table. 
     * @return  the size of the hash table
     */
    public int size ()
    {
        return SLOTS * nBuckets;
    } // size

    /********************************************************************************
     * Print the hash table.
     */
    private void print ()
    {
        out.println ("Hash Table (Extendable Hashing)");
        out.println ("-------------------------------------------");
        
        for(Map.Entry<K,V> entry : this.entrySet()) {
            out.println(entry.getKey() + " : " + entry.getValue());
        }
       
        out.println ("-------------------------------------------");
    } // print

    /********************************************************************************
     * Hash the key using the hash function.
     * @param key  the key to hash
     * @return  the location of the directory entry referencing the bucket
     */
    private int h (Object key)
    {
        return key.hashCode () % mod;
    } // h

    /********************************************************************************
     * The main method used for testing.
     * @param  the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main (String [] args)
    {
        ExtHashMap <Integer, Integer> ht = new ExtHashMap <> (Integer.class, Integer.class, 3);
        int nKeys = 30;
        if (args.length == 1) nKeys = Integer.valueOf (args [0]);
        for (int i = 1; i < nKeys; i += 2) ht.put (i, i * i);
      
        ht.print();
        for (int i = 0; i < nKeys; i++) {
            out.println ("key = " + i + " value = " + ht.get (i));
        } // for
        out.println ("-------------------------------------------");
        out.println ("Average number of buckets accessed = " + ht.count / (double) nKeys);
        
    } // main

} // ExtHashMap class

