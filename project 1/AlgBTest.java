/**********************************************************************
*   Author:         Michael Adams
*   Last Edit:      3/1/2018
*
***********************************************************************/

import java.util.Random;
import java.math.BigInteger;
import java.lang.StringBuilder;
import java.util.Arrays;
import java.util.*;

public class AlgBTest {
	public static final int BITLENGTH = 512;

	public static void main(String[] args) {

	// create rand obj
	Random rnd = new Random();
	
	// create random 512 bit ciphertext (c) and modulus (n) 
	LargeInteger c = new LargeInteger(BITLENGTH, rnd);
	LargeInteger n = new LargeInteger(BITLENGTH, rnd);

	while (n.isEven()) {
		n = new LargeInteger(BITLENGTH, rnd);
		System.out.print(".");
	}
	System.out.println();

	// create random, mostly 0s, all 1s d to test 512 bit private key (d)
	LargeInteger d_rand = new LargeInteger(BITLENGTH, rnd);
	
	byte[] temp0 = new byte[BITLENGTH/8];
	byte[] temp1 = new byte[BITLENGTH/8];
	for (int i = 0; i < temp0.length; i++) {
		temp0[i] = (byte)1;
		temp1[i] = (byte)255;
	}

	LargeInteger d_zeros = new LargeInteger(temp0);
	LargeInteger d_ones = new LargeInteger(temp1);


	// create random 256 bit and 1024 bit private key (d)
	LargeInteger d_short = new LargeInteger(BITLENGTH/2, rnd);
	LargeInteger d_long = new LargeInteger(BITLENGTH*2, rnd);

	/*
	System.out.println("c: " + c.toString());
	System.out.println("n: " + n.toString());
	System.out.println("d_rand: " + d_rand.toString());
	System.out.println("d_zeros: " + d_zeros.toString());
	System.out.println("d_ones: " + d_ones.toString());
	System.out.println("d_short: " + d_short.toString());
	System.out.println("d_long: " + d_long.toString());

	System.out.println("d_rand: " + d_rand.length());
	System.out.println("d_short: " + d_short.length());
	System.out.println("d_long: " + d_long.length()); */

	LargeInteger res = new LargeInteger();
	LargeInteger res1 = new LargeInteger();
	long startTime, stopTime, delta;
	double time = 0;

	for (int i = 0; i < 5; i++) {
		startTime = System.nanoTime();
		res = c.constModularExp(d_rand, n);
		stopTime = System.nanoTime();
		delta = (stopTime - startTime);
		time += delta * Math.pow(10,-9);
		System.out.print(delta + "-");
	}
	time = time / 5;
	System.out.println();
	System.out.println("Avg 512-rand time: " + time);
	time = 0;
	for (int i = 0; i < 5; i++) {
		res = new LargeInteger();
		startTime = System.nanoTime();
		res = c.constModularExp(d_zeros, n);
		stopTime = System.nanoTime();
		delta = (stopTime - startTime);
		time += delta * Math.pow(10,-9);
		System.out.print(delta + "-");
	}
	time = time / 5;
	System.out.println();
	System.out.println("Avg zeros-512 time: " + time);
	time = 0;
	for (int i = 0; i < 5; i++) {
		res = new LargeInteger();
		startTime = System.nanoTime();
		res = c.constModularExp(d_ones, n);
		stopTime = System.nanoTime();
		delta = (stopTime - startTime);
		time += delta * Math.pow(10,-9);
		System.out.print(delta + "-");
	}
	time = time / 5;
	System.out.println();
	System.out.println("Avg ones-512 time: " + time);
	time = 0;
	for (int i = 0; i < 5; i++) {
		res = new LargeInteger();
		startTime = System.nanoTime();
		res = c.constModularExp(d_short, n);
		stopTime = System.nanoTime();
		delta = (stopTime - startTime);
		time += delta * Math.pow(10,-9);
		System.out.print(delta + "-");
	}
	time = time / 5;
	System.out.println();
	System.out.println("Avg rand-256 time: " + time);
	time = 0;
	for (int i = 0; i < 5; i++) {
		res = new LargeInteger();
		startTime = System.nanoTime();
		res = c.constModularExp(d_long, n);
		stopTime = System.nanoTime();
		delta = (stopTime - startTime);
		time += delta * Math.pow(10,-9);
		System.out.print(delta + "-");
	}
	time = time / 5;
	System.out.println();
	System.out.println("Avg rand-1024 time: " + time);
	}
}