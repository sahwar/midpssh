/*
 * This file is part of "MidpSSH".
 * 
 * This file was adapted from Java Secure Channel (www.jcraft.com/jsch/) for
 * MidpSSH by Karl von Randow
 * 
 * Copyright (c) 2002,2003,2004 ymnk, JCraft,Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The names of the authors may not be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,
 * INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package ssh.v2;

import java.util.Random;

public class DHKeyExchange {

	public static final byte[] g = { 2 };

	public static final byte[] p = { (byte) 0x00, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xC9, (byte) 0x0F, (byte) 0xDA, (byte) 0xA2,
			(byte) 0x21, (byte) 0x68, (byte) 0xC2, (byte) 0x34, (byte) 0xC4,
			(byte) 0xC6, (byte) 0x62, (byte) 0x8B, (byte) 0x80, (byte) 0xDC,
			(byte) 0x1C, (byte) 0xD1, (byte) 0x29, (byte) 0x02, (byte) 0x4E,
			(byte) 0x08, (byte) 0x8A, (byte) 0x67, (byte) 0xCC, (byte) 0x74,
			(byte) 0x02, (byte) 0x0B, (byte) 0xBE, (byte) 0xA6, (byte) 0x3B,
			(byte) 0x13, (byte) 0x9B, (byte) 0x22, (byte) 0x51, (byte) 0x4A,
			(byte) 0x08, (byte) 0x79, (byte) 0x8E, (byte) 0x34, (byte) 0x04,
			(byte) 0xDD, (byte) 0xEF, (byte) 0x95, (byte) 0x19, (byte) 0xB3,
			(byte) 0xCD, (byte) 0x3A, (byte) 0x43, (byte) 0x1B, (byte) 0x30,
			(byte) 0x2B, (byte) 0x0A, (byte) 0x6D, (byte) 0xF2, (byte) 0x5F,
			(byte) 0x14, (byte) 0x37, (byte) 0x4F, (byte) 0xE1, (byte) 0x35,
			(byte) 0x6D, (byte) 0x6D, (byte) 0x51, (byte) 0xC2, (byte) 0x45,
			(byte) 0xE4, (byte) 0x85, (byte) 0xB5, (byte) 0x76, (byte) 0x62,
			(byte) 0x5E, (byte) 0x7E, (byte) 0xC6, (byte) 0xF4, (byte) 0x4C,
			(byte) 0x42, (byte) 0xE9, (byte) 0xA6, (byte) 0x37, (byte) 0xED,
			(byte) 0x6B, (byte) 0x0B, (byte) 0xFF, (byte) 0x5C, (byte) 0xB6,
			(byte) 0xF4, (byte) 0x06, (byte) 0xB7, (byte) 0xED, (byte) 0xEE,
			(byte) 0x38, (byte) 0x6B, (byte) 0xFB, (byte) 0x5A, (byte) 0x89,
			(byte) 0x9F, (byte) 0xA5, (byte) 0xAE, (byte) 0x9F, (byte) 0x24,
			(byte) 0x11, (byte) 0x7C, (byte) 0x4B, (byte) 0x1F, (byte) 0xE6,
			(byte) 0x49, (byte) 0x28, (byte) 0x66, (byte) 0x51, (byte) 0xEC,
			(byte) 0xE6, (byte) 0x53, (byte) 0x81, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
			(byte) 0xFF };

	static final int RSA = 0;

	static final int DSS = 1;

	private byte[] V_S;

	private byte[] V_C;

	private byte[] I_S;

	private byte[] I_C;

	private byte[] K_S;

	private byte[] H;

	private byte[] e, f, K;

	private DHBasicKeyPairGenerator myKpairGen;

	private DHBasicAgreement myKeyAgree;

	public DHKeyExchange() {
		myKpairGen = new DHBasicKeyPairGenerator();
		myKeyAgree = new DHBasicAgreement();
		getE();
	}

	public byte[] getE() {
		if (e == null) {
			BigInteger p = new BigInteger( DHKeyExchange.p );
			myKpairGen.generateKeyPair( new Random(), p, new BigInteger( g ) );
			myKeyAgree.init(myKpairGen.getPrivate(), p );
			e = myKpairGen.getPublic().toByteArray();
		}
		return e;
	}

	/**
	 * @return Returns the k.
	 */
	public byte[] getK() {
		if (K == null) {
			K = myKeyAgree.calculateAgreement( new BigInteger( f ) ).toByteArray();
		}
		return K;
	}

	/**
	 * 
	 * @param K_S
	 * @param f
	 * @param r
	 *            part 1 of sig of H
	 * @param s
	 *            part 2 of sig of H
	 * @return
	 * @throws Exception
	 */
	public boolean next(byte[] K_S, byte[] f, byte[] sig_of_h) {

		int i, j;

		// K_S is server_key_blob, which includes ....
		// string ssh-dss
		// impint p of dsa
		// impint q of dsa
		// impint g of dsa
		// impint pub_key of dsa

		this.f = f;
		getK();

		//The hash H is computed as the HASH hash of the concatenation
		// of the
		//following:
		// string V_C, the client's version string (CR and NL excluded)
		// string V_S, the server's version string (CR and NL excluded)
		// string I_C, the payload of the client's SSH_MSG_KEXINIT
		// string I_S, the payload of the server's SSH_MSG_KEXINIT
		// string K_S, the host key
		// mpint e, exchange value sent by the client
		// mpint f, exchange value sent by the server
		// mpint K, the shared secret
		// This value is called the exchange hash, and it is used to
		// authenti-
		// cate the key exchange.

		SshPacket2 buf = new SshPacket2(null);
		buf.putString(V_C);
		buf.putString(V_S);
		buf.putString(I_C);
		buf.putString(I_S);
		buf.putString(K_S);
		buf.putMpInt(e);
		buf.putMpInt(f);
		buf.putMpInt(K);
		byte[] foo = buf.getData();

		SHA1Digest sha = new SHA1Digest();
		sha.update(foo, 0, foo.length);

		H = new byte[sha.getDigestSize()];
		sha.doFinal(H, 0);

		//System.out.print("H -> "); //dump(H, 0, H.length);

		/*
		 * SshPacket2 pp = new SshPacket2( null ); pp.putBytes( K_S );
		 * System.out.println( "READ \"" + pp.getString() + "\"" );
		 * pp.getMpInt(); System.out.println( "READ p" ); pp.getMpInt();
		 * System.out.println( "READ q" ); pp.getMpInt(); System.out.println(
		 * "READ g" ); pp.getMpInt(); System.out.println( "READ y" );
		 */

		i = 0;
		j = 0;
		j = ((K_S[i++] << 24) & 0xff000000) | ((K_S[i++] << 16) & 0x00ff0000)
				| ((K_S[i++] << 8) & 0x0000ff00) | ((K_S[i++]) & 0x000000ff);
		String alg = new String(K_S, i, j);
		i += j;

		boolean result = false;

		if (alg.equals("ssh-dss")) {
			byte[] q = null;
			byte[] tmp;
			byte[] p;
			byte[] g;

			j = ((K_S[i++] << 24) & 0xff000000)
					| ((K_S[i++] << 16) & 0x00ff0000)
					| ((K_S[i++] << 8) & 0x0000ff00)
					| ((K_S[i++]) & 0x000000ff);
			tmp = new byte[j];
			System.arraycopy(K_S, i, tmp, 0, j);
			i += j;
			p = tmp;
			j = ((K_S[i++] << 24) & 0xff000000)
					| ((K_S[i++] << 16) & 0x00ff0000)
					| ((K_S[i++] << 8) & 0x0000ff00)
					| ((K_S[i++]) & 0x000000ff);
			tmp = new byte[j];
			System.arraycopy(K_S, i, tmp, 0, j);
			i += j;
			q = tmp;
			j = ((K_S[i++] << 24) & 0xff000000)
					| ((K_S[i++] << 16) & 0x00ff0000)
					| ((K_S[i++] << 8) & 0x0000ff00)
					| ((K_S[i++]) & 0x000000ff);
			tmp = new byte[j];
			System.arraycopy(K_S, i, tmp, 0, j);
			i += j;
			g = tmp;
			j = ((K_S[i++] << 24) & 0xff000000)
					| ((K_S[i++] << 16) & 0x00ff0000)
					| ((K_S[i++] << 8) & 0x0000ff00)
					| ((K_S[i++]) & 0x000000ff);
			tmp = new byte[j];
			System.arraycopy(K_S, i, tmp, 0, j);
			i += j;
			byte[] y = tmp;
			//	SignatureDSA sig=new SignatureDSA();
			//	sig.init();
			DSASigner sig = new DSASigner();
			sig.setY(new BigInteger(y));
			sig.setP(new BigInteger(p));
			sig.setQ(new BigInteger(q));
			sig.setG(new BigInteger(g));

			result = sig.verifySignature(H, sig_of_h);
		} else {
			System.out.println("unknow alg");
		}
		return result;
	}

	/**
	 * @param i_c
	 *            The i_C to set.
	 */
	public void setI_C(byte[] i_c) {
		I_C = i_c;
	}

	/**
	 * @param i_s
	 *            The i_S to set.
	 */
	public void setI_S(byte[] i_s) {
		I_S = i_s;
	}

	/**
	 * @param v_c
	 *            The v_C to set.
	 */
	public void setV_C(byte[] v_c) {
		V_C = v_c;
	}

	/**
	 * @param v_s
	 *            The v_S to set.
	 */
	public void setV_S(byte[] v_s) {
		V_S = v_s;
	}

	/**
	 * @return Returns the h.
	 */
	public byte[] getH() {
		return H;
	}
}