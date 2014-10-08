
/************************************************************************************
 * @file BpTreeMap.java
 *
 * @author  John Miller
 */

import java.io.*;
import java.lang.reflect.Array;
import static java.lang.System.out;
import java.util.*;

/************************************************************************************
 * This class provides B+Tree maps.  B+Trees are used as multi-level index structures
 * that provide efficient access for both point queries and range queries.
 */
public class BpTreeMap <K extends Comparable <K>, V>
       extends AbstractMap <K, V>
       implements Serializable, Cloneable, SortedMap <K, V>
{
    /** The maximum fanout for a B+Tree node.
     */
    private static final int ORDER = 4;

    /** The class for type K.
     */
    private final Class <K> classK;

    /** The class for type V.
     */
    private final Class <V> classV;

    /********************************************************************************
     * This inner class defines nodes that are stored in the B+tree map.
     */
    private class Node
    {
        boolean   isLeaf;
        int       nKeys;
        K []      key;
        Object [] ref;

        @SuppressWarnings("unchecked")
        Node (boolean _isLeaf)
        {
            isLeaf = _isLeaf;
            nKeys  = 0;
            key    = (K []) Array.newInstance (classK, ORDER - 1);
            if (isLeaf) {
                ref = new Object [ORDER];
            } else {
                ref = (Node []) Array.newInstance (Node.class, ORDER);
            } // if
        } // constructor

        // Yeah, whatever.
        public int nKeys() {
            for(int i = 0; i < key.length; i++) {
                if(key[i] == null) {
                    return i;
                }
            }
            return key.length;
        }

        // Remove first element of a node
        public K removeFirst() {

            K k = null;
            // Use list interface to easily remove first element
            ArrayList<K> keyList = new ArrayList(Arrays.asList(this.key));
            ArrayList<Object> refList = new ArrayList(Arrays.asList(this.ref));
            k = keyList.get(0);

            keyList.remove(0); refList.remove(0);

            // Reset lists
            key = (K []) Array.newInstance (classK, ORDER - 1);
            if (isLeaf) {
                ref = new Object [ORDER];
            } else {
                ref = (Node []) Array.newInstance (Node.class, ORDER);
            }

            for(int i = 0; i < keyList.size(); i++) {
                this.key[i] = keyList.get(i);
            }

            for(int i = 0; i < refList.size(); i++) {
                this.ref[i] = refList.get(i);
            }

            this.nKeys--;
            
            // @DEBUG
            for(int i = 0; i < this.nKeys(); i++) {
                out.println(this.key[i] + " : " + this.ref[i]);
            }

            return k;
        }

        // insert a key into the node
        public void insertKey(K k) {
            ArrayList<K> keyList = new ArrayList<K>(Arrays.asList(key));
            int i = 0;
            for(K _k : keyList) {
                if(keyList.get(i) == null) {
                    // Reached the end of sequence
                    keyList.add(i, k);
                    break;
                } else if(keyList.get(i).compareTo(k) >= 0) {
                    // Found a value greater than what we want to insert
                    keyList.add(i, k);
                    break;
                }
                i++;
            }

            // Get rid of trailing nulls
            keyList.trimToSize();

            // Copy contents
            for(int j = 0; j < ORDER - 1; j++) {
                if(j < keyList.size()) {
                    this.key[j] = keyList.get(j);
                } else {
                    this.key[j] = null;
                }
            }
            nKeys++;
        }

        // Returns: Whether or not it was inserted properly
        // Assumption: Node is leaf
        public boolean insertKeyValue(K k, V v) {
            if(!this.isLeaf) {
                return false;
            } else {
                this.insertKey(k);
                this.pointRight(k, v);
                return true;
            }
        }

        // Returns: Whether or not it was inserted properly
        // Assumption: Node is internal
        public boolean insertKeyNode(K k, Node n) {
            if(this.isLeaf) {
                return false;
            } else {
                this.insertKey(k);
                this.pointRight(k, n);
                return true;
            }
        }

        // Get the left pointer from a key
        public Object leftPointer(K k) {
            int idx = Arrays.asList(key).indexOf(k);
            if(idx == -1) return null;
            return ref[idx];
        }

        // Get the right pointer from a key
        public Object rightPointer(K k) {
            int idx = Arrays.asList(key).indexOf(k);
            if(idx == -1) return null;
            return ref[idx + 1];
        }

        // Set left pointer for key
        public void pointLeft(K k, Object o) {
            int idx = Arrays.asList(key).indexOf(k);
            if(idx == -1) return;
            ref[idx] = o;
        }

        // Set right pointer for key
        public void pointRight(K k, Object o) {
            int idx = Arrays.asList(key).indexOf(k);
            if(idx == -1) return;
            ref[idx + 1] = o;
        }
    } // Node inner class

    /** The root of the B+Tree
     */
    private Node root;

    /** The counter for the number nodes accessed (for performance testing).
     */
    private int count = 0;
    
    private int totalKeys = 0;

    /********************************************************************************
     * Construct an empty B+Tree map.
     * @param _classK  the class for keys (K)
     * @param _classV  the class for values (V)
     */
    public BpTreeMap (Class <K> _classK, Class <V> _classV)
    {
        classK = _classK;
        classV = _classV;
        root   = new Node (true);
    } // constructor

    /********************************************************************************
     * Return null to use the natural order based on the key type.  This requires the
     * key type to implement Comparable.
     */
    public Comparator <? super K> comparator () 
    {
        return null;
    } // comparator

    /********************************************************************************
     * Return a set containing all the entries as pairs of keys and values.
     * @return  the set view of the map
     */
    public Set <Map.Entry <K, V>> entrySet ()
    {
        //Set <Map.Entry <K, V>> enSet = new HashSet <> ();
        K firstkey = firstKey();
        K lastkey = lastKey();
        SortedMap<K, V> sortedmap = new TreeMap<K, V>();
        traverse (root, 0, sortedmap, firstkey, lastkey);
        //enSet.addAll(sortedmap.entrySet());
        //return enSet;
        return sortedmap.entrySet();
    } // entrySet

    /********************************************************************************
     * Given the key, look up the value in the B+Tree map.
     * @param key  the key used for look up
     * @return  the value associated with the key
     */
    @SuppressWarnings("unchecked")
    public V get (Object key)
    {
        return find ((K) key, root);
    } // get

    /********************************************************************************
     * Put the key-value pair in the B+Tree map.
     * @param key    the key to insert
     * @param value  the value to insert
     * @return  null (not the previous value)
     */
    public V put (K key, V value)
    {
        insert (key, value, root, null);
        totalKeys++;
        return null;
    } // put

    /********************************************************************************
     * Return the first (smallest) key in the B+Tree map.
     * @return  the first key in the B+Tree map.
     * @author Deborah Brown
     */
    public K firstKey() 
    {
        if (size() == 0){
            throw new NoSuchElementException();
        }
        if(size() == 1){
            return root.key[0];
        }
        Node current = root;
        while(current.ref[0] != null){
            if (current.isLeaf){
                return current.key[0];
            }
            else{
                current = (Node) current.ref[0];
            }
        }
        throw new NoSuchElementException();
    } // firstKey

    /********************************************************************************
     * Return the last (largest) key in the B+Tree map.
     * @return  the last key in the B+Tree map.
     * @author Deborah Brown
     */
    public K lastKey () 
    {
        if(size() == 0){
            throw new NoSuchElementException();
        }
        Node ln = lastNode (root, 0);
        K k = (K) ln.ref[ln.nKeys() - 1];
        return k;
    }
    
    public Node lastNode (Node current, int level) 
    {
       if ( ! current.isLeaf) {
           return lastNode((Node) current.ref [current.nKeys() - 1], level + 1);
       } 
       else {
           return current;
       }
    }
    
    public void traverse (Node current, int level, SortedMap<K,V> sortedmap, K fromKey, K toKey) 
    {
        for (int i = 0; i < current.nKeys(); i++) {
            if (( ((K)current.ref[i]).compareTo(fromKey) >= 0) &&
                ( ((K)current.ref[i]).compareTo(toKey) <= 0))   
                sortedmap.put(current.key[i], (V)current.ref[i]);
        }
        
        if ( ! current.isLeaf) {
            for (int i = 0; i <= current.nKeys(); i++){ 
                traverse ((Node) current.ref [i], level + 1, sortedmap, fromKey, toKey);
            }
        }
    }

    /********************************************************************************
     * Return the portion of the B+Tree map where key < toKey.
     * @return  the submap with keys in the range [firstKey, toKey)
     */
    public SortedMap <K,V> headMap (K toKey)
    {
        K firstkey = firstKey();
        //V fromV = find (firstkey, root);
        SortedMap<K, V> sortedmap = new TreeMap<K, V>();
        //V toV = find (toKey, root);
        traverse (root, 0, sortedmap, firstkey, toKey); 
        return sortedmap;
    } // headMap

    /********************************************************************************
     * Return the portion of the B+Tree map where fromKey <= key.
     * @return  the submap with keys in the range [fromKey, lastKey]
     */
    public SortedMap <K,V> tailMap (K fromKey)
    {
        K lastkey = lastKey();
        SortedMap<K, V> sortedmap = new TreeMap<K, V>();
        traverse (root, 0, sortedmap, fromKey, lastkey); 
        return sortedmap;

        
    } // tailMap

    /********************************************************************************
     * Return the portion of the B+Tree map whose keys are between fromKey and toKey,
     * i.e., fromKey <= key < toKey.
     * @return  the submap with keys in the range [fromKey, toKey)
     */
    public SortedMap <K,V> subMap (K fromKey, K toKey)
    {
        SortedMap<K, V> sortedmap = new TreeMap<K, V>();
        traverse (root, 0, sortedmap, fromKey, toKey); 
        return sortedmap;
    } // subMap

    /********************************************************************************
     * Return the size (number of keys) in the B+Tree.
     * @return  the size of the B+Tree
     */
    public int size ()
    {
       return totalKeys;
    } // size

    /********************************************************************************
     * Print the B+Tree using a pre-order traveral and indenting each level.
     * @param n      the current node to print
     * @param level  the current level of the B+Tree
     */
    @SuppressWarnings("unchecked")
    private void print (Node n, int level)
    {
        if(level == 0) {
            out.println ("BpTreeMap");
            out.println ("-------------------------------------------");
        }
        
        for (int j = 0; j < level; j++) out.print ("\t");
        out.print ("[ . ");
        for (int i = 0; i < n.nKeys(); i++) out.print (n.key [i] + " . ");
        out.println ("]");
        if (!n.isLeaf) {
            for (int i = 0; i <= n.nKeys(); i++){ 
                print ((Node) n.ref [i], level + 1);
            }
        } // if

        if(level == 0) {
            out.println ("-------------------------------------------");
        }
    } // print

    /********************************************************************************
     * Recursive helper function for finding a key in B+trees.
     * @param key  the key to find
     * @param n  the current node
     */
    @SuppressWarnings("unchecked")
    private V find (K key, Node n)
    {
        count++;
        for (int i = 0; i < n.nKeys(); i++) {
            K k_i = n.key [i];
            if (key.compareTo (k_i) <= 0) {
                if (n.isLeaf) {
                    return (key.equals (k_i)) ? (V) n.ref [i] : null;
                } else {
                    return find (key, (Node) n.ref [i]);
                } // if
            } // if
        } // for
        return (n.isLeaf) ? null : find (key, (Node) n.ref [n.nKeys()]);
    } // find


    // Insert a value into the root of the tree (handles splitting)
    // Assumption: Root is the only element (a leaf)
    private void insertRoot(K key, V ref) {
        if(root.nKeys() >= ORDER - 1) {
            Node newRight = split(key, ref, root);
            Node newRoot = new Node(false);
            K rootKey = newRight.key[0]; 
            newRoot.key[0] = rootKey;
            newRoot.pointLeft(rootKey, root);
            newRoot.pointRight(rootKey, newRight);
            newRoot.nKeys = 1;
            this.root = newRoot;
            this.root.isLeaf = false;
        } else {
            this.root.insertKeyValue(key, ref);
        }
    }

    // Insert a k/v pair into an inner node
    // Assumption: n is an inner node 
    private Map.Entry<K,Node> insertInner(K k, V ref, Node n) {
        // Pointer we want to follow to find leaf
        Node ptrToFollow = null;

        // If we have a leaf, defer to insertLeaf
        if(n.isLeaf) {
            out.println("insert pointer " + n);
            return insertLeaf(k, ref, n);
        }

        // k is less than all keys
        if(k.compareTo(n.key[0]) < 0) {
            // follow leftmost path
            out.println("leftmost");
            // This is null pointer
            ptrToFollow = (Node) n.ref[0];
            out.println("pointer " + ptrToFollow);
        } else { 
            for(int i = 0; i < n.nKeys(); i++) {
                if( i == n.nKeys() - 1 
                    || (k.compareTo(n.key[i]) >= 0 && k.compareTo(n.key[i+1]) <= 0) ) {
                    // k >= key[i] and k <= keys[i+1]
                    // Go right from this location
                    ptrToFollow = (Node) n.ref[i + 1];
                }
            }
        }

        Map.Entry<K,Node> overflow = insertInner(k, ref, ptrToFollow);

        if(overflow != null) {
            // If the inner node is full, have to split again
            if(n.nKeys() >= ORDER - 1) {
                    Node rightNode = split(k, overflow.getValue(), n);
                    K newKey = rightNode.removeFirst();
                    out.println("New key: " + newKey);
                    // NOT removal that causes null pointers
                    // Return the first element from the right node of the split, but remove from inner node.
                    out.println(rightNode);
                    return new AbstractMap.SimpleEntry(newKey, rightNode);
            // Otherwise, we just insert it.
            } else {
                n.insertKeyNode(overflow.getKey(), overflow.getValue());
            }
        }
        // Everything's good
        return null;
    }

    // Assumption: n is a leaf node
    private Map.Entry<K, Node> insertLeaf(K key, V ref, Node n) {
        if(n.nKeys() >= ORDER - 1) {
            // Not enough room for another; must split
            Node rightNode = split(key, ref, n);
            // Set n's sibling to rightNode...?
            // Return the RIGHT node to insert into parent.
            return new AbstractMap.SimpleEntry(rightNode.key[0], rightNode);
        } else {
            // Just insert it
            n.insertKeyValue(key, ref);
            return null;
        }
    }

    /********************************************************************************
     * Recursive helper function for inserting a key in B+trees.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @param p    the parent node
     * @author Ben Kovach
     */
    private Map.Entry<K,Node> insert (K key, V ref, Node n, Node p)
    {
        if(n == root && root.isLeaf) {
            // Insert into the root since it's the only element
            insertRoot(key, ref);
            return null;
        } else if(n.isLeaf) {
            // n is a leaf, insert into it. return whatever insertLeaf does.
            return insertLeaf(key, ref, n);
        } else {
            if(n == root) {
                // need to set the root
            }
            // Locate the ref we want to insert into
            // n is an inner node, recurse, insert, handle splits
            return insertInner(key, ref, n);
        }
    }

    /********************************************************************************
     * Wedge the key-ref pair into node n.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     * @param i    the insertion position within node n
     */
    private void wedge (K key, V ref, Node n, int i)
    {
        for (int j = n.nKeys(); j > i; j--) {
            n.key [j] = n.key [j - 1];
            n.ref [j] = n.ref [j - 1];
        } // for
        n.key [i] = key;
        n.ref [i] = ref;
        n.nKeys++;
    } // wedge

    /********************************************************************************
     * Split node n and return the newly created node.
     * @param key  the key to insert
     * @param ref  the value/node to insert
     * @param n    the current node
     */
    private Node split (K key, Object ref, Node n)
    {
        out.println("split node " + n);
        for(K k : n.key) {
            out.println("k: " + k);
        }

        //finds the correct spot in the node
        int newIndexSpot = -1;
        if ( key.compareTo(n.key[0]) < 0){
            newIndexSpot = 0;
        }
        else if (key.compareTo(n.key[n.nKeys() - 1]) > 0){
            newIndexSpot = n.nKeys();
        }
        else {
            // @MODIFIED
           for(int i = 0; i < n.nKeys() - 1; i++){
               if((key.compareTo(n.key[i]) > 0) & (key.compareTo(n.key[i + 1]) < 0)){
                   newIndexSpot = i + 1;
               }
           }
        }    
       
       K []  tempKey = (K []) Array.newInstance (classK, ORDER);

       Object [] tempRef = null;
       if (n.isLeaf) {
            out.println("is leaf");
           tempRef = new Object [ORDER + 1];
       } else {
            out.println("nonleaf");
           tempRef =  (Node []) Array.newInstance (Node.class, ORDER + 1);
       } // if
       
       // TODO: Fails when nonleaf encountered
       for(int i = 0; i < ORDER; i++){
           if(i == newIndexSpot){
               tempKey[newIndexSpot] = key;
               if(n.isLeaf) {
                tempRef[newIndexSpot] = (V) ref; 
               } else {
                tempRef[newIndexSpot] = (Node) ref; 
               }
               
           }
           else if(i > newIndexSpot){
               tempKey[i] = n.key[i - 1];
               tempRef[i] = n.ref[i - 1];
           }
           else{
               tempKey[i] = n.key[i];
               tempRef[i] = n.ref[i];
               //tempRef[i] = java.util.Arrays.copyOfRange(n.ref, i, i);
           }
       }

       Node nodeToReturn = new Node(n.isLeaf);

       //this fills nodeToReturn with correct values and n with correct values
       int numKeysOld = (int)Math.ceil((double)ORDER/2);
       
       n.key    = (K []) Array.newInstance (classK, ORDER - 1);
       if (n.isLeaf) {
          n.ref = new Object [ORDER];
       } else {
           n.ref = (Node []) Array.newInstance (Node.class, ORDER);
       } // if
       
       for(int k = 0; k < ORDER; k++){
           if(k < (ORDER - numKeysOld)){
               n.key[k] = tempKey[k];  
               n.ref[k] = tempRef[k];  
           }
               
           if(k >= (ORDER - numKeysOld)){
              for(int l = 0; l < ORDER - 1; l++){
                   if(nodeToReturn.key[l] == null){
                       nodeToReturn.key[l] = tempKey[k];
                       nodeToReturn.ref[l] = tempRef[k];
                       break;
                   }
               }
           }
       }

       // Set nkeys
       for(int i = 0; i < nodeToReturn.key.length; i++) {
          if(nodeToReturn.key[i] != null) {
            nodeToReturn.nKeys++;
          }
       }
        return nodeToReturn;

    } // split

    /********************************************************************************
     * The main method used for testing.
     * @param  the command-line arguments (args [0] gives number of keys to insert)
     */
    public static void main (String [] args)
    {
        BpTreeMap <Integer, Integer> bpt = new BpTreeMap <> (Integer.class, Integer.class);
        // int totKeys = 10;
        
        // if (args.length == 1) totKeys = Integer.valueOf (args [0]);
        
        // for (int i = 0; i <= totKeys; i += 1) {
        //     bpt.put (i, i * i);
        //     bpt.print(bpt.root, 0);
        // }

        bpt.put(16, 4);
        bpt.put(25, 5);
        bpt.put(9, 3);
        bpt.put(1, 1);
        bpt.put(4, 2);

        bpt.print (bpt.root, 0);
        // for (int i = 0; i < totKeys; i++) {
        //     out.println ("key = " + i + " value = " + bpt.get (i));
        // } // for
        // out.println ("-------------------------------------------");
        // out.println ("Average number of nodes accessed = " + bpt.count / (double) totKeys);
        // SortedMap sm = bpt.headMap(9);
        // SortedMap sm2 = bpt.tailMap(1);
        // SortedMap sm3 = bpt.subMap(1, 4);
        // out.println (sm.toString());
        // out.println (sm2.toString());
        // out.println (sm3.toString());
        // Set <Entry<Integer, Integer>> es = bpt.entrySet();
        // out.println(es.toString());
    } // main

} // BpTreeMap class

