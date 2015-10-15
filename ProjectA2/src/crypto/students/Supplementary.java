package crypto.students;

import java.math.BigInteger;

import org.apache.log4j.Logger;

/***
 * In this class all the candidates must implement the methods
 * related to key derivation. You can create auxiliary functions
 * if you need it, using ONLY Java standard classes.
 * 
 * @author Pablo Serrano
 */
public class Supplementary {
	
	private static Logger log = Logger.getLogger(Supplementary.class);
	
	/***
	 * Receives a 2048 bits key and applies a word by word XOR
	 * to yield a 64 bit integer at the end.
	 * 
	 * @param key 2048 bit integer form part A1 DH Key Exchange Protocol
	 * @return A 64 bit integer
	 */
	public static BigInteger parityWordChecksum(BigInteger key) {
        String binaryS = change(key, 10, 2);
        BigInteger result = BigInteger.ZERO;
        for(int i = 0; i < binaryS.length(); i = i + 64) {
            result = result.xor(new BigInteger(binaryS.substring(i, i+64), 2));
        }
		return result;
	}

    /***
     *
     * @param num a BigIntefer number
     * @param from the original radix
     * @param to the returned result radix
     * @return A String that is a binary string with 2048 bits
     */
    public static String change(BigInteger num,int from, int to){
        String binaryS = new java.math.BigInteger(num.toString(), from).toString(to);
        if(binaryS.length()<2048) {
            while(binaryS.length() < 2048) {
                binaryS = "0" + binaryS;
            }
        }
        return binaryS;
    }

	/***
	 * 
	 * @param key 2048 bit integer form part A1 DH Key Exchange Protocol
	 * @param p A random 64 bit prime integer
	 * @return A 64 bit integer for use as a key for a Stream Cipher
	 */
	public static BigInteger deriveSuppementaryKey(BigInteger key, BigInteger p) {
		return key.mod(p);
	}

}
