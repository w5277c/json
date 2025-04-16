/*--------------------------------------------------------------------------------------------------------------------------------------------------------------
Владельцем данного исходного кода является Удовиченко Константин Александрович, емайл:w5277c@gmail.com, по всем правовым вопросам обращайтесь на email.
----------------------------------------------------------------------------------------------------------------------------------------------------------------
07.01.2017	w5277c@gmail.com		Начало
--------------------------------------------------------------------------------------------------------------------------------------------------------------*/
package ru.pp.w5277c.json;

public class Utils {
	private	final	static	char[]	ALPHABET	= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
	private final	static	int[]	toInt		= new int[128];
	static {
		for(int i=0; i< ALPHABET.length; i++){
			toInt[ALPHABET[i]]= i;
		}
	}

	public static byte[] parseHexBinary(String hexString) {
		if(null == hexString || hexString.isEmpty()) {
			return new byte[0x00];
		}

		byte[] result = new byte[hexString.length() / 0x02];
		for(int pos = 0; pos < result.length; pos++) {
			String substr = hexString.substring(pos * 0x02, pos * 0x02 + 0x02).toLowerCase();
			result[pos] = (byte)Integer.parseInt(substr, 16);
		}
		return result;
	}

	public static String printHexBinary(byte[] bytes) {
		if(null == bytes || 0 == bytes.length) {
			return "";
		}

		StringBuilder result = new StringBuilder();
		for(int pos = 0; pos < bytes.length; pos++) {
			String num = Integer.toHexString(bytes[pos] & 0xff).toLowerCase();
			if(num.length() < 0x02) {
				result.append("0");
			}
			result.append(num);
		}
		return result.toString();
	}

	public static byte[] parseBase64Binary(String base64String) {
		int delta = base64String.endsWith( "==" ) ? 2 : base64String.endsWith( "=" ) ? 1 : 0;
		byte[] result = new byte[base64String.length()*3/4 - delta];
		int mask=0xff;
		int index=0x00;
		for(int pos=0; pos<base64String.length(); pos+=4){
			int c0 = toInt[base64String.charAt(pos)];
			int c1 = toInt[base64String.charAt(pos+1)];
			result[index++]=(byte)(((c0 << 2) | (c1 >> 4)) & mask);
			if(index >= result.length){
				return result;
			}
			int c2 = toInt[base64String.charAt(pos+2)];
			result[index++]=(byte)(((c1 << 4) | (c2 >> 2)) & mask);
			if(index >= result.length){
				return result;
			}
			int c3 = toInt[base64String.charAt(pos+3)];
			result[index++]=(byte)(((c2 << 6) | c3) & mask);
		}
		return result;
	}

	public static String printBase64Binary(byte bytes[]) {
		int size = bytes.length;
		char[] buffer = new char[((size+2)/3)*4];
		int index=0;
		int pos=0;
		while(pos < size){
			byte b0 = bytes[pos++];
			byte b1 = (pos < size) ? bytes[pos++] : 0;
			byte b2 = (pos < size) ? bytes[pos++] : 0;

			int mask = 0x3F;
			buffer[index++] = ALPHABET[(b0 >> 2) & mask];
			buffer[index++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
			buffer[index++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
			buffer[index++] = ALPHABET[b2 & mask];
		}
		switch(size % 3){
			case 1: buffer[--index]  = '=';
			case 2: buffer[--index]  = '=';
		}
		return new String(buffer);
	}
}
