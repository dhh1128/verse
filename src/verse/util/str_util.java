/**
 * $Id$
 *
 * Proprietary and confidential.
 * Copyright $Date::      $ Perfect Search Corporation.
 * All rights reserved.
 *
 * Author: Daniel Hardman
 * Created: Sep 1, 2009
 */
package verse.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import verse.dbc.precondition;
import verse.util.OSInfo.ShellStyle;

/**
 * Contains extra routines to facilitate working with strings.
 */
public class str_util {
	/**
	 * @return true if a String is null or empty.
	 */
	public static boolean is_null_or_empty(CharSequence s) {
		return s == null || s.length() == 0;
	}

	private static final Pattern BOOL_TRUE_PAT = Pattern.compile(
			"^\\s*(t(rue)?|y(es)?|on|checked|-[1-9]\\d*)\\s*$",
			Pattern.CASE_INSENSITIVE);

	/**
	 * A more permissive validate than {@link Boolean#parseBoolean(String)}; true comes
	 * from any non-zero number, "t(rue)?", "y(es)?", "on", "checked" (all text
	 * case-insensitive).
	 * 
	 * @param s
	 *            text to validate
	 * @return true if string matches anything known to represent
	 *         <code>true</code>.
	 * @throws NumberFormatException parseLong throws it
	 */
	public static boolean parseBooleanLenient(CharSequence s) throws NumberFormatException {
		if (!is_null_or_empty(s)) {
			char c1 = s.charAt(0);
			if (Character.isDigit(c1) && Long.parseLong(s.toString()) != 0)
				return true;
			Matcher m = BOOL_TRUE_PAT.matcher(s);
			return m.matches();
		}
		return false;
	}

	private static final String SQUEEZABLE = "\t\r\n ";

	/**
	 * Replace all runs of whitespace with a single space, and remove
	 * leading/trailing whitespace.
	 */
	public static String squeeze(CharSequence txt) {
		if (is_null_or_empty(txt))
			return txt == null ? null : txt.toString();

		int lettersTo = indexOfAny(txt, SQUEEZABLE);
		if (lettersTo == -1)
			return txt == null ? null : txt.toString();

		// Find where chars other than whitespace begin.
		int lettersFrom = 0;
		// If we started with whitespace, trim it.
		if (lettersTo == 0) {
			lettersFrom = indexOfAny(txt, SQUEEZABLE, 1, true);
			// If we didn't find anything except whitespace, return empty
			// string.
			if (lettersFrom == -1)
				return "";
			// Find where next whitespace begins.
			lettersTo = indexOfAny(txt, SQUEEZABLE, lettersFrom + 1);
			// This logic is repeated below, but we're saving an allocation of
			// a StringBuilder by short-circuiting it...
			if (lettersTo == -1)
				return txt.subSequence(lettersFrom, txt.length()).toString();
		}

		boolean first = true;
		StringBuilder sb = new StringBuilder();
		do {
			if (first)
				first = false;
			else
				sb.append(' ');
			try {
				sb.append(txt.subSequence(lettersFrom, lettersTo));
			} catch (StringIndexOutOfBoundsException ex) {
				System.out.println(ex.toString());
			}
			lettersFrom = indexOfAny(txt, SQUEEZABLE, lettersTo + 1, true);
			if (lettersFrom == -1) {
				break;
			}
			lettersTo = indexOfAny(txt, SQUEEZABLE, lettersFrom + 1);
			if (lettersTo == -1)
				lettersTo = txt.length();
		} while (true);
		return sb.toString();
	}

	/**
	 * Finds a specific character in a CharSequence. This method is mainly
	 * useful internally; if you know you have a String, for instance, just use
	 * its {@link String#indexOf(int)} method.
	 * 
	 * @param haystack
	 *            The text to search through.
	 * @param c
	 *            The character to search for.
	 * @return index of character, or -1 on failure.
	 */
	public static int indexOf(CharSequence haystack, char c) {
		for (int i = 0; i < haystack.length(); ++i) {
			if (c == haystack.charAt(i)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return the offset of the first char in <code>any</code> that appears in
	 *         <code>haystack</code>
	 */
	public static int indexOfAny(CharSequence haystack, CharSequence any) {
		return indexOfAny(haystack, any, 0, false);
	}

	/**
	 * @return the offset of the first char in <code>any</code> that appears in
	 *         <code>haystack</code> -- or, if <code>invert</code> is true,
	 *         reverses the logic to find the offset of the first char that is
	 *         NOT in <code>any</code>.
	 */
	public static int indexOfAny(CharSequence haystack, CharSequence any,
			boolean invert) {
		return indexOfAny(haystack, any, 0, invert);
	}

	/**
	 * @return the offset of the first char in <code>any</code> that appears in
	 *         <code>haystack</code>, beginning at <code>offset</code>
	 */
	public static int indexOfAny(CharSequence haystack, CharSequence any,
			int offset) {
		return indexOfAny(haystack, any, offset, false);
	}

	/**
	 * @return the offset of the first char in <code>any</code> that appears in
	 *         <code>haystack</code>, beginning at <code>offset</code> -- or, if
	 *         <code>invert</code> is true, reverses the logic to find the
	 *         offset of the first char that is NOT in <code>any</code>.
	 */
	public static int indexOfAny(CharSequence haystack, CharSequence any,
			int offset, boolean invert) {
		if (is_null_or_empty(haystack) || is_null_or_empty(any))
			return -1;
		int anyCount = any.length();
		for (int i = offset; i < haystack.length(); ++i) {
			char ch = haystack.charAt(i);
			boolean matchAny = false;
			for (int j = 0; j < anyCount; ++j) {
				if (ch == any.charAt(j)) {
					matchAny = true;
					break;
				}
			}
			if (matchAny != invert)
				return i;
		}
		return -1;
	}

	/**
	 * @return the offset of the last char in <code>any</code> that appears in
	 *         <code>haystack</code>
	 */
	public static int lastIndexOfAny(CharSequence haystack, CharSequence any) {
		return lastIndexOfAny(haystack, any, -1, false);
	}

	/**
	 * @return the offset of the last char in <code>any</code> that appears in
	 *         <code>haystack</code> -- or, if <code>invert</code> is true,
	 *         reverses the logic to find the offset of the last char that is
	 *         NOT in <code>any</code>.
	 */
	public static int lastIndexOfAny(CharSequence haystack, CharSequence any,
			boolean invert) {
		return lastIndexOfAny(haystack, any, -1, invert);
	}

	/**
	 * @return the offset of the last char in <code>any</code> that appears in
	 *         <code>haystack</code>, beginning at <code>offset</code>
	 */
	public static int lastIndexOfAny(CharSequence haystack, CharSequence any,
			int offset) {
		return lastIndexOfAny(haystack, any, offset, false);
	}

	/**
	 * @return the offset of the last char in <code>any</code> that appears in
	 *         <code>haystack</code>, beginning at <code>offset</code> and
	 *         walking backward -- or, if <code>invert</code> is true, reverses
	 *         the logic to find the offset of the last char that is NOT in
	 *         <code>any</code>.
	 */
	public static int lastIndexOfAny(CharSequence haystack, CharSequence any,
			int offset, boolean invert) {
		if (is_null_or_empty(haystack) || is_null_or_empty(any))
			return -1;
		if (offset < 0)
			offset = haystack.length() + offset;
		int anyCount = any.length();
		for (int i = offset; i >= 0; --i) {
			char ch = haystack.charAt(i);
			boolean matchAny = false;
			for (int j = 0; j < anyCount; ++j) {
				if (ch == any.charAt(j)) {
					matchAny = true;
					break;
				}
			}
			if (matchAny != invert)
				return i;
		}
		return -1;
	}

	/**
	 * Like {@link String#replaceAll(String, String)}, but allows a regex to be passed instead
	 * of compiling a new one for every invocation.
	 */
	public static String replaceAll(CharSequence haystack, Pattern searchFor,
			CharSequence replaceWith) {
		if (is_null_or_empty(haystack) || searchFor == null)
			return haystack == null ? null : haystack.toString();
		if (replaceWith == null)
			replaceWith = "";
		Matcher m = searchFor.matcher(haystack);
		if (!m.find())
			return haystack.toString();
		int to = 0;
		int from = 0;
		StringBuilder sb = new StringBuilder();
		do {
			to = m.start();
			if (to > from)
				sb.append(haystack.subSequence(from, to));
			if (replaceWith.length() > 0)
				sb.append(replaceWith);
			from = m.end();
		} while (m.find());
		to = haystack.length();
		if (to > from)
			sb.append(haystack.subSequence(from, to));
		return sb.toString();
	}

	/**
	 * @return # of occurrences of <code>needle</code> in <code>haystack</code>
	 */
	public static int count(CharSequence haystack, char needle) {
		return count(haystack, needle, 0);
	}

	/**
	 * @return # of occurrences of <code>needle</code> in <code>haystack</code>,
	 *         beginning at <code>offset</code>
	 */
	public static int count(CharSequence haystack, char needle, int offset) {
		int n = 0;
		for (int i = offset; i < haystack.length(); ++i) {
			if (haystack.charAt(i) == needle)
				++n;
		}
		return n;
	}

	/**
	 * @return # of non-overlapping occurrences of needle in haystack
	 */
	public static int count(CharSequence haystack, CharSequence needle) {
		return count(haystack, needle, false);
	}

	/**
	 * @param allowOverlaps
	 *            governs whether occurrences are required to be independent or
	 *            whether they can overlap.
	 * @return # of occurrences of needle in haystack
	 */
	public static int count(CharSequence haystack, CharSequence needle,
			boolean allowOverlaps) {
		int n = 0;
		char ch = needle.charAt(0);
		for (int i = 0; i < haystack.length() - needle.length(); ++i) {
			if (haystack.charAt(i) == ch) {
				boolean found = true;
				for (int j = 1; j < needle.length(); ++j) {
					if (haystack.charAt(i + j) != needle.charAt(j)) {
						found = false;
						break;
					}
				}
				if (found) {
					++n;
					if (!allowOverlaps)
						i += needle.length() - 1;
				}
			}
		}
		return n;
	}

	/**
	 * @return # of occurrences of any char in <code>any</code>, in
	 *         <code>haystack</code>
	 */
	public static int countAny(CharSequence haystack, CharSequence any) {
		return countAny(haystack, any, 0);
	}

	/**
	 * @return # of occurrences of any char in <code>any</code>, in
	 *         <code>haystack</code>, beginning at <code>offset</code>.
	 */
	public static int countAny(CharSequence haystack, CharSequence any,
			int offset) {
		int n = 0;
		while ((offset = indexOfAny(haystack, any, offset)) != -1) {
			++offset;
			++n;
		}
		return n;
	}

	/**
	 * @return a String composed of elements from the specified array, separated
	 *         by the specified delimiter.
	 */
	public static String join(CharSequence delim, Object[] items) {
		if (items.length == 0) 
		{
			return "";
		} 
		else if (items.length == 1) 
		{
			return items[0] != null ? items[0].toString() : "";
		}
		
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object o : items) {
			if (first)
				first = false;
			else
				sb.append(delim);
			if (o != null) {
				sb.append(o.toString());
			}
		}
		return sb.toString();
	}

	/**
	 * @return a String composed of elements from the specified collection,
	 *         separated by the specified delimiter.
	 */
	public static String join(CharSequence delim, Iterable<?> items) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object o : items) {
			if (first)
				first = false;
			else
				sb.append(delim);
			if (o != null) {
				sb.append(o.toString());
			}
		}
		return sb.toString();
	}

	/**
	 * Capitalizes the first character of a sequence.
	 * 
	 * @param text
	 */
	public static String capitalize(CharSequence text) {
		if (is_null_or_empty(text))
			return text == null ? null : "";
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toUpperCase(text.charAt(0)));
		sb.append(text.subSequence(1, text.length()));
		return sb.toString();
	}

	/**
	 * Capitalizes every word in a sequence.
	 * 
	 * @param words
	 * @return A string with every word capitalized.
	 */
	public static String toTitleCase(String words) {
		return toTitleCase(words, " ", true, true);
	}

	/**
	 * Converts a sequence of words or tokens to a camelCase identifier.
	 * 
	 * @param words
	 *            The sequence to convert; must contain embedded delimiters of
	 *            some sort.
	 * @param wordDelimChars
	 *            The chars that delimit the tokens.
	 * @return A stringInCamelCase.
	 */
	public static String toCamelCase(String words, String wordDelimChars) {
		precondition.checkAndExplain(!is_null_or_empty(wordDelimChars),
				"wordDelimChars cannot be null/empty; it's needed to "
						+ "know how to segment words");
		return toTitleCase(words, wordDelimChars, false, false);
	}

	/**
	 * Capitalizes every word in a sequence.
	 * 
	 * @param words
	 *            The words to capitalize
	 * @param wordDelimChars
	 *            The chars that separate words; typically a space
	 * @param retainDelims
	 *            Should the wordDelims chars be retained in the final output?
	 * @param capFirst
	 *            Should the first word be capitalized? If no, a camel case
	 *            effect is achieved.
	 * @return A string with every word capitalized.
	 */
	public static String toTitleCase(String words, String wordDelimChars,
			boolean retainDelims, boolean capFirst) {
		if (is_null_or_empty(words))
			return words == null ? null : "";
		StringBuilder sb = new StringBuilder();
		boolean capitalize = capFirst;
		boolean foundNonDelim = false;
		for (int i = 0; i < words.length(); ++i) {
			char c = words.charAt(i);
			if (wordDelimChars.indexOf(c) != -1) {
				capitalize = (foundNonDelim || capFirst);
				if (retainDelims) {
					sb.append(c);
				}
			} else {
				foundNonDelim = true;
				if (capitalize) {
					sb.append(Character.toUpperCase(c));
					capitalize = false;
				} else {
					sb.append(c);
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Pad a string with spaces on the right.
	 * 
	 * @param txt
	 *            String to pad. Null treated like empty.
	 * @param n
	 *            How wide to make the string.
	 * @return padded string.
	 */
	public static String padRight(String txt, int n) {
		precondition.checkAndExplain(n >= 0, "pad width cannot be negative");
		if (txt == null) {
			txt = "";
		}
		return String.format("%1$-" + n + "s", txt);
	}

	/**
	 * Pad a string on the right.
	 * 
	 * @param txt
	 *            String to pad. Null treated like empty.
	 * @param n
	 *            How wide to make the string.
	 * @param with
	 *            char to pad with.
	 * @return padded string.
	 */
	public static String padRight(String txt, int n, char with) {
		precondition.checkAndExplain(n >= 0, "pad width cannot be negative");
		if (txt == null) {
			txt = "";
		}
		if (txt.length() < n) {
			char[] extra = new char[n - txt.length()];
			Arrays.fill(extra, with);
			txt = txt + new String(extra);
		}
		return txt;
	}

	/**
	 * Pad a string on the left with spaces.
	 * 
	 * @param txt
	 *            String to pad. Null treated like empty.
	 * @param n
	 *            How wide to make the string.
	 * @return padded string.
	 */
	public static String padLeft(String txt, int n) {
		precondition.checkAndExplain(n >= 0, "pad width cannot be negative");
		if (txt == null) {
			txt = "";
		}
		return String.format("%1$#" + n + "s", txt);
	}

	/**
	 * Pad a string on the left.
	 * 
	 * @param txt
	 *            String to pad. Null treated like empty.
	 * @param n
	 *            How wide to make the string.
	 * @param with
	 *            char to pad with.
	 * @return padded string.
	 */
	public static String padLeft(String txt, int n, char with) {
		precondition.checkAndExplain(n >= 0, "pad width cannot be negative");
		if (txt == null) {
			txt = "";
		}
		if (txt.length() < n) {
			char[] extra = new char[n - txt.length()];
			Arrays.fill(extra, with);
			txt = new String(extra) + txt;
		}
		return txt;
	}

	/**
	 * The default line delimiter on the current platform (e.g., \n or \r\n).
	 */
	public static final String PLATFORM_DEFAULT_LINE_DELIM = String
			.format("%n");

	/**
	 * Wrap a string at the specified width, using
	 * {@link #PLATFORM_DEFAULT_LINE_DELIM the platform's default line
	 * separator} as a delimiter.
	 * 
	 * @param txt
	 * @param width
	 * @return wrapped string
	 */
	public static String wrap(String txt, int width) {
		return wrap(txt, width, PLATFORM_DEFAULT_LINE_DELIM);
	}

	/**
	 * @param ch
	 *            character to test
	 * @return <code>true</code> if character indicates a point where a string
	 *         could be wrapped.
	 */
	public static boolean isWrapChar(char ch) {
		return Character.isSpaceChar(ch) || ch == '\r' || ch == '\n';
	}

	/**
	 * Wrapping text is moderately complex. Initially the algorithm was
	 * implemented as a single function, but it got difficult to debug and
	 * understand, so it was refactored into an object. Now all state variables
	 * are visible to object methods without passing them around in fat
	 * parameter lists, and we can enforce invariant conditions.
	 */
	private static class Wrapper {
		public static String process(String txt, int width, String eol) {
			Wrapper w = new Wrapper(txt, width, eol);
			while (w.nextLine()) {
				w.wrapLine();
			}
			return w.sb.toString();
		}

		private Wrapper(String txt, int width, String eol) {
			this.txt = txt;
			this.width = width;
			this.sb = new StringBuilder();
			this.eol = eol;
			this.line = new LineDescrip();
		}

		private static class LineDescrip {
			int begin;
			// Index of first char not part of current line. May point beyond
			// end of string.
			int end;
			// Index of first char of next line. May point beyond end of string.
			int next = 0;
			boolean endsWithEol = true;
			CharSequence indent;
		}

		private final String txt;
		private final StringBuilder sb;
		private final int width;
		private final String eol;
		private LineDescrip line;

		private void wrapLine() {
			int len = line.end - line.begin;
			if (len <= width) {
				if (len > 0) {
					sb.append(txt.subSequence(line.begin, line.end));
				}
			} else {
				int i;
				for (i = line.begin; i < line.end
						&& Character.isSpaceChar(txt.charAt(i)); ++i) {
				}
				line.indent = (i > line.begin) ? txt.subSequence(line.begin, i)
						: null;
				if (line.indent != null) {
					sb.append(line.indent);
				}
				line.begin = i;
				while (softWrap())
					;
				// If we have anything left, append it here.
				if (line.end - line.begin > 0) {
					sb.append(txt.subSequence(line.begin, line.end));
				}
			}
			if (line.endsWithEol) {
				sb.append(eol);
			}
		}

		/**
		 * When called, line.begin should be pointing at a place where we can
		 * start looking for another soft wrap point. This offset may or may not
		 * contain a whitespace char. The same condition applies on return as
		 * long as the function returns true. When the function returns false,
		 * line.begin must point at a non-whitespace char or past end of line.
		 */
		private boolean softWrap() {
			int i;
			for (i = line.begin; i < line.end
					&& Character.isSpaceChar(txt.charAt(i)); ++i) {
			}
			line.begin = i;
			int maxJ = i + width
					- (line.indent == null ? 0 : line.indent.length());
			// If what we have left on this line will fit in the required width,
			// then no further soft wraps are needed and we are done.
			if (maxJ >= line.end) {
				return false;
			}
			// Okay, we need to look for a wrap point. First try searching
			// from the max offset backward; this is likely to be most
			// efficient.
			// It is possible that we won't find anything if we have an
			// unbroken run of non-space chars that's longer than the max width.
			int j;
			boolean found = false;
			for (j = maxJ; j >= i; --j) {
				if (Character.isSpaceChar(txt.charAt(j))) {
					found = true;
					break;
				}
			}
			// If we didn't find anything, search the other direction.
			if (!found) {
				for (j = maxJ + 1; j < line.end; ++j) {
					if (Character.isSpaceChar(txt.charAt(j))) {
						found = true;
						break;
					}
				}
			}
			// If we still didn't find anything, then return false. This huge
			// line will be written unmodified.
			if (!found) {
				return false;
			}
			sb.append(txt.subSequence(line.begin, j));
			sb.append(eol);
			// Append indent, if any.
			if (line.indent != null) {
				sb.append(line.indent);
			}
			line.begin = j + 1;
			return true;
		}

		/**
		 * When called, line.next always points to the first char of the next
		 * line, or to an index >= txt.length() to end. When we leave, we either
		 * return false (no more lines), or line.begin, line.end, line.next. and
		 * line.endsWithEol are set correctly.
		 */
		private boolean nextLine() {
			if (line.next >= txt.length()) {
				return false;
			}
			line.begin = line.next;
			char ch = txt.charAt(line.begin);
			if (ch == '\r') {
				boolean nextIsLF = (line.begin + 1 < txt.length() && txt
						.charAt(line.begin + 1) == '\n');
				line.next = line.begin + 1 + (nextIsLF ? 1 : 0);
			} else if (ch == '\n') {
				line.next = line.begin + 1;
			} else {
				line.endsWithEol = false;
				int i;
				for (i = line.begin; i < txt.length(); ++i) {
					ch = txt.charAt(i);
					if (ch == '\r') {
						++i;
						if (i < txt.length() && txt.charAt(i) == '\n') {
							++i;
						}
						line.endsWithEol = true;
						break;
					} else if (ch == '\n') {
						++i;
						line.endsWithEol = true;
						break;
					}
				}
				line.next = i;
			}
			// Now right trim line by looking for last char on line that's not
			// a wrap char.
			int i;
			for (i = line.next - 1; i >= line.begin; --i) {
				if (!isWrapChar(txt.charAt(i))) {
					break;
				}
			}
			line.end = i + 1;
			return true;
		}
	}

	/**
	 * Wrap a string at the specified width. Lines that exceed the specified
	 * length are wrapped with whatever whitespace they began with, in addition
	 * to the supplied eol marker. This allows indents to be preserved.
	 * 
	 * @param txt
	 *            Text to wrap.
	 * @param width
	 *            Wrap at what width?
	 * @param eol
	 *            String to place at the end of every line. All line endings are
	 *            normalized to this value, although they are recognized in
	 *            whatever form the text contains.
	 * @return the wrapped string.
	 */
	public static String wrap(String txt, int width, String eol) {
		precondition.checkAndExplain(width >= 1, "wrap width must be >= 1");
		precondition.checkNotNull(eol, "eol");
		if (is_null_or_empty(txt)) {
			return txt;
		}
		return Wrapper.process(txt, width, eol);
	}

    /**
     * Count how many line breaks characters or pairs are in the string. All three line
     * break conventions (Windows CR+LF, *nix LF, and old Mac CR) are handled transparently.
     * 
     * Text editors and scanning tools like grep, awk, sed, gcc, and so forth always
     * consider files to have a final line that is incomplete. This means that an empty
     * file has 0 complete lines, even though the cursor in a text editor will show that
     * you're on line 1. When reading a text file, the line count = 1 + complete lines,
     * so if the file contains one CR+LF, then "wc -l" will report 2. Likewise, if you seek
     * to the end of a file in a text editor, the line number you see = 1 + complete lines.
     *
     * @param txt String to scan.
     * @return
     */
    public static int count_complete_lines(CharSequence txt) {
        int lc = 0;
        if (txt != null) {
            boolean skipNext = false;
            for (int i = 0; i < txt.length(); ++i) {
                char c = txt.charAt(i);
                if (c == '\r') {
                    skipNext = true;
                    lc += 1;
                } else if (c == '\n') {
                    if (skipNext) {
                        skipNext = false;
                    } else {
                        lc += 1;
                    }
                } else {
                    skipNext = false;
                }
            }
        }
        return lc;
    }
	
	/**
	 * Convert a string of lines to an array of strings.
	 * 
	 * @param lines
	 * 
	 * @return An array of strings, one per line. Lines are trimmed, and empty
	 *         lines are discarded. If lines is empty or null, return value is
	 *         null as well.
	 */
	public static String[] getArrayFromLines(String lines) {
		return getArrayFromLines(lines, true, false);
	}

	/**
	 * Convert a string of lines to an array of strings.
	 * 
	 * @param lines
	 *            A multiline string.
	 * @param trim
	 *            Should items be trimmed? If false, lines may end with a \r or
	 *            similar delimiters.
	 * @param keepEmpty
	 *            Should lines that are empty be retained?
	 * 
	 * @return An array of strings, one per line. Lines are trimmed. If lines is
	 *         empty or null, return value is null as well.
	 */
	public static String[] getArrayFromLines(String lines, boolean trim,
			boolean keepEmpty) {
		int keepCount = 0;
		String[] items = null;
		if (!is_null_or_empty(lines)) {
			items = lines.split("\n");
			if (trim || !keepEmpty) {
				for (int i = 0; i < items.length; ++i) {
					String line = trim ? items[i].trim() : items[i];
					if (keepEmpty || !line.isEmpty()) {
						items[keepCount++] = line;
					}
				}
				if (keepCount < items.length) {
					if (keepCount == 0) {
						return null;
					}
					String[] kept = new String[keepCount];
					System.arraycopy(items, 0, kept, 0, keepCount);
					items = kept;
				}
			}
		}
		return items;
	}
	
	/**
	 * Convert a string of lines to a list of strings.
	 * 
	 * @param lines
	 * 
	 * @return A list of strings, one per line. Lines are trimmed, and empty
	 *         lines are discarded. If lines is empty or null, return value is
	 *         null as well.
	 */
	public static List<String> getListFromLines(String lines) {
		return getListFromLines(lines, true, false);
	}

	/**
	 * Convert a string of lines to a list of strings.
	 * 
	 * @param lines
	 *            A multiline string.
	 * @param trim
	 *            Should items be trimmed? If false, lines may end with a \r or
	 *            similar delimiters.
	 * @param keepEmpty
	 *            Should lines that are empty be retained?
	 * 
	 * @return A list of strings, one per line. Lines are trimmed. If lines is
	 *         empty or null, return value is null as well.
	 */
	public static List<String> getListFromLines(String lines, boolean trim, boolean keepEmpty) {
		String[] items = getArrayFromLines(lines, trim, keepEmpty);
		return items == null ? null : Arrays.asList(items);
	}

	/**
	 * Convert an array of objects to a multi-line string.
	 * 
	 * @param items Any array of objects -- String[], URI[], etc.
	 * @return A string where each item in the original array is delimited by a new line (\n).
	 */
	public static String getLinesFromArray(Object[] items) {
		return items == null || items.length == 0 ? null : join("\n", items);
	}
	
	/**
	 * Convert a list of objects to a multi-line string.
	 * 
	 * @param items Any list of objects -- List<String>, ArrayList<URI>, etc.
	 * @return A string where each item in the original list is delimited by a new line (\n).
	 */
	public static String getLinesFromList(List<?> items) {
		return items == null || items.isEmpty() ? null : join("\n", items);
	}

	/**
	 * Given a range of text delimited by either a double or single quote, find
	 * the close quote, taking into account the possibility of escape sequences
	 * that might embed a literal close quote char. Quoted strings are assumed
	 * to occupy a single line, so if a CR or LF is encountered before the
	 * terminator, the function returns -1.
	 * 
	 * @param txt
	 *            The string containing quoted text.
	 * @param beginOffset
	 *            The offset of the begin quote char.
	 * @return The offset of the end quote char, or -1 if not found.
	 */
	public static int indexOfCloseQuote(CharSequence txt, int beginOffset) {
		char ch = txt.charAt(beginOffset);
		for (int i = beginOffset + 1; i < txt.length(); ++i) {
			char c = txt.charAt(i);
			if (c == '\\') {
				++i;
			} else if (c == ch) {
				return i;
			} else if (c == '\r' || c == '\n') {
				return -1;
			}
		}
		return -1;
	}
	
	/**
	 * Given a range of text delimited by a grouping char (paren, square bracket, 
	 * angle bracket, guillemet, curly brace, open curly quote, inverted question 
	 * mark, etc.), find the corresponding close group char. Quoted strings inside
	 * the groupings (which might contain spurious close group or open group chars) 
	 * are not taken into account.
	 * 
	 * @param txt
	 *            The string containing quoted text.
	 * @param beginOffset
	 *            The offset of the begin group char.
	 * @return The offset of the end group char, or -1 if not found.
	 */
	public static int indexOfCloseGroup(CharSequence txt, int beginOffset) {
		return indexOfCloseGroup(txt, beginOffset, false, false);
	}
	
	/**
	 * Given a range of text delimited by a grouping char (paren, square bracket, 
	 * angle bracket, guillemet, curly brace, open curly quote, inverted question 
	 * mark, etc.), find the corresponding close group char, optionally taking 
	 * into account the possibility of quoted strings inside the text.
	 * 
	 * @param txt
	 *            The string containing quoted text.
	 * @param beginOffset
	 *            The offset of the begin group char.
	 * @param skipDoubleQuotedStrs
	 * 			  Ignore open and close chars insided double-quoted strings.
	 * @param skipSingleQuotedStrs
	 * 			  Ignore open and close chars inside single-quoted strings.
	 * @return The offset of the end group char, or -1 if not found.
	 */
	public static int indexOfCloseGroup(CharSequence txt, int beginOffset, 
			boolean skipDoubleQuotedStrs, boolean skipSingleQuotedStrs) {
		char closer;
		char ch = txt.charAt(beginOffset);
		switch (ch) {
		case '(': closer = ')'; break;
		case '[': closer = ']';	break;
		case '{': closer = '}';	break;
		case '<': closer = '>'; break;
		case '\u00AB': closer = '\u00BB'; break; // guillement
		case '\u2018': closer = '\u2019'; break; // single curly quote
		case '\u201a': closer = '\u2019'; break; // low left single curly quote; ends with high right single curly quote
		case '\u201c': closer = '\u201d'; break; // double curly quote
		case '\u201e': closer = '\u201d'; break; // low left double curly quote; ends with high double curly quote
		case '\u00a1': closer = '!'; break; // inverted exclamation point
		case '\u00bf': closer = '?'; break; // inverted question mark
		case '\u2039': closer = '\u203a'; break; // single angle quote
		case '\u300c': closer = '\u300d'; break; // cjk left corner bracket
		case '\u300e': closer = '\u300f'; break; // cjk left white corner bracket
		case '\uff08': closer = '\uff09'; break; // cjk fullwidth parens
		case '\uff1c': closer = '\uff1e'; break; // cjk fullwidth angle brackets
		case '\uff3b': closer = '\uff3d'; break; // cjk fullwidth square brackets
		case '\uff5b': closer = '\uff5d'; break; // cjk fullwidth curly braces
		case '\uff62': closer = '\uff63'; break; // cjk fullwidth left corner bracket
		case '\uff5f': closer = '\uff60'; break; // cjk fullwidth double parens
		default:
			throw new IllegalArgumentException(String.format("Char '%s' at offset %d is not a supported grouping character.", Character.toString(ch), beginOffset));
		}
		int openCount = 1;
		for (int i = beginOffset + 1; i < txt.length(); ++i) {
			char c = txt.charAt(i);
			if (c == closer) {
				--openCount;
				if (openCount == 0) {
					return i;
				}
			} else if (c == ch) {
				++openCount;
			} else {
				boolean skip = (c == '"') ? skipDoubleQuotedStrs : (c == '\'') ? skipSingleQuotedStrs : false;
				if (skip) {
					i = indexOfCloseQuote(txt, i);
					if (i == -1) {
						return i;
					}
				}
			}
		}
		return -1;		
	}
	
	/**
	 * Given a multiline string where each line represents a separate regex,
	 * build a {@link Pattern}[].
	 * 
	 * @param lines A \n-delimited set of lines where each line is a {@link Pattern regex}.
	 * 		Lines are automatically trimmed during the conversion, and blank
	 * 		lines are disregarded.
	 * @return {@link Pattern}[] or null if no patterns were found
	 */
	public static Pattern[] getRegexesFromLines(String lines) {
		return getRegexesFromLines(lines,false);
	}
	
	/**
	 * Given a multiline string where each line represents a separate regex,
	 * build a {@link Pattern}[].
	 * 
	 * @param lines A \n-delimited set of lines where each line is a {@link Pattern regex}.
	 * 		Lines are automatically trimmed during the conversion, and blank
	 * 		lines are disregarded.
	 * @param ifCaseInsensitive set true if case-insensitive.
	 * @return {@link Pattern}[] or null if no patterns were found
	 */
	public static Pattern[] getRegexesFromLines(String lines, boolean ifCaseInsensitive) {
		Pattern[] patterns = null;
		if (lines != null) {
			String[] pats = getArrayFromLines(lines);
			if (pats != null && pats.length > 0) {
				patterns = new Pattern[pats.length];
				int i = 0;
				for (String item : pats) {
					Pattern pat = (ifCaseInsensitive)
								  ? Pattern.compile(item, Pattern.CASE_INSENSITIVE) 
								  : Pattern.compile(item); 
					patterns[i++] = pat;
				}
			}
		}
		return patterns;
	}
	
	/**
	 * Given a Pattern[], attempt to match the specified string against each
	 * element until a match is found.
	 * 
	 * @param patterns
	 * @return Matcher on success, or null if no match was found.
	 */
	public static Matcher matchAny(Pattern[] patterns, String potentialMatch) 
	{
		return matchAny(Arrays.asList(patterns), potentialMatch);
	}
	
	/**
	 * Given a Pattern[], attempt to match the specified string against each
	 * element until a match is found.
	 * 
	 * @param patterns
	 * @return Matcher on success, or null if no match was found.
	 */
	public static Matcher matchAny(Collection<Pattern> patterns, String potentialMatch) 
	{
		Matcher m = null;
		if (patterns != null) {
			for (Pattern pattern : patterns) {
				m = pattern.matcher(potentialMatch);
				if (m.matches()) {
					return m;
				}
			}
		}
		return null;
	}
	
	private static final Pattern SIZE_PATTERN = Pattern.compile("^(\\d+)\\s*([KMGTPkmgtp][Bb]?)?$");

	// TODO: add support for fractional units (e.g., "10.3GB").
	/**
	 * Convert text size specifier to the long size value in bytes.
	 * The specifier is the number that can be suffixed by K, M, G, T, or P
	 * meaning kilo, mega, giga, tera, and petabytes.
	 */
	public static long parseSize(String str) {
		if (str == null) {
			return 0L;
		}
		str = str.trim();
		Matcher m = SIZE_PATTERN.matcher(str);
		if (!m.matches()) {
			throw new IllegalArgumentException("Illegal size specifier. " +
					"Specify digital size with optional size suffix (K, M, G, T, or P).");
		}
		long l = Long.parseLong(m.group(1));
		String units = m.group(2);
		if (units != null) {
    		char c = Character.toUpperCase(m.group(2).charAt(0));
    		switch (c) {
    		case 'P': l *= 1024L;
            case 'T': l *= 1024L;
    		case 'G': l *= 1024L;
    		case 'M': l *= 1024L;
    		case 'K': l *= 1024L;
    		}
		}
		return l;
	}
	
	public final static String[] SIZE_UNITS = { "B", "KB", "MB", "GB", "TB", "PB" };
	
	private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));
	
	/**
	 * Converts a number of bytes into the largest whole unit with it's abbreviation.
	 * This conversion is approximate but will create a more friendly output than bytes.
	 */
	public static String formatSize(long bytes) 
	{
		double rtn = bytes;
		
		for(String unit : SIZE_UNITS)
		{
			if(rtn < 1024L)
			{
				return SIZE_FORMAT.format(rtn) + " " + unit;
			}
			else
			{
				rtn = rtn / 1024;
			}
		}
		
		return SIZE_FORMAT.format(rtn) + " " + SIZE_UNITS[SIZE_UNITS.length-1];
	}
	
	/**
	 * Converts a number of bytes into the unit designated.
	 * This conversion is approximate but will create a more friendly output than bytes.
	 */
	public static String formatSize(long bytes, String unit)
	{
		if(unit == null)
		{
			return String.valueOf(bytes) + " " + unit;
		}
		
		unit = unit.toUpperCase().trim();
		
		double rtn = bytes;
		
		for(String u : SIZE_UNITS)
		{
			if(unit.equals(u))
			{
				return SIZE_FORMAT.format(rtn) + " " + unit;
			}
			else
			{
				rtn = rtn / 1024;
			}
		}
		
		return SIZE_FORMAT.format(rtn) + " " + SIZE_UNITS[SIZE_UNITS.length-1];
	}
	
	/**
	 * @return true if strings are equal. Either string can be null;
	 * two null strings are considered equal, but null strings do
	 * not equal empty strings.
	 */
	public static boolean safeEquals(String s1, String s2) {
		if (s1 == null) {
			return s2 == null;
		} else if (s2 == null) {
			return false;
		} else {
			return s1.equals(s2);
		}
	}
	
	/**
	 * Trim characters from the beginning and end of a string
	 * @param txt A String containing characters that should be trimmed
	 * @return The trimmed String
	 */
	public static String trim(String txt, String charlist)
	{
		int leftIndex = 0;
		int rightIndex = txt.length()-1;
		
		int i;
		
		for(i = leftIndex; i < txt.length(); i++)
		{
			if(charlist.contains(String.valueOf(txt.charAt(i))))
			{
				leftIndex = i;
				continue;
			}
			leftIndex = i;
			break;
		}
		
		for(i = rightIndex; i >= 0; i--)
		{
			if(charlist.contains(String.valueOf(txt.charAt(i))))
			{
				rightIndex = i;
				continue;
			}
			rightIndex = i;
			break;
		}
		
		
		return (leftIndex > rightIndex) ? "" : txt.substring(leftIndex, rightIndex+1);
	}
	
	/**
	 * Escapes basic('&', '<', '>', '"') XML entities in the inputString
	 * 
	 * @param inputString the string that is scanned and modified
	 * @return A string with escaped the XML entities
	 */
	public static String escapeEntities(String inputString) 
	{
		if(is_null_or_empty(inputString))
		{
			return "";
		}
		
		StringBuilder rtn = new StringBuilder();
		
		int len = inputString.length();
		for (int i = 0; i < len; ++i) 
		{
			char ch = inputString.charAt(i);
			switch (ch) 
			{
				case '&':
					rtn.append("&amp;");
					break;
				case '<':
					rtn.append("&lt;");
					break;
				case '"':
					rtn.append("&quot;");
					break;
				case '>':
					rtn.append("&gt;");
					break;
				default:
					rtn.append(ch);
					break;
			}		
		}
		
		return rtn.toString();
	}
	
	/**
	 * Removes n char from the right side of the String
	 * 
	 * @param inputString the string that is scanned and modified
	 */
	public static String trimRight(String inputString, int num) 
	{
		if(inputString.length() - num <= 0)
		{
			return "";
		}
		
		return inputString.substring(0, inputString.length()-num);
	}
	
	/**
	 * Removes n char from the left side of the String
	 * 
	 * @param inputString the string that is scanned and modified
	 */
	public static String trimLeft(String inputString, int num) 
	{
		if(inputString.length() - num <= 0)
		{
			return "";
		}
		
		return inputString.substring(num, inputString.length());
	}
}
