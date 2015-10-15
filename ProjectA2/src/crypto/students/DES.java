package crypto.students;

/**
 * Created by lld on 15/10/3.
 * refer to the code of https://dl.dropboxusercontent.com/u/31222469/blog/crypto/DES.java
 */
import java.math.BigInteger;
import java.nio.ByteBuffer;

// please, consider to use unsigned long comparisons
// to avoid any problem...
public class DES {

    private BigInteger key;
    private byte[] IP = {
            58, 50, 42, 34, 26, 18, 10, 2,
            60, 52, 44, 36, 28, 20, 12, 4,
            62, 54, 46, 38, 30, 22, 14, 6,
            64, 56, 48, 40, 32, 24, 16, 8,
            57, 49, 41, 33, 25, 17,  9, 1,
            59, 51, 43, 35, 27, 19, 11, 3,
            61, 53, 45, 37, 29, 21, 13, 5,
            63, 55, 47, 39, 31, 23, 15, 7
    };
    private byte[] InverseIP = {
            40, 8, 48, 16, 56, 24, 64, 32,
            39, 7, 47, 15, 55, 23, 63, 31,
            38, 6, 46, 14, 54, 22, 62, 30,
            37, 5, 45, 13, 53, 21, 61, 29,
            36, 4, 44, 12, 52, 20, 60, 28,
            35, 3, 43, 11, 51, 19, 59, 27,
            34, 2, 42, 10, 50, 18, 58, 26,
            33, 1, 41,  9, 49, 17, 57, 25
    };
    private byte[] E = {
            32,  1,  2,  3,  4,  5,
            4,  5,  6,  7,  8,  9,
            8,  9, 10, 11, 12, 13,
            12, 13, 14, 15, 16, 17,
            16, 17, 18, 19, 20, 21,
            20, 21, 22, 23, 24, 25,
            24, 25, 26, 27, 28, 29,
            28, 29, 30, 31, 32,  1
    };
    public byte[] P = {
            16,  7, 20, 21,
            29, 12, 28, 17,
            1, 15, 23, 26,
            5, 18, 31, 10,
            2,  8, 24, 14,
            32, 27,  3,  9,
            19, 13, 30,  6,
            22, 11,  4, 25
    };
    private byte[][][] S = {
            { 		{ 14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7 },
                    { 0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8 },
                    { 4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0 },
                    { 15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13 }
            },
            { 		{ 15, 1, 8, 14, 6, 11, 3, 2, 9, 7, 2, 13, 12, 0, 5, 10 },
                    { 3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5 },
                    { 0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15 },
                    { 13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9 }
            },
            { 		{ 10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8 },
                    { 13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1 },
                    { 13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7 },
                    { 1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12 }
            },
            { 		{ 7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15 },
                    { 13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9 },
                    { 10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4 },
                    { 3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14 }
            },
            { 		{ 2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9 },
                    { 14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6 },
                    { 4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14 },
                    { 11, 8, 12, 7, 1, 14, 2, 12, 6, 15, 0, 9, 10, 4, 5, 3 }
            },
            { 		{ 12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11 },
                    { 10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8 },
                    { 9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6 },
                    { 4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13 }

            },
            { 		{ 4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1 },
                    { 13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6 },
                    { 1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2 },
                    { 6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12 }

            },
            { 		{ 13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7 },
                    { 1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2 },
                    { 7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8 },
                    { 2, 1, 14, 7, 4, 10, 18, 13, 15, 12, 9, 0, 3, 5, 6, 11 }

            } };
    private byte[] PC1 = {
            57, 49, 41, 33, 25, 17,  9,
            1, 58, 50, 42, 34, 26, 18,
            10,  2, 59, 51, 43, 35, 27,
            19, 11,  3, 60, 52, 44, 36,
            63, 55, 47, 39, 31, 23, 15,
            7, 62, 54, 46, 38, 30, 22,
            14,  6, 61, 53, 45, 37, 29,
            21, 13,  5, 28, 20, 12,  4
    };
    private byte[] PC2 = {
            14, 17, 11, 24,  1,  5,
            3, 28, 15,  6, 21, 10,
            23, 19, 12,  4, 26,  8,
            16,  7, 27, 20, 13,  2,
            41, 52, 31, 37, 47, 55,
            30, 40, 51, 45, 33, 48,
            44, 49, 39, 56, 34, 53,
            46, 42, 50, 36, 29, 32
    };
    private byte[] Shifts = {
            1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1
    };

    public DES(BigInteger key) {
        this.key = key;
    }
    public long cipher_function(long r, long k) {
        byte[] eR = permuate(longToBytes(r), E);
        byte[] byteK = longToBytes(k);
        byte[] xorResult = xor(eR, byteK);
        byte[] result = substituation6to4(xorResult);
        result = permuate(result, P);
        return new BigInteger(result).longValue();
    }

    // the xor function of two byte arrays
    public byte[] xor(byte[] first, byte[] second) {
        byte[] result = new byte[first.length];
        for(int i = 0; i < result.length; i++) {
            result[i] = (byte)(first[i] ^ second[i]);
        }
        return result;
    }

    // selection functions taking 6 bits blocks and yieling 4 bits blocks
    public byte[] substituation6to4 (byte[] input) {
        byte[] byteOfsix = separateBytes(input, 6);
        byte[] result = new byte[byteOfsix.length/2];
        int halfByte = 0;
        for (int b = 0; b < input.length; b++) {
            byte valByte = input[b];
            int r = 2 * (valByte >> 7 & 0x0001) + (valByte >> 2 & 0x0001);
            int c = valByte >> 3 & 0x000F;
            int val = S[b][r][c];
            if (b % 2 == 0)
                halfByte = val;
            else
                result[b / 2] = (byte) (16 * halfByte + val);
        }
        return result;
    }

    // separate the byte array to 6 bits blocks
    public byte[] separateBytes (byte[] input, int length) {
        int numOfBytes = (8 * input.length - 1) / length + 1;
        byte[] result = new byte[numOfBytes];
        for(int i = 0; i < result.length; i++) {
            for (int j = 0; j < length; j++) {
                int value = getBit(input, i * length + j);
                setBit(result, 8 * i + j, value);
            }
        }
        return result;
    }

    public long[] key_schedule() {
        byte[] initialKey = key.shiftRight(key.bitLength() - 64).toByteArray();
        byte[] pc1Key = permuate(initialKey, PC1);
        byte[] C = extractBits(pc1Key, 0, PC1.length/2);
        byte[] D = extractBits(pc1Key, PC1.length/2, PC1.length/2);
        long[] result = new long[16];
        for (int i = 0; i < 16; i++) {
            C = rotateLeft(C, 28, Shifts[i]);
            D = rotateLeft(D, 28, Shifts[i]);
            byte[] key = concatBits(C, 28, D, 28);
            result[i] = new BigInteger(key).longValue();
        }
        return result;
    }


    public long crypt(long input_block, boolean encrypt_mode) {
        long result = 0L;
        byte[] L = new byte[4];
        byte[] R = new byte[4];
        if(encrypt_mode) {
            byte[] inputIp = permuate(longToBytes(input_block), IP);
            L = extractBits(inputIp, 0, IP.length/2);
            R = extractBits(inputIp, IP.length/2,IP.length/2);
            long[] key = key_schedule();
            for(int i = 0; i < 16; i++) {
                byte[] temp = L;
                L = R;
                R = xor(temp, longToBytes(cipher_function(bytesToLong(R), key[i])));
            }

            result = bytesToLong(permuate(concatBits(R, IP.length/2, L, IP.length/2), InverseIP));
        } else {
            byte[] inputIp = permuate(longToBytes(input_block), IP);
            L = extractBits(inputIp, 0, IP.length/2);
            R = extractBits(inputIp, IP.length/2,IP.length/2);
            long[] key = key_schedule();
            for(int i = 0; i < 16; i++) {
                byte[] temp = R;
                R = L;
                L = xor(temp, longToBytes(cipher_function(bytesToLong(L), key[15 - i])));
            }
            result = bytesToLong(permuate(concatBits(R, IP.length/2, L, IP.length/2), InverseIP));
        }
        return result;
    }
    public long encrypt(long block) {
        return crypt(block, true);
    }
    public long decrypt(long block) {
        return crypt(block, false);
    }

    public byte[] permuate(byte[] input, byte[] table) {
        int numOfBytes = (table.length - 1)/8 + 1;
        byte[] result = new byte[numOfBytes];
        for(int i = 0; i < table.length; i++) {
            int value = getBit(input, table[i] - 1);
            result = setBit(result, i, value);
        }
        return result;
    }

    // get the value of the index of the table
    private int getBit(byte[] table, int index) {
            byte indexByte = table[index / 8];
            int value = indexByte >> (8 - (index % 8 + 1)) & 0x0001;
            return value;
    }

    // set the value at the index of the input
    private byte[] setBit(byte[] input, int index, int value) {
        byte indexByte = input[index/8];
        indexByte = (byte) (((0xFF7F >> (index % 8)) & indexByte) & 0x00FF);
        byte newByte = (byte) ((value << (8 - ((index % 8 ) + 1))) | indexByte);
        input[index/8] = newByte;
        return input;
    }

    public static void main(String args[]) {
        DES des = new DES(new BigInteger("0f1571c947d9e859", 16));
        System.out.println(0x02468aceeca86420L);
        System.out.println(0xda02ce3a89ecac3bL);
        System.out.println(des.decrypt(0xda02ce3a89ecac3bL));
        System.out.println(des.encrypt(0x02468aceeca86420L));
    }

    // get bits from index of the input and the length is n
    private byte[] extractBits(byte[] input, int index, int n) {
        int numOfBytes = (n - 1) / 8 + 1;
        byte[] result = new byte[numOfBytes];
        for (int i = 0; i < n; i++) {
            int val = getBit(input, index + i);
            setBit(result, i, val);
        }
        return result;
    }

    // shiftLeft rotately
    private byte[] rotateLeft(byte[] input, int length, int shiftLen ) {
        int numOfBytes = (length - 1) / 8 + 1;
        byte[] result = new byte[numOfBytes];
        for(int i =0; i < length; i ++) {
            int val = getBit(input, (i + shiftLen) % length);
            setBit(result, i, val);
        }
        return result;
    }

    private byte[] concatBits(byte[] first, int firstLen, byte[] second, int secondLen) {
        int numOfBytes = (firstLen + secondLen - 1) / 8 + 1;
        byte[] result = new byte[numOfBytes];
        int j = 0;
        for (int i = 0; i < firstLen; i++) {
            int val = getBit(first, i);
            setBit(result, j, val);
            j++;
        }

        for (int i = 0; i < secondLen; i++) {
            int val = getBit(second, i);
            setBit(result, j, val);
            j++;
        }
        return result;
    }

    // convert the long to bytes
    public byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
    // convert the bytes to long
    public long bytesToLong(byte[] bytes) {
        return new BigInteger(bytes).longValue();
    }
}
