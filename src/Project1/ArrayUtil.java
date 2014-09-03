
/****************************************************************************************
 * @file  ArrayUtil.java
 *
 * @author   John Miller
 */

import java.util.Arrays;

import static java.lang.System.arraycopy;

class ArrayUtil
{
    /************************************************************************************
     * Concatenate two arrays of type T to form a new wider array.
     *
     * @see http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java
     *
     * @param arr1  the first array
     * @param arr2  the second array
     * @return  a wider array containing all the values from arr1 and arr2
     */
    public static <T> T [] concat (T [] arr1, T [] arr2)
    {
        T [] result = Arrays.copyOf (arr1, arr1.length + arr2.length);
        arraycopy (arr2, 0, result, arr1.length, arr2.length);
        return result;
    } // concat

    /**************************************************************************************
     * Search an array of type T for an element and return that element's index
     * 
     * @param array the array to search
     * @param element  the element to search for
     * @return an int >=0 if element exists, -1 otherwise
     */
    public static <T> int indexOf(T [] array, T element){
    	
    	for(int i=0; i<array.length; i++){
    		if(array[i].equals(element)){
    			return i;
    		}
    	}
    	
    	return -1;
    }
} // ArrayUtil class

