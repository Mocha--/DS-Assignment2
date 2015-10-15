package crypto.students;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Base64;

import org.apache.log4j.Logger;

public class StreamCipher {
	
	private static Logger log = Logger.getLogger(StreamCipher.class);
	
	private BigInteger key;
	private BigInteger prime;
	private BigInteger p1;
	private BigInteger p2;
	private BigInteger r_i;
	
	public StreamCipher(BigInteger share, BigInteger prime, BigInteger p, BigInteger q) {
		this.key = share; // shared key from DH
		this.prime = prime; // DH prime modulus
		this.p1 = Supplementary.deriveSuppementaryKey(share, p);
		this.p2 = Supplementary.deriveSuppementaryKey(share, q);
		this.r_i = Supplementary.parityWordChecksum(share); // shift register
	}
	
	/***
	 * Updates the shift register for XOR-ing the next byte.
	 */
	public void updateShiftRegister() {
		r_i = (p1.multiply(r_i).add(p2)).mod(prime);
	}

	/***
	 * This function returns the shift register to its initial possition
	 */
	public void reset() {
		r_i = Supplementary.parityWordChecksum(key);
	}
	
	/***
	 * Gets N numbers of bits from the MOST SIGNIFICANT BIT (inclusive).
	 * @param value Source from bits will be extracted
	 * @param n The number of bits taken
	 * @return The n most significant bits from value
	 */
	private byte msb(BigInteger value, int n) {
		if(value.compareTo(BigInteger.valueOf((int)(Math.pow(2, n) - 1))) == -1){
		    return (byte)(Integer.parseInt(value.toString(2),2));
		}
		else {
			String binaryS = value.shiftRight(value.bitLength() - n).toString(2);
			return (byte)(Integer.parseInt(binaryS,2));
		}
	}
	
	/***
	 * Takes a cipher text/plain text and decrypts/encrypts it.
	 * @param msg Either Plain Text or Cipher Text.
	 * @return If PT, then output is CT and vice-versa.
	 */
	private byte[] _crypt(byte[] msg) {
		byte[] processedMsg = new byte[msg.length];
		for (int i = 0; i < msg.length; i++) {
			processedMsg[i] = (byte) (((int) msg[i]) ^ ((int) (msb(r_i, 8))));
			updateShiftRegister();
		}
		return processedMsg;
	}
	
	//-------------------------------------------------------------------//
	// Auxiliary functions to perform encryption and decryption          //
	//-------------------------------------------------------------------//
	public String encrypt(String msg) {
		// input: plaintext as a string
		// output: a base64 encoded ciphertext string
		log.debug("line to encrypt: [" + msg + "]");
		String result = null;
		try {
			byte[] asArray = msg.getBytes("UTF-8");
			result = Base64.getEncoder().encodeToString(_crypt(asArray));
			log.debug("encrypted text: [" + result + "]");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public String decrypt(String msg) {
		// input: a base64 encoded ciphertext string 
		// output: plaintext as a string
		log.debug("line to decrypt (base64): [" + msg + "]");
		String result = null;
		try {
			byte[] asArray = Base64.getDecoder().decode(msg.getBytes("UTF-8"));
			result = new String(_crypt(asArray), "UTF-8");
			log.debug("decrypted text; [" + result + "]");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

}
