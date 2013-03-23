/**
 * $Id$
 * 
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 * 
 */
package verse.util;

/**
 * Converts bytes to hexadecimal string and hexadecimal string to bytes
 * 
 */
public class Hex {
	private static final String HEXES = "0123456789abcdef";

	/**
	 * Convert byte array to hexadecimal string
	 * 
	 * @param raw
	 *            byte array
	 * @return hexadecimal string
	 */
	public static String encode(byte[] raw) {
		if (raw == null) {
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * raw.length);
		for (final byte b : raw) {
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(
					HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}

	/**
	 * Convert hexadecimal string to byte array
	 * 
	 * @param s
	 *            hexadecimal string
	 * @return byte array
	 */
	public static byte[] decode(String s) {
		int len = s.length();
		byte[] raw = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			int first = Character.digit(s.charAt(i), 16);
			int second = Character.digit(s.charAt(i + 1), 16);
			raw[i / 2] = (byte) ((first << 4) + second);
		}
		return raw;
	}
}
