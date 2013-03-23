package verse.util;

import java.util.Random;
import verse.dbc.precondition;


/**
 * Generates a guid of an arbitrary size. The guid is easy to read and type, and
 * it can be of an arbitrary length. Guids are written in mixed case and in
 * blocks of 4 chars by default for maximum ease of reading. However, guids
 * should be compared case-insensitive and ignoring block separators (see
 * {@link #compare(String, String)}.)
 */
public class FriendlyGuid {

	/** Make this class impossible to construct. */
	private FriendlyGuid() {
	}

	private static final String ID_CHARS = "ACEfgHJkMnpRsTUvWxY2345679";
//	private static final String ID_CHARS_LOWER = ID_CHARS.toLowerCase();
	private static final String FULL_CHARS_LOWER = "abcdefghijklmnopqrstuvwxyz0123456789";
	private static final int ID_BASE = ID_CHARS.length();
	private static final Random idGenerator = new Random();

	/**
	 * @return A unique, 24-digit string with each 4-digit segment delimited by
	 *         hyphens; can be used to identify objects. See
	 *         {@link #next(int, char, int)}. This string represents
	 *         approximately 128 bits of randomness.
	 */
	public static String next() {
		return next(24, '-', 4);
	}

	/**
	 * @return A unique string with each 4-digit segment delimited by hyphens;
	 *         can be used to identify objects. See
	 *         {@link #next(int, char, int)}.
	 * @param digits
	 *            How many digits should be returned?
	 */
	public static String next(int digits) {
		return next(digits, '-', 4);
	}

	/**
	 * @return A unique string with each 4-digit segment delimited by a
	 *         separator char; can be used to identify objects. See
	 *         {@link #next(int, char, int)}.
	 * @param digits
	 *            How many digits should be returned?
	 * @param separator
	 *            What char should be written between groups of digits? If '\0'
	 *            is used, no separator is written.
	 */
	public static String next(int digits, char separator) {
		return next(digits, separator, 4);
	}

	/**
	 * @return A unique string that can be used to identify objects. We could
	 *         use a unique random number or standard guid, but we're trying to
	 *         make these values short and easy to read; hence the conversion to
	 *         a string with a base > 16, and the use of chars that are
	 *         maximally visually distinct from one another.
	 * @param digits
	 *            How many significant digits should the ID contain? There are
	 *            approximately 26 unique digit chars, so a digits of, say, 4,
	 *            would have a maximum range of 26^4 or about 450k, and 8 digits
	 *            would have a range of about 208 billion.
	 * @param separator
	 *            What char should be written between groups of digits? Default
	 *            is a hyphen. If '\0' is used, no separator is written.
	 * @param segmentLength
	 *            How many chars should be grouped into a segment, delimited by
	 *            the separator char? Default is 4.
	 */
	public static String next(int digits, char separator, int segmentLength) {
		precondition.checkAndExplain(digits > 0,
                "digits must be greater than zero");
		precondition.checkAndExplain(segmentLength > 0,
				"segmentLength must be greater than one");

		StringBuilder sb = new StringBuilder();
		long val = nextLong();
		int remainder = (separator != '\0') ? segmentLength - 1 : -1;
		for (int i = 0; i < digits; ++i) {
			int n = (int) (val % ID_BASE);
			sb.append(ID_CHARS.charAt(n));
			// Every 12 digits, regen our number since we're approaching
			// the limits of a 64-bit value.
			if (i % 12 == 11) {
				val = nextLong();
			} else {
				val /= ID_BASE;
			}
			if ((i % segmentLength == remainder) && (i < digits - 1)) {
				sb.append(separator);
			}
		}
		return sb.toString();
	}

	private static long nextLong() {
		long n;
		synchronized (idGenerator) {
			n = idGenerator.nextLong();
		}
		return Math.abs(n);
	}

	/**
	 * Compares two guids, ignoring case and any characters that are not guid
	 * digits.
	 * 
	 * @param guid1
	 * @param guid2
	 * @return a negative number if guid1 < guid2, 0 if the two guids are equal,
	 *         or a positive number if guid1 > guid2.
	 */
	public static int compare(String guid1, String guid2) {
		if (guid1 == null) {
			return (guid2 == null) ? 0 : -1;
		} else if (guid2 == null) {
			return 1;
		}

		int len1 = guid1.length();
		int len2 = guid2.length();
		for (int i = 0, j = 0;;) {
			char c1 = 0, c2 = 0;
			// Get next significant char in first guid.
			do {
				if (i == len1)
					break;
				c1 = Character.toLowerCase(guid1.charAt(i));
				++i;
			} while (FULL_CHARS_LOWER.indexOf(c1) == -1);
			// Get next significant char in second guid.
			do {
				if (j == len2)
					break;
				c2 = Character.toLowerCase(guid2.charAt(j));
				++j;
			} while (FULL_CHARS_LOWER.indexOf(c2) == -1);

			int n = c1 - c2;
			if (n != 0) {
				return n;
			}
			
			if (i == len1) {
				return (j == len2) ? 0 : -1;
			} else if (j == len2) {
				return 1;
			}
		}
	}

}
