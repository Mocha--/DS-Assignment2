package crypto.students;

import java.math.BigInteger;
import java.util.Random;

import org.apache.log4j.Logger;

/***
 * In this class, all the candidates must implement their own
 * math and crypto functions required to solve any calculation 
 * and encryption/decryption task involved in this project.
 * 
 * @author pabloserrano
 *
 */
public class DHEx {
	
	// debug logger
	private static Logger log = Logger.getLogger(DHEx.class);
	
	private static Random rnd = new Random();
	
	public static BigInteger createPrivateKey(int size) {
		// Create a fixed size random BigInteger
		BigInteger bigInteger = new BigInteger(size,rnd);
		return bigInteger;
	}

	public static BigInteger[] createDHPair(BigInteger generator, BigInteger prime, 
			BigInteger skClient) {
		BigInteger[] pair = new BigInteger[2];
		// pair[0] stores private key of the client
		pair[0] = skClient;
		// pair[1] stores public key of the client, which could be calculated by generator to the power of the skClient
		// modulo prime by using modExp method.
		pair[1] = modExp(generator,skClient,prime);
		return pair;
	}
	
	public static BigInteger getDHSharedKey(BigInteger pk, BigInteger sk, BigInteger prime) {
		BigInteger shared = modExp(pk,sk,prime);
		return shared;
	}
	
	public static BigInteger modExp(BigInteger base, BigInteger exp, BigInteger modulo) {
		// set the result to 1
		BigInteger result = BigInteger.ONE;
		// create a bigInteger which equals 2
		BigInteger two = BigInteger.valueOf(2);
		/*
		set the base equals to base modulo modulo(parameter) to ensure that at the completion of every loop, the variable
		 base is equivalent to (base^(2^i))(mod m) (i is the executed times of the loop)
		*/
		base = base.mod(modulo);
		// if exp equals 0, the loop will end
		while(exp.compareTo(BigInteger.ZERO) == 1) {
			/*
			If the rightest bit of the exp is zero, no code executes since this effectively multiplies the running total
			 by one. If the rightest bit of exp instead is one, the variable base (containing the value (base^(2^i))(mod m)
			  of the original base) is simply multiplied in.
			*/
			if (exp.mod(two).compareTo(BigInteger.ONE) == 0) {
				result = (result.multiply(base)).mod(modulo);
			}
			// exp is right shifted on bit
			exp = exp.shiftRight(1);
			// to ensure that at the completion of every loop, the variable base is equivalent to (base^(2^i))(mod m)
			// (i is the executed times of the loop)
			base = (base.multiply(base)).mod(modulo);
		}
		return result;
	}
}
