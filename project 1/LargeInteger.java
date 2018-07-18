/**********************************************************************
*   Author:         Michael Adams
*   Last Edit:      3/1/2018
*
*   note: LargeInteger used here was built on work done in CS1501
***********************************************************************/

import java.util.Random;
import java.math.BigInteger;
import java.lang.StringBuilder;
import java.util.Arrays;
import java.io.Serializable;

public class LargeInteger implements Serializable {
    public static final byte[] ONE = {(byte) 1};
    public static final byte[] ZERO = {(byte) 0}; //does the same thing as ONE basically
    private byte[] val;

    // create an empty LargeInteger of default size
    public LargeInteger() {
        this(512);
    }

    // create an empty LargeInteger of given size
    public LargeInteger(int n) {
        this(new byte[n]);
    }

    /**
     * Construct the LargeInteger from a given byte array
     * @param b the byte array that this LargeInteger should represent
     */
    public LargeInteger(byte[] b) {
        val = b;
    }

    /**
     * Construct the LargeInteger by generatin a random n-bit number that is
     * probably prime (2^-100 chance of being composite).
     * @param n the bitlength of the requested integer
     * @param rnd instance of java.util.Random to use in prime generation
     */
    public LargeInteger(int n, Random rnd) {
        val = BigInteger.probablePrime((n-1), rnd).toByteArray();
    }
    
    /**
     * Return this LargeInteger's val
     * @return val
     */
    public byte[] getVal() {
        return val;
    }

    /**
     * Return the number of bytes in val
     * @return length of the val byte array
     */
    public int length() {
        return val.length;
    }

    /** 
     * Add a new byte as the most significant in this
     * @param extension the byte to place as most significant
     */
    public void extend(byte extension) {
        byte[] newv = new byte[val.length + 1];
        newv[0] = extension;
        for (int i = 0; i < val.length; i++) {
            newv[i + 1] = val[i];
        }
        val = newv;
    }

    /**
     * If this is negative, most significant bit will be 1 meaning most 
     * significant byte will be a negative signed number
     * @return true if this is negative, false if positive
     */
    public boolean isNegative() {
        return (val[0] < 0);
    }

    /**
     * Computes the sum of this and other
     * @param other the other LargeInteger to sum with this
     */
    public LargeInteger add(LargeInteger other) {
        byte[] a, b;
        // If operands are of different sizes, put larger first ...
        if (val.length < other.length()) {
            a = other.getVal();
            b = val;
        }
        else {
            a = val;
            b = other.getVal();
        }

        // ... and normalize size for convenience
        if (b.length < a.length) {
            int diff = a.length - b.length;

            byte pad = (byte) 0;
            if (b[0] < 0) {
                pad = (byte) 0xFF;
            }

            byte[] newb = new byte[a.length];
            for (int i = 0; i < diff; i++) {
                newb[i] = pad;
            }

            for (int i = 0; i < b.length; i++) {
                newb[i + diff] = b[i];
            }

            b = newb;
        }

        // Actually compute the add
        int carry = 0;
        byte[] res = new byte[a.length];
        for (int i = a.length - 1; i >= 0; i--) {
            // Be sure to bitmask so that cast of negative bytes does not
            //  introduce spurious 1 bits into result of cast
            carry = ((int) a[i] & 0xFF) + ((int) b[i] & 0xFF) + carry;

            // Assign to next byte
            res[i] = (byte) (carry & 0xFF);

            // Carry remainder over to next byte (always want to shift in 0s)
            carry = carry >>> 8;
        }

        LargeInteger res_li = new LargeInteger(res);
    
        // If both operands are positive, magnitude could increase as a result
        //  of addition
        if (!this.isNegative() && !other.isNegative()) {
            // If we have either a leftover carry value or we used the last
            //  bit in the most significant byte, we need to extend the result
            if (res_li.isNegative()) {
                res_li.extend((byte) carry);
            }
        }
        // Magnitude could also increase if both operands are negative
        else if (this.isNegative() && other.isNegative()) {
            if (!res_li.isNegative()) {
                res_li.extend((byte) 0xFF);
            }
        }

        // Note that result will always be the same size as biggest input
        //  (e.g., -127 + 128 will use 2 bytes to store the result value 1)
        return res_li;
    }

    /**
     * Negate val using two's complement representation
     * @return negation of this
     */
    public LargeInteger negate() {
        byte[] neg = new byte[val.length];
        int offset = 0;

        // Check to ensure we can represent negation in same length
        //  (e.g., -128 can be represented in 8 bits using two's 
        //  complement, +128 requires 9)
        if (val[0] == (byte) 0x80) { // 0x80 is 10000000
            boolean needs_ex = true;
            for (int i = 1; i < val.length; i++) {
                if (val[i] != (byte) 0) {
                    needs_ex = false;
                    break;
                }
            }
            // if first byte is 0x80 and all others are 0, must extend
            if (needs_ex) {
                neg = new byte[val.length + 1];
                neg[0] = (byte) 0;
                offset = 1;
            }
        }

        // flip all bits
        for (int i  = 0; i < val.length; i++) {
            neg[i + offset] = (byte) ~val[i];
        }

        LargeInteger neg_li = new LargeInteger(neg);
    
        // add 1 to complete two's complement negation
        return neg_li.add(new LargeInteger(ONE));
    }

    /**
     * Implement subtraction as simply negation and addition
     * @param other LargeInteger to subtract from this
     * @return difference of this and other
     */
    public LargeInteger subtract(LargeInteger other) {
        return this.add(other.negate());
    }

    /**
     * Compute the product of this and other
     * @param other LargeInteger to multiply by this
     * @return product of this and other
     */
    public LargeInteger multiply(LargeInteger other) {
        LargeInteger result = new LargeInteger(); //initialize result to 0
        LargeInteger thisCopy, otherCopy; //make copies of this and other
        boolean neg = false; //check if we need to negate

        if (this.isNegative()) {
            thisCopy = this.negate(); //this is neg so negate
            neg = !neg;
        }
        else {
            thisCopy = this.makeCopy(); //this is pos so just copy
        }

        if (other.isNegative()) {
            otherCopy = other.negate(); //other is neg so negate
            neg = !neg;
        }
        else {
            otherCopy = other.makeCopy(); //other is pos so just copy
        }

        while (!otherCopy.isZero()) { //iterate thru otherCopy
            if (!otherCopy.isEven()) {
                result = result.add(new LargeInteger(extendArr(thisCopy.getVal())));
            }
            thisCopy = thisCopy.leftShift(1);
            otherCopy = otherCopy.rightShift(1);
        }

        if (neg) {
            result = result.negate(); //negate result if one of the operands was negative
        }
        return result;
    }
    
    /**
     * Run the extended Euclidean algorithm on this and other
     * @param other another LargeInteger
     * @return an array structured as follows:
     *   0:  the GCD of this and other
     *   1:  a valid x value
     *   2:  a valid y value
     * such that this * x + other * y == GCD in index 0
     */
    public LargeInteger[] XGCD(LargeInteger other) {
        LargeInteger[] returnArr = new LargeInteger[3]; //create return LargeInteger arr
        LargeInteger s = new LargeInteger(ZERO); //intialize s to zero
        LargeInteger t = new LargeInteger(ONE); //intialize t to one
        LargeInteger r = other.makeCopy(); //copy other
        LargeInteger s2 = new LargeInteger(ONE);
        LargeInteger t2 = new LargeInteger(ZERO);
        LargeInteger r2 = this.makeCopy();
        LargeInteger q;
        LargeInteger temp;

        while (!r.isZero()) {
            q = r2.divide(r);
            temp = r;
            LargeInteger res1 = q.multiply(temp);
            r = (r2.subtract(res1));
            r2 = temp;

            temp = s;
            LargeInteger res2 = q.multiply(temp);
            s = (s2.subtract(res2));
            s2 = temp;

            temp = t;
            LargeInteger res3 = q.multiply(temp);
            
            t = (t2.subtract(res3));
            t2 = temp;
        }
        returnArr[0] = r2.trimLeadZeros();
        returnArr[1] = s2.trimLeadZeros();
        returnArr[2] = t2.trimLeadZeros();
        return returnArr;
    }

    /**
      * Compute the result of raising this to the power of y mod n
      * @param y exponent to raise this to
      * @param n mod value to use
      * @return this^y mod n
     */
    public LargeInteger modularExp(LargeInteger y, LargeInteger n) {
        LargeInteger zeroInt = new LargeInteger(ZERO);
        LargeInteger oneInt = new LargeInteger(ONE);
        LargeInteger base = this.makeCopy();

        if (y.equals(oneInt.negate())) { //mod inverse if == -1
            return this.modularInverse(n);
        }
        if (y.lessThan(zeroInt)) { //moc inverse if < 0
            System.out.println("modInverse instead of negative modExp");
            base = base.modularInverse(n);
            y = y.negate();
        }

        LargeInteger result = new LargeInteger(ONE);
        LargeInteger exp = y.makeCopy();
        //compute result
        while (!exp.isZero()) {
            if (!exp.isEven()) { //if exponent is odd, do additional mult
                result = result.multiply(base).mod(n);
            }
            exp = exp.rightShift(1); 
            base = base.multiply(base).mod(n); //square base
        }
        return result;
    }

    public LargeInteger constModularExp(LargeInteger y, LargeInteger n) {
        LargeInteger zeroInt = new LargeInteger(ZERO);
        LargeInteger oneInt = new LargeInteger(ONE);
        LargeInteger base = this.makeCopy();
        LargeInteger temp = new LargeInteger();

        if (y.equals(oneInt.negate())) { //mod inverse if == -1
            return this.modularInverse(n);
        }
        if (y.lessThan(zeroInt)) { //moc inverse if < 0
            System.out.println("modInverse instead of negative modExp");
            base = base.modularInverse(n);
            y = y.negate();
        }

        LargeInteger result = new LargeInteger(ONE);
        LargeInteger exp = y.makeCopy();
        //compute result
        while (!exp.isZero()) {
            if (!exp.isEven()) {
                result = result.multiply(base).mod(n);
            }
            else if (exp.isEven()) { //extraneous mult to even time
                temp = result.multiply(base).mod(n);
            }
            exp = exp.rightShift(1);
            base = base.multiply(base).mod(n);
        }
        return result;
    }

    // make a copy of the array
    public LargeInteger makeCopy() {
        byte[] copyVal = new byte[this.length()];

        for (int i = 0; i < this.length(); i++) {
            copyVal[i] = this.val[i];
        }

        return new LargeInteger(copyVal);
    }

    // is LargeInteger zero?
    public boolean isZero() {
        for (int i = 0; i < this.length(); i++) {
            if (this.val[i] != 0) {
                return false;
            }
        }
        return true;
    }

    // is LargeInteger even?
    public boolean isEven() {
        byte lsb = (byte) (this.val[this.length() - 1] & 0x1);
        return (lsb == 0);
    }

    // extend byte array by 1 byte
    public static byte[] extendArr(byte[] arr) {
        byte[] extendedArr = new byte[arr.length + 1];
        //copy into extended arr
        for (int i = 0; i < arr.length; i++) {
            extendedArr[i+1] = arr[i];
        }
        return extendedArr;
    }

    // shifts LargetInteger to the left a specific distance
    public LargeInteger leftShift(int amt) {
        byte[] arr = this.makeCopy().getVal();

        for (int i = 0; i < amt; i++) {
            boolean carry = false;
            boolean inc = (arr[0] & 0x80) != 0;

            if (inc) {
                arr = extendArr(arr);
            }

            for (int j = arr.length - 1; j >= 0; j--) {
                boolean msb = ((byte) (arr[j] & 0x80)) != 0;
                arr[j] <<= 1;
                if (carry) {
                    arr[j] |= 1;
                }

                if (msb) {
                    carry = true;
                } else {
                    carry = false;
                }
            }
        }

        return new LargeInteger(arr);
    }

    // shifts LargetInteger to the right a specific distance
    public LargeInteger rightShift(int amt) {
        byte[] arr = this.makeCopy().getVal();

        for (int i = 0; i < amt; i++) {
            boolean carry = false;

            for (int j = 0; j < arr.length; j++) {
                boolean lsb = ((byte) (arr[j] & 0x1)) != 0;
                boolean msb = ((byte) (arr[j] & 0x80)) != 0;

                arr[j] = (byte) ((arr[j] & 0xFF) >> 1);
                if (carry) {
                    arr[j] |= 0x80;
                }

                if (lsb) {
                    carry = true;
                } else {
                    carry = false;
                }
            }
            arr[0] &= 0x7F;   
        }
        return new LargeInteger(arr);
    }

    // wrapper to call actual dividing method
    public LargeInteger divide(LargeInteger other) {
        return divAndMod(this, other)[0];
    }

    // compute division and modulus
    public LargeInteger[] divAndMod(LargeInteger x, LargeInteger y) {
        LargeInteger dividend, divisor;
        boolean invQuo = false;
        boolean invRem = false;

        if (x.isNegative()) {
            invQuo = !invQuo;
            invRem = true;
            dividend = x.negate();
        } else {
            dividend = x.makeCopy();
        }

        if (y.isNegative()) {
            invQuo = !invQuo;
            divisor = y.negate();
        } else {
            divisor = y.makeCopy();
        }

        if (dividend.equals(divisor)) {
            LargeInteger one = new LargeInteger(ONE);

            if (invQuo) {
                one = one.negate();
            }
            return new LargeInteger[] { one, new LargeInteger(ZERO) };
        }

        if (dividend.lessThan(divisor)) {
            return new LargeInteger[] { dividend, new LargeInteger(ZERO) };
        }

        LargeInteger rem = new LargeInteger();
        LargeInteger quo = new LargeInteger(dividend.length());

        for (int i = 0; i < dividend.length() * 8; i++) {
            rem = rem.leftShift(1);

            if (dividend.getBit(i)) {
                rem = rem.setLsb(true);
            }

            if (!(rem.trimLeadZeros().lessThan(divisor))) {
                rem = rem.subtract(divisor);
                quo = quo.setBit(i);
            }
        }

        if (invQuo) {
            quo = quo.negate();
        }

        if (invRem) {
            rem = rem.negate();
        }
        return new LargeInteger[] { quo, rem };
    }

    // extract specific bit
    public static byte extractBit(byte b, byte offset) {
        if ((b & offset) == 0) {
            return (byte) 0;
        } else {
            return (byte) 1;
        }
    }

    // trim off leading zeros
    public LargeInteger trimLeadZeros() {
        int leadZeros = 0;

        for (int i = 0; i < this.length(); i++) {
            if (this.val[i] == 0) {
                leadZeros++;
            } else {
                break;
            }
        }
        byte[] newArr = new byte[this.length() - leadZeros];
        for (int i = 0; i < newArr.length; i++) {
            newArr[i] = this.val[i + leadZeros];
        }
        return new LargeInteger(newArr);
    }

    // compute inverse
    public LargeInteger modularInverse(LargeInteger other) {
        LargeInteger base = other.makeCopy();        
        base = base.add(new LargeInteger(ONE));

        while (!base.mod(this).isZero()) {
            base = base.add(other);
        }

        return base.divide(this);
    }

    // compute modulus
    public LargeInteger mod(LargeInteger other) {
        if (this.lessThan(other)) {
            return this.makeCopy();
        } else if (this.equals(other)) {
            return new LargeInteger();
        }

        LargeInteger div = this.divide(other.makeCopy());
        LargeInteger mult = div.multiply(other.makeCopy());
        LargeInteger result = this.subtract(mult);

        return new LargeInteger(extendArr(result.getVal()));
    }

    // get specific bit
    public boolean getBit(int i) {
        int byteIndx = i / 8;
        int bitIndx = 7 - (i - (byteIndx * 8));
        return (this.val[byteIndx] & (int) Math.pow(2, bitIndx)) != 0;
    }

    // set specific bit to 1
    public LargeInteger setBit(int i) {
        LargeInteger copyInt = this.makeCopy();
        int byteIndx = i / 8;
        int bitIndx = 7 - (i - (byteIndx * 8));
        copyInt.val[byteIndx] |= (int) Math.pow(2, bitIndx);
        return copyInt;
    }

    // set the LSB to 0/1
    public LargeInteger setLsb(boolean bit) {
        LargeInteger copyInt = this.makeCopy();

        if (bit) {
            copyInt.val[copyInt.length() - 1] |= 0x1;
        } else {
            copyInt.val[copyInt.length() - 1] &= 0xFE;
        }
        return copyInt;
    }

    // compute GCD
    private static LargeInteger gcd(LargeInteger a, LargeInteger b) {
        if (b.isZero()) {
            return a;
        } else {
            return gcd(b, a.mod(b));
        }
    }

    // wrapper to call actual GCD
    public LargeInteger gcd(LargeInteger other) {
        return this.gcd(this, other);
    }

    // string stuff so we can actually see what the values are
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (int i = 0; i < this.length(); i++) {
            s.append(String.format("%8s",
                Integer.toBinaryString(this.val[i] & 0xFF)).replace(' ', '0'));
        }

        return s.toString();
    }

    // standard compareTo
    // -1 if this < other
    // 0 if this == other
    // 1 if this > other
    public int compareTo(LargeInteger other) {
        byte[] offsets = { (byte) 0x1, (byte) 0x2, (byte) 0x4,
            (byte) 0x8, (byte) 0x10, (byte) 0x20, (byte) 0x40, (byte) 0x80 };
        LargeInteger thisTrim = this.trimLeadZeros();
        LargeInteger otherTrim = other.trimLeadZeros();

        if (thisTrim.length() > otherTrim.length()) {
            return 1;
        } else if (thisTrim.length() < otherTrim.length()) {
            return -1;
        }

        byte[] thisVal = thisTrim.getVal();
        byte[] otherVal = otherTrim.getVal();

        for (int i = 0; i < thisTrim.length(); i++) {
            for (int j = 7; j >= 0; j--) {
                byte byte1 = extractBit(thisVal[i], offsets[j]);
                byte byte2 = extractBit(otherVal[i], offsets[j]);

                if (byte1 > byte2) {
                    return 1;
                } else if (byte1 < byte2) {
                    return -1;
                }
            }
        }

        return 0;
    }

    public LargeInteger subtractOne() {
        return this.subtract(new LargeInteger(ONE));
    }

    public boolean equals(LargeInteger other) {
        return this.compareTo(other) == 0;
    }

    public boolean greaterThan(LargeInteger other) {
        return this.compareTo(other) > 0;
    }

    public boolean lessThan(LargeInteger other) {
        return this.compareTo(other) < 0;
    }
}