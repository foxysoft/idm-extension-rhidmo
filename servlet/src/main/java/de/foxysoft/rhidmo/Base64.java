package de.foxysoft.rhidmo;

import java.util.Arrays;

/**
 *
 * Additional performance optimizations applied.
 */
public class Base64 {
	public static final String BASE64_PREFIX = "{B64}";
	private static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	private static final int[] IA = new int[256];

	static {
		Arrays.fill(IA, -2);
	    for (int i = 0, iS = CA.length; i < iS; i++) {
	    	IA[CA[i]] = i;
	    }

	    IA[61] = 0;
	    IA[13] = -1;
	    IA[10] = -1;
	    IA[9] = -1;
	    IA[32] = -1;
	}

	public static String encode(byte[] byteArr) {
		int sLen = (byteArr != null) ? byteArr.length : 0;
	    if(sLen == 0) {
	    	return "";
	    }

	    int eLen = sLen / 3 * 3;
	    int cCnt = (sLen - 1) / 3 + 1 << 2;
	    int dLen = cCnt;

	    int left = sLen - eLen;
	    int resLen = dLen;
	    if (left > 0) {
	    	if(left == 2) {
	    		resLen--;
	    	}
	    	else {
	    		resLen -= 2;
	    	}
	    }

	    char[] dArr = new char[resLen];

	    for(int s = 0, d = 0; s < eLen; ) {
	    	int i = (byteArr[s++] & 0xFF) << 16 | (byteArr[s++] & 0xFF) << 8 | byteArr[s++] & 0xFF;

	    	dArr[d++] = CA[i >>> 18 & 0x3F];
	    	dArr[d++] = CA[i >>> 12 & 0x3F];
	    	dArr[d++] = CA[i >>> 6 & 0x3F];
	    	dArr[d++] = CA[i & 0x3F];
	    }

	    if(left > 0) {
	    	int i = (byteArr[eLen] & 0xFF) << 10 | ((left == 2) ? ((byteArr[sLen - 1] & 0xFF) << 2) : 0);

	    	dArr[dLen - 4] = CA[i >> 12];
	    	dArr[dLen - 3] = CA[i >>> 6 & 0x3F];
	    	if (left == 2) {
	    		dArr[dLen - 2] = CA[i & 0x3F];
	    	}
	    }

	    return new String(dArr);
	}

	  public static byte[] decode(String aSource){
	    int sLen = (aSource != null) ? aSource.length() : 0;
	    if (sLen == 0) {
	    	return new byte[0];
	    }

	    int sepCnt = countIllegalCharacters(aSource.toCharArray(), sLen);

	    int pad = 0;
	    for (int i = sLen; i > 1 && IA[aSource.charAt(--i)] <= 0;) {
	    	if (aSource.charAt(i) == '=') {
	    		pad++;
	    	}
	    }

	    int len = ((sLen - sepCnt) * 6 >> 3) - pad;

	    byte[] dArr = new byte[len];

	    for (int s = 0, d = 0; d < len; ) {
	    	int i = 0;
	    	for (int j = 0; j < 4; j++) {
	    		int c = (s < sLen) ? IA[aSource.charAt(s++)] : 0;
	    		if (c >= 0) {
	    			i |= c << 18 - j * 6;
	    		} else {
	    			j--;
	    		}
	    	}

	    	dArr[d++] = (byte)(i >> 16);
	    	if (d < len) {
	    		dArr[d++] = (byte)(i >> 8);
	    		if (d < len) {
	    			dArr[d++] = (byte)i;
	    		}
	    	}
	    }
	    return dArr;
	  }

	  private static int countIllegalCharacters(char[] b64chars, int charsLen) {
		  int sepCnt = 0;
		  for (int i = 0; i < charsLen; i++) {
			  if (IA[b64chars[i]] < 0) {
				  sepCnt++;
				  if (IA[b64chars[i]] < -1) {
					  return 0;
				  }
		      	}
		  }
		  return sepCnt;
	  }
}