/* Copyright (c) 2006 LibJ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * You should have received a copy of The MIT License (MIT) along with this
 * program. If not, see <http://opensource.org/licenses/MIT/>.
 */

package org.libj.lang;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Utility functions that provide common operations pertaining to {@link String}
 * and {@link StringBuilder}.
 */
public final class Strings {
  private static final char[] alphaNumeric = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
  private static final SecureRandom secureRandom = new SecureRandom();

  private static String getRandom(final SecureRandom secureRandom, final int length, final int start, final int len) {
    if (length == 0)
      return "";

    if (length < 0)
      throw new IllegalArgumentException("Length must be non-negative: " + length);

    final char[] array = new char[length];
    for (int i = 0; i < length; ++i)
      array[i] = alphaNumeric[start + secureRandom.nextInt(len)];

    return new String(array);
  }

  /**
   * Returns a randomly constructed alphanumeric string of the specified length.
   *
   * @param secureRandom The {@link SecureRandom} instance for generation of
   *          random values.
   * @param len The length of the string to construct.
   * @return A randomly constructed alphanumeric string of the specified length.
   * @throws IllegalArgumentException If {@code len} is negative.
   * @throws NullPointerException If {@code secureRandom} is null.
   */
  public static String getRandomAlphaNumeric(final SecureRandom secureRandom, final int len) {
    return getRandom(secureRandom, len, 0, alphaNumeric.length);
  }

  /**
   * Returns a randomly constructed alphanumeric string of the specified length.
   * <p>
   * This method uses a static {@link SecureRandom} instance for generation of
   * random values.
   *
   * @param len The length of the string to construct.
   * @return A randomly constructed alphanumeric string of the specified length.
   * @throws IllegalArgumentException If {@code len} is negative.
   */
  public static String getRandomAlphaNumeric(final int len) {
    return getRandom(secureRandom, len, 0, alphaNumeric.length);
  }

  /**
   * Returns a randomly constructed alpha string of the specified length.
   *
   * @param secureRandom The {@link SecureRandom} instance for generation of
   *          random values.
   * @param len The length of the string to construct.
   * @return A randomly constructed alpha string of the specified length.
   * @throws IllegalArgumentException If {@code len} is negative.
   * @throws NullPointerException If {@code secureRandom} is null.
   */
  public static String getRandomAlpha(final SecureRandom secureRandom, final int len) {
    return getRandom(secureRandom, len, 0, alphaNumeric.length - 10);
  }

  /**
   * Returns a randomly constructed alpha string of the specified length.
   * <p>
   * This method uses a static {@link SecureRandom} instance for generation of
   * random values.
   *
   * @param len The length of the string to construct.
   * @return A randomly constructed alpha string of the specified length.
   * @throws IllegalArgumentException If {@code len} is negative.
   */
  public static String getRandomAlpha(final int len) {
    return getRandom(secureRandom, len, 0, alphaNumeric.length - 10);
  }

  /**
   * Returns a randomly constructed numeric string of the specified length.
   *
   * @param secureRandom The {@link SecureRandom} instance for generation of
   *          random values.
   * @param len The length of the string to construct.
   * @return A randomly constructed numeric string of the specified length.
   * @throws IllegalArgumentException If {@code len} is negative.
   * @throws NullPointerException If {@code secureRandom} is null.
   */
  public static String getRandomNumeric(final SecureRandom secureRandom, final int len) {
    return getRandom(secureRandom, len, alphaNumeric.length - 10, 10);
  }

  /**
   * Returns a randomly constructed numeric string of the specified length.
   * <p>
   * This method uses a static {@link SecureRandom} instance for generation of
   * random values.
   *
   * @param len The length of the string to construct.
   * @return A randomly constructed numeric string of the specified length.
   * @throws IllegalArgumentException If {@code len} is negative.
   */
  public static String getRandomNumeric(final int len) {
    return getRandom(secureRandom, len, alphaNumeric.length - 10, 10);
  }

  private static boolean interpolateShallow(final StringBuilder text, final Map<String,String> properties, final String open, final String close) {
    boolean changed = false;
    for (int start = text.length() - close.length() - 1; (start = text.lastIndexOf(open, start - 1)) > -1;) {
      final int end = text.indexOf(close, start + open.length());
      if (end < start)
        continue;

      final String key = text.substring(start + open.length(), end);
      final String value = properties.get(key);
      if (value != null) {
        text.replace(start, end + close.length(), value);
        changed = true;
      }
    }

    return changed;
  }

  private static String interpolateDeep(final StringBuilder text, final Map<String,String> properties, final String prefix, final String suffix) {
    final int max = properties.size() * properties.size();
    for (int i = 0; interpolateShallow(text, properties, prefix, suffix); ++i)
      if (i == max)
        throw new IllegalArgumentException("Loop detected");

    return text.toString();
  }

  /**
   * Interpolates all the <i>value</i> strings in the specified {@link Map} by
   * matching {@code prefix + value + suffix}, where <i>value</i> is a
   * <i>key</i> in the {@link Map}, and replacing it with the value from the
   * {@link Map}.
   * <p>
   * This performance of this algorithm is {@code O(n^2)} by nature. If the
   * specified {@link Map} has {@code key=value} entries that result in a loop,
   * this method will throw a {@link IllegalArgumentException}.
   * <p>
   * <blockquote>
   * <b>Example:</b>
   * <p>
   * <table>
   * <caption>Input, with prefix=<code>"${"</code>, and suffix=<code>"}"</code></caption>
   * <tr><td><b>Key</b></td><td><b>Value</b></td></tr>
   * <tr><td>title</td><td>The ${subject} jumps over the ${object}</td></tr>
   * <tr><td>subject</td><td>${adj1} fox</td></tr>
   * <tr><td>object</td><td>${adj2} dog</td></tr>
   * <tr><td>adj1</td><td>quick brown</td></tr>
   * <tr><td>adj2</td><td>lazy</td></tr>
   * </table>
   * <p>
   * <table>
   * <caption>Output</caption>
   * <tr><td><b>Key</b></td><td><b>Value</b></td></tr>
   * <tr><td>title</td><td>The quick brown fox jumps over the lazy dog</td></tr>
   * <tr><td>subject</td><td>quick brown fox</td></tr>
   * <tr><td>object</td><td>lazy dog</td></tr>
   * <tr><td>adj1</td><td>quick brown</td></tr>
   * <tr><td>adj2</td><td>lazy</td></tr>
   * </table>
   * </blockquote>
   *
   * @param properties The map to interpolate.
   * @param prefix String prefixing the key name.
   * @param suffix String suffixing the key name.
   * @return The specified map, with its values interpolated.
   * @see #interpolate(String,Map,String,String)
   * @throws IllegalArgumentException If the specified {@code properties} has
   *           {@code key=value} entries that result in a loop.
   * @throws NullPointerException If {@code properties}, {@code prefix}, or
   *           {@code suffix} is null.
   */
  public static Map<String,String> interpolate(final Map<String,String> properties, final String prefix, final String suffix) {
    Objects.requireNonNull(properties);
    Objects.requireNonNull(prefix);
    Objects.requireNonNull(suffix);
    StringBuilder builder = null;
    for (final Map.Entry<String,String> entry : properties.entrySet()) {
      final String value = entry.getValue();
      if (value != null) {
        if (builder == null)
          builder = new StringBuilder(value.length());
        else
          builder.setLength(0);

        builder.append(value);
        entry.setValue(interpolateDeep(builder, properties, prefix, suffix));
      }
    }

    return properties;
  }

  /**
   * Interpolates the specified string by matching {@code prefix + key + suffix}
   * substring and replacing it with the <i>value</i> of the {@code key=value}
   * mapping in the properties {@link Map}.
   * <p>
   * <blockquote>
   * <b>Example:</b>
   * <p>
   * <b>Input</b>: text=<code>The ${subject} jumps over the ${object}</code>,
   * prefix=<code>"${"</code>, suffix=<code>"}"</code>
   * <p>
   * <table>
   * <caption>Properties</caption>
   * <tr><td><b>Key</b></td><td><b>Value</b></td></tr>
   * <tr><td>subject</td><td>quick brown fox</td></tr>
   * <tr><td>object</td><td>lazy dog</td></tr>
   * </table>
   * <p>
   * <b>Output</b>: {@code The quick brown fox jumps over the lazy dog}
   * </blockquote>
   *
   * @param text The string to interpolate.
   * @param properties The map with key=value entries for interpolation.
   * @param prefix String prefixing the key name.
   * @param suffix String suffixing the key name.
   * @return The interpolated string.
   * @see #interpolate(Map,String,String)
   * @throws NullPointerException If {@code text}, {@code properties},
   *           {@code prefix}, or {@code suffix} is null.
   */
  public static String interpolate(final String text, final Map<String,String> properties, final String prefix, final String suffix) {
    return interpolateDeep(new StringBuilder(Objects.requireNonNull(text)), Objects.requireNonNull(properties), Objects.requireNonNull(prefix), Objects.requireNonNull(suffix));
  }

  /**
   * Replaces each substring in the specified {@link StringBuilder} that matches
   * the literal target sequence with the specified literal replacement
   * sequence. The replacement proceeds from the beginning of the string to the
   * end, for example, replacing "aa" with "b" in the string "aaa" will result
   * in "ba" rather than "ab".
   *
   * @param builder The {@link StringBuilder}.
   * @param target The sequence of char values to be replaced
   * @param replacement The replacement sequence of char values
   * @return Whether the specified {@link StringBuilder} was changed as a result
   *         of this operation.
   * @throws NullPointerException If {@code builder}, {@code target}, or
   *           {@code replacement} is null.
   * @see String#replace(CharSequence, CharSequence)
   * @throws OutOfMemoryError If the specified parameters result in a
   *           {@link StringBuilder} that grows beyond length of
   *           {@link Integer#MAX_VALUE}.
   */
  public static boolean replace(final StringBuilder builder, final CharSequence target, final CharSequence replacement) {
    final String tgtStr = target.toString();
    final String replStr = replacement.toString();
    int j = builder.lastIndexOf(tgtStr);
    if (j < 0)
      return false;

    final int tgtLen = tgtStr.length();
    final int tgtLen1 = Math.max(tgtLen, 1);
    final int thisLen = builder.length();

    final int newLenHint = thisLen - tgtLen + replStr.length();
    if (newLenHint < 0)
      throw new OutOfMemoryError();

    do {
      builder.replace(j, j + tgtLen1, replStr);
    }
    while ((j = builder.lastIndexOf(tgtStr, j - tgtLen1)) > -1);
    return true;
  }

  /**
   * Replaces each substring of the specified {@link StringBuilder} that matches
   * the given {@code target} sequence with the given {@code replacement}
   * sequence. If a replacement operation results in a {@link StringBuilder}
   * with substrings that match the given {@code target} sequence, each
   * substring will be replaced as well.
   *
   * @param builder The {@link StringBuilder} in which all substrings are to be
   *          replaced.
   * @param target The sequence to be replaced.
   * @param replacement The sequence to be substituted for each match.
   * @return Whether the specified {@link StringBuilder} was changed as a result
   *         of this operation.
   * @throws OutOfMemoryError If the specified parameters result in a
   *           {@link StringBuilder} that grows beyond length of
   *           {@link Integer#MAX_VALUE}, or if the specified parameters result
   *           in a {@link StringBuilder} that grows perpetually.
   */
  public static boolean replaceAll(final StringBuilder builder, final CharSequence target, final CharSequence replacement) {
    int i = 0;
    for (; replace(builder, target, replacement); ++i);
    return i > 0;
  }

  /**
   * Tests if the specified {@link CharSequence} starts with the specified
   * prefix.
   *
   * @param string The {@link CharSequence}.
   * @param prefix The prefix.
   * @return {@code true} if the {@code prefix} character sequence is a prefix
   *         of {@code builder}; {@code false} otherwise. Note also that
   *         {@code true} will be returned if {@code prefix} is an empty string
   *         or is equal to {@code builder}.
   * @throws NullPointerException If {@code builder} or {@code prefix} is null.
   */
  public static boolean startsWith(final CharSequence string, final CharSequence prefix) {
    if (prefix.length() == 0)
      return true;

    if (string.length() < prefix.length())
      return false;

    for (int i = 0; i < prefix.length(); ++i)
      if (string.charAt(i) != prefix.charAt(i))
        return false;

    return true;
  }

  /**
   * Tests if the specified {@link CharSequence} ends with the specified suffix.
   *
   * @param string The {@link CharSequence}.
   * @param suffix The suffix.
   * @return {@code true} if the {@code suffix} character sequence is a suffix
   *         of {@code builder}; {@code false} otherwise. Note also that
   *         {@code true} will be returned if {@code suffix} is an empty string
   *         or is equal to {@code builder}.
   * @throws NullPointerException If {@code builder} or {@code suffix} is null.
   */
  public static boolean endsWith(final CharSequence string, final CharSequence suffix) {
    if (suffix.length() == 0)
      return true;

    if (string.length() < suffix.length())
      return false;

    final int offset = string.length() - suffix.length();
    for (int i = suffix.length() - 1; i >= 0; --i)
      if (string.charAt(offset + i) != suffix.charAt(i))
        return false;

    return true;
  }

  /**
   * Converts the characters in the specified {@link StringBuilder} to lowercase
   * using case mapping information from the UnicodeData file.
   *
   * @param builder The {@link StringBuilder}.
   * @return The specified {@link StringBuilder}, with its characters converted
   *         to lowercase.
   * @throws NullPointerException If {@code builder} is null.
   * @see Character#toLowerCase(char)
   */
  public static StringBuilder toLowerCase(final StringBuilder builder) {
    for (int i = 0; i < builder.length(); ++i)
      builder.setCharAt(i, Character.toLowerCase(builder.charAt(i)));

    return builder;
  }

  /**
   * Converts the characters in the specified {@link StringBuilder} to uppercase
   * using case mapping information from the UnicodeData file.
   *
   * @param builder The {@link StringBuilder}.
   * @return The specified {@link StringBuilder}, with its characters converted
   *         to uppercase.
   * @throws NullPointerException If {@code builder} is null.
   * @see Character#toUpperCase(char)
   */
  public static StringBuilder toUppereCase(final StringBuilder builder) {
    for (int i = 0; i < builder.length(); ++i)
      builder.setCharAt(i, Character.toUpperCase(builder.charAt(i)));

    return builder;
  }

  private static StringBuilder changeCase(final StringBuilder builder, final boolean upper, final int beginIndex, final int endIndex) {
    if (builder.length() == 0)
      return builder;

    if (beginIndex < 0)
      throw new IllegalArgumentException("start index (" + beginIndex + ") must be non-negative");

    if (endIndex < beginIndex)
      throw new IllegalArgumentException("start index (" + beginIndex + ") > end index (" + endIndex + ")");

    if (builder.length() < beginIndex)
      throw new IllegalArgumentException("start index (" + beginIndex + ") > string length (" + builder.length() + ")");

    if (beginIndex == endIndex)
      return builder;

    for (int i = beginIndex; i < endIndex; ++i)
      builder.setCharAt(i, upper ? Character.toUpperCase(builder.charAt(i)) : Character.toLowerCase(builder.charAt(i)));

    return builder;
  }

  /**
   * Converts the characters in the specified {@link StringBuilder} spanning the
   * provided index range to lowercase using case mapping information from the
   * UnicodeData file.
   *
   * @param builder The {@link StringBuilder}.
   * @param beginIndex The beginning index, inclusive.
   * @param endIndex The ending index, exclusive.
   * @return The specified {@link StringBuilder}, with the characters spanning
   *         the index range converted to lowercase.
   * @exception IllegalArgumentException If the {@code beginIndex} is negative,
   *              or {@code endIndex} is larger than the length of the
   *              {@link StringBuilder}, or {@code beginIndex} is larger than
   *              {@code endIndex}.
   * @throws NullPointerException If {@code builder} is null.
   * @see Character#toLowerCase(char)
   */
  public static StringBuilder toLowerCase(final StringBuilder builder, final int beginIndex, final int endIndex) {
    return changeCase(builder, false, beginIndex, endIndex);
  }

  /**
   * Converts all of the characters in the specified {@link StringBuilder}
   * starting at the provided begin index to lowercase using case mapping
   * information from the UnicodeData file.
   *
   * @param builder The {@link StringBuilder}.
   * @param beginIndex The beginning index, inclusive.
   * @return The specified {@link StringBuilder}, with all the characters
   *         following the provided begin index converted to lowercase.
   * @exception IllegalArgumentException If the {@code beginIndex} is negative
   *              or larger than the length of the {@link StringBuilder}.
   * @throws NullPointerException If {@code builder} is null.
   * @see Character#toLowerCase(char)
   */
  public static StringBuilder toLowerCase(final StringBuilder builder, final int beginIndex) {
    return changeCase(builder, false, beginIndex, builder.length());
  }

  /**
   * Converts the characters in the specified {@link StringBuilder} spanning the
   * provided index range to uppercase using case mapping information from the
   * UnicodeData file.
   *
   * @param builder The {@link StringBuilder}.
   * @param beginIndex The beginning index, inclusive.
   * @param endIndex The ending index, exclusive.
   * @return The specified {@link StringBuilder}, with the characters spanning
   *         the index range converted to uppercase.
   * @exception IllegalArgumentException If the {@code beginIndex} is negative,
   *              or {@code endIndex} is larger than the length of the
   *              {@link StringBuilder}, or {@code beginIndex} is larger than
   *              {@code endIndex}.
   * @throws NullPointerException If {@code builder} is null.
   * @see Character#toLowerCase(char)
   */
  public static StringBuilder toUpperCase(final StringBuilder builder, final int beginIndex, final int endIndex) {
    return changeCase(builder, true, beginIndex, endIndex);
  }

  /**
   * Converts all of the characters in the specified {@link StringBuilder}
   * starting at the provided begin index to uppercase using case mapping
   * information from the UnicodeData file.
   *
   * @param builder The {@link StringBuilder}.
   * @param beginIndex The beginning index, inclusive.
   * @return The specified {@link StringBuilder}, with all the characters
   *         following the provided begin index converted to uppercase.
   * @exception IllegalArgumentException If the {@code beginIndex} is negative
   *              or larger than the length of the {@link StringBuilder}.
   * @throws NullPointerException If {@code builder} is null.
   * @see Character#toLowerCase(char)
   */
  public static StringBuilder toUpperCase(final StringBuilder builder, final int beginIndex) {
    return changeCase(builder, true, beginIndex, builder.length());
  }

  /**
   * Returns a left-padded representation of the specified length for the
   * provided string. If {@code length > string.length()}, preceding characters
   * are filled with spaces ({@code ' '}). If {@code length == string.length()},
   * the provided string instance is returned. If
   * {@code length < string.length()}, this method throws
   * {@link IllegalArgumentException}.
   * <p>
   * This method is equivalent to calling {@code padLeft(string, length, ' ')}.
   *
   * @param string The string to pad.
   * @param length The length of the returned, padded string.
   * @return A left-padded representation of the specified length for the
   *         provided string.
   * @throws IllegalArgumentException If {@code length} is less than
   *           {@code string.length()}.
   * @throws NullPointerException If {@code string} is null.
   */
  public static String padLeft(final String string, final int length) {
    return pad(string, length, false, ' ');
  }

  /**
   * Returns a left-padded representation of the specified length for the
   * provided string. If {@code length > string.length()}, preceding characters
   * are filled with the specified {@code pad} char. If
   * {@code length == string.length()}, the provided string instance is
   * returned. If {@code length < string.length()}, this method throws
   * {@link IllegalArgumentException}.
   *
   * @param string The string to pad.
   * @param length The length of the returned, padded string.
   * @param pad The padding character.
   * @return A left-padded representation of the specified length for the
   *         provided string.
   * @throws IllegalArgumentException If {@code length} is less than
   *           {@code string.length()}.
   * @throws NullPointerException If {@code string} is null.
   */
  public static String padLeft(final String string, final int length, final char pad) {
    return pad(string, length, false, pad);
  }

  /**
   * Returns a right-padded representation of the specified length for the
   * provided string. If {@code length > string.length()}, ending characters are
   * filled with spaces ({@code ' '}). If {@code length == string.length()}, the
   * provided string instance is returned. If {@code length < string.length()},
   * this method throws {@link IllegalArgumentException}.
   * <p>
   * This method is equivalent to calling {@code padRight(string, length, ' ')}.
   *
   * @param string The string to pad.
   * @param length The length of the returned, padded string.
   * @return A right-padded representation of the specified length for the
   *         provided string.
   * @throws IllegalArgumentException If {@code length} is less than
   *           {@code string.length()}.
   * @throws NullPointerException If {@code string} is null.
   */
  public static String padRight(final String string, final int length) {
    return pad(string, length, true, ' ');
  }

  /**
   * Returns a right-padded representation of the specified length for the
   * provided string. If {@code length > string.length()}, ending characters are
   * filled with the specified {@code pad} char. If
   * {@code length == string.length()}, the provided string instance is
   * returned. If {@code length < string.length()}, this method throws
   * {@link IllegalArgumentException}.
   *
   * @param string The string to pad.
   * @param length The length of the returned, padded string.
   * @param pad The padding character.
   * @return A right-padded representation of the specified length for the
   *         provided string.
   * @throws IllegalArgumentException If {@code length} is less than
   *           {@code string.length()}.
   * @throws NullPointerException If {@code string} is null.
   */
  public static String padRight(final String string, final int length, final char pad) {
    return pad(string, length, true, pad);
  }

  private static String pad(final String string, final int length, final boolean right, final char pad) {
    final int len = string.length();
    if (length == len)
      return string;

    if (length < len)
      throw new IllegalArgumentException("length (" + length + ") must be greater or equal to string length (" + len + ")");

    final char[] chars = new char[length];
    if (right) {
      Arrays.fill(chars, len, length, pad);
      for (int i = 0; i < len; ++i)
        chars[i] = string.charAt(i);
    }
    else {
      final int offset = length - len;
      Arrays.fill(chars, 0, offset, pad);
      for (int i = 0; i < len; ++i)
        chars[i + offset] = string.charAt(i);
    }

    return new String(chars);
  }

  /**
   * Returns the hexadecimal representation of the specified value up to the
   * provided digits. If the number of digits is less than the full length of
   * the hexadecimal representation, the extra most significant digits are
   * truncated. If the number of digits is less than the full length of the
   * hexadecimal representation, the resultant string is left-padded with zeros
   * ({@code '0'}).
   *
   * @param value The value to convert to hexadecimal representation.
   * @param digits The number of digits to return, least significant digits
   *          first.
   * @return The hexadecimal representation of the specified value up to the
   *         provided digits
   */
  static String hex(long value, final int digits) {
    final boolean negative = value < 0;
    if (negative)
      value = -value;

    String hex = Long.toString(value & ((1L << 4 * digits) - 1), 16);
    if (hex.length() < digits)
      hex = padLeft(hex, digits, '0');

    return negative ? "-" + hex : hex;
  }

  /**
   * Returns the UTF-8 literal hexadecimal encoding of the specified
   * {@code char}.
   *
   * @param ch The {@code char} to encode.
   * @return The UTF-8 literal hexadecimal encoding of the specified
   *         {@code char}.
   */
  public static String toUTF8Literal(final char ch) {
    return "\\x" + hex(ch, 2);
  }

  /**
   * Returns the string of UTF-8 literal hexadecimal encodings of characters of
   * the specified {@link CharSequence}.
   *
   * @param string The {@link CharSequence} to encode.
   * @return The string of UTF-8 literal hexadecimal encodings of characters of
   *         the specified {@link CharSequence}.
   */
  public static String toUTF8Literal(final CharSequence string) {
    final int len = string.length();
    final StringBuilder builder = new StringBuilder(len * 4);
    for (int i = 0; i < len; ++i)
      builder.append(toUTF8Literal(string.charAt(i)));

    return builder.toString();
  }

  /**
   * Returns a base 26 representation of {@code n} in alphabetical digits. The
   * {@code 0}th string is {@code "a"}, and the {@code 25}th is {@code "z"}. For
   * {@code n} between {@code 26} and {@code 51}, the resulting string is 2
   * characters in length, and starts with {@code 'a'}. For {@code n} between
   * {@code 52} and {@code 77}, the resulting string is 2 characters in length,
   * and starts with {@code 'b'}. In effect, this method
   *
   * @param n The decimal value to convert into a base 26 representation of
   *          {@code n} in alphabetical digits.
   * @return A base 26 representation of {@code n} in alphabetical digits.
   */
  public static String getAlpha(final int n) {
    final int scale;
    return n < '{' - 'a' ? String.valueOf((char)('a' + n)) : getAlpha((scale = n / ('{' - 'a')) - 1) + (char)('a' + n - scale * ('{' - 'a'));
  }

  /**
   * Returns the prefix string that is shared amongst all members for the
   * specified {@link String} array.
   *
   * @param strings The {@link String} array in which to find a common prefix.
   * @return The prefix string that is shared amongst all members for the
   *         specified {@link String} array.
   */
  public static String getCommonPrefix(final String ... strings) {
    if (strings == null || strings.length == 0)
      return null;

    if (strings.length == 1)
      return strings[0];

    for (int i = 0; i < strings[0].length(); ++i)
      for (int j = 1; j < strings.length; ++j)
        if (i == strings[j].length() || strings[0].charAt(i) != strings[j].charAt(i))
          return strings[0].substring(0, i);

    return strings[0];
  }

  /**
   * Returns the prefix string that is shared amongst all members for the
   * specified {@link Collection}.
   *
   * @param strings The {@link Collection} of strings in which to find a common
   *          prefix.
   * @return The prefix string that is shared amongst all members for the
   *         specified {@link Collection}.
   */
  public static String getCommonPrefix(final Collection<String> strings) {
    if (strings == null || strings.size() == 0)
      return null;

    Iterator<String> iterator = strings.iterator();
    if (strings.size() == 1)
      return iterator.next();

    final String string0 = iterator.next();
    for (int i = 0; i < string0.length(); ++i) {
      if (i > 0) {
        iterator = strings.iterator();
        iterator.next();
      }

      while (iterator.hasNext()) {
        final String next = iterator.next();
        if (i == next.length() || string0.charAt(i) != next.charAt(i))
          return string0.substring(0, i);
      }
    }

    return string0;
  }

  /**
   * Returns a representation of the specified string that is able to be
   * contained in a {@link String} literal in Java.
   *
   * @param string The string to transform.
   * @return A representation of the specified string that is able to be
   *         contained in a {@link String} literal in Java.
   */
  public static String escapeForJava(final String string) {
    return string == null ? null : string.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  public static String printColumns(final String ... columns) {
    // Split input strings into columns and rows
    final String[][] strings = new String[columns.length][];
    int maxLines = 0;
    for (int i = 0; i < columns.length; ++i) {
      strings[i] = columns[i] == null ? null : columns[i].split("\n");
      if (strings[i] != null && strings[i].length > maxLines)
        maxLines = strings[i].length;
    }

    // Store an array of column widths
    final int[] widths = new int[columns.length];
    // calculate column widths
    for (int i = 0, maxWidth = 0; i < columns.length; ++i) {
      if (strings[i] != null) {
        for (int j = 0; j < strings[i].length; ++j)
          if (strings[i][j].length() > maxWidth)
            maxWidth = strings[i][j].length();
      }
      else if (maxWidth < 4) {
        maxWidth = 4;
      }

      widths[i] = maxWidth + 1;
    }

    // Print the lines
    final StringBuilder builder = new StringBuilder();
    for (int j = 0; j < maxLines; ++j) {
      if (j > 0)
        builder.append('\n');

      for (int i = 0; i < strings.length; ++i) {
        final String line = strings[i] == null ? "null" : j < strings[i].length ? strings[i][j] : "";
        builder.append(String.format("%-" + widths[i] + "s", line));
      }
    }

    return builder.length() == 0 ? "null" : builder.toString();
  }

  /**
   * Returns a string consisting of a specific number of concatenated
   * repetitions of an input character. For example,
   * {@code Strings.repeat('a', 3)} returns the string {@code "aaa"}.
   *
   * @param ch The {@code char} to repeat.
   * @param count A nonnegative number of times to repeat the specified
   *          {@code char}.
   * @return A string containing the specified {@code char} repeated
   *         {@code count} times; an empty string if {@code count == 0}.
   * @throws NullPointerException If the specified string is null.
   * @throws IllegalArgumentException If {@code count < 0}.
   * @throws ArrayIndexOutOfBoundsException If
   *           {@code string.length() * count > Integer.MAX_VALUE}.
   */
  public static String repeat(final char ch, final int count) {
    if (count < 0)
      throw new IllegalArgumentException("count < 0");

    if (count == 0)
      return "";

    final char[] chars = new char[count];
    Arrays.fill(chars, ch);
    return new String(chars);
  }

  /**
   * Returns a string consisting of a specific number of concatenated
   * repetitions of an input string. For example,
   * {@code Strings.repeat("ha", 3)} returns the string {@code "hahaha"}.
   *
   * @param string Any non-null string.
   * @param count A nonnegative number of times to repeat the specified string.
   * @return A string containing the specified {@code string} repeated
   *         {@code count} times; an empty string if {@code count == 0}; the
   *         {@code string} if {@code count == 1}.
   * @throws NullPointerException If the specified string is null.
   * @throws IllegalArgumentException If {@code count < 0}.
   * @throws ArrayIndexOutOfBoundsException If
   *           {@code string.length() * count > Integer.MAX_VALUE}.
   */
  public static String repeat(final String string, final int count) {
    if (count < 0)
      throw new IllegalArgumentException("count < 0");

    if (count == 0 || string.length() == 0)
      return "";

    if (count == 1)
      return string;

    final int length = string.length();
    final long longSize = (long)length * count;
    final int size = (int)longSize;
    if (size != longSize)
      throw new ArrayIndexOutOfBoundsException("Required array size too large: " + longSize);

    final char[] chars = new char[size];
    string.getChars(0, length, chars, 0);
    int n = length;
    for (; n < size - n; n <<= 1)
      System.arraycopy(chars, 0, chars, n, n);

    System.arraycopy(chars, 0, chars, n, size - n);
    return new String(chars);
  }

  /**
   * Encodes the specified {@link String} into a sequence of bytes using the
   * named charset, storing the result into a new byte array.
   * <p>
   * This method differentiates itself from {@link String#getBytes(String)} by
   * throwing the unchecked {@link UnsupportedOperationException} instead of the
   * checked {@link UnsupportedEncodingException} if the named charset is not
   * supported.
   *
   * @param string The string to encode.
   * @param charsetName The name of a supported
   *          {@linkplain java.nio.charset.Charset charset}.
   * @return The resultant byte array.
   * @throws UnsupportedOperationException If the named charset is not
   *           supported.
   * @throws NullPointerException If {@code string} or {@code charsetName} is
   *           null.
   * @see String#getBytes(String)
   */
  public static byte[] getBytes(final String string, final String charsetName) {
    try {
      return string.getBytes(charsetName);
    }
    catch (final UnsupportedEncodingException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  /**
   * Returns the specified string with any leading and trailing characters
   * matching the provided {@code char} removed.
   *
   * @param string The string to be trimmed.
   * @param ch The {@code char} to remove from the front and back of the
   *          specified string.
   * @return The specified string with any leading and trailing characters
   *         matching the provided {@code char} removed.
   */
  public static String trim(final String string, final char ch) {
    if (string == null)
      return null;

    int i = -1;
    final int len = string.length();
    while (++i < len && string.charAt(i) == ch);
    if (i == len)
      return "";

    int j = len;
    while (j > i && string.charAt(--j) == ch);
    return i == 0 && j == len - 1 ? string : string.substring(i, j + 1);
  }

  /**
   * Returns the index within the specified string of the first occurrence of
   * the provided character that is not within a quoted section of the string,
   * starting the search at the specified index. A quoted section of a string
   * starts with a double-quote character ({@code '"'}) and ends with a
   * double-quote character or the end of the string.
   *
   * @param string The string.
   * @param ch The character to find.
   * @param fromIndex The index to start the search from. There is no
   *          restriction on the value of {@code fromIndex}. If it is greater
   *          than or equal to the length of the string, it has the same effect
   *          as if it were equal to one less than the length of the string: the
   *          entire string may be searched. If it is negative, it has the same
   *          effect as if it were {@code -1}: {@code -1} is returned.
   * @return The index within the specified string of the first occurrence of
   *         the provided character that is not within a quoted section of the
   *         string, or {@code -1} if the character is not found in an unquoted
   *         section.
   * @throws NullPointerException If the specified string is null.
   */
  public static int indexOfUnQuoted(final CharSequence string, final char ch, final int fromIndex) {
    boolean esacped = false;
    boolean quoted = false;
    for (int i = Math.max(fromIndex, 0), len = string.length(); i < len; ++i) {
      final char c = string.charAt(i);
      if (c == '\\')
        esacped = true;
      else if (esacped)
        esacped = false;
      else if (c == ch && !quoted)
        return i;
      else if (c == '"')
        quoted = !quoted;
    }

    return -1;
  }

  /**
   * Returns the index within the specified string of the first occurrence of
   * the provided character that is not within a quoted section of the string. A
   * quoted section of a string starts with a double-quote character
   * ({@code '"'}) and ends with a double-quote character or the end of the
   * string.
   *
   * @param string The string.
   * @param ch The character to find.
   * @return The index within the specified string of the first occurrence of
   *         the provided character that is not within a quoted section of the
   *         string, or {@code -1} if the character is not found in an unquoted
   *         section.
   * @throws NullPointerException If the specified string is null.
   */
  public static int indexOfUnQuoted(final CharSequence string, final char ch) {
    return indexOfUnQuoted(string, ch, 0);
  }

  /**
   * Returns the index within the specified string of the last occurrence of the
   * provided character that is not within a quoted section of the string,
   * searching backward starting at the specified index. A quoted section of a
   * string ends with a double-quote character ({@code '"'}) and starts with a
   * double-quote character or the start of the string.
   *
   * @param string The string.
   * @param ch The character to find.
   * @param fromIndex The index to start the search from. There is no
   *          restriction on the value of {@code fromIndex}. If it is greater
   *          than or equal to the length of the string, it has the same effect
   *          as if it were equal to one less than the length of the string: the
   *          entire string may be searched. If it is negative, it has the same
   *          effect as if it were {@code -1}: {@code -1} is returned.
   * @return The index within the specified string of the first occurrence of
   *         the provided character that is not within a quoted section of the
   *         string, or {@code -1} if the character is not found in an unquoted
   *         section.
   * @throws NullPointerException If the specified string is null.
   */
  public static int lastIndexOfUnQuoted(final CharSequence string, final char ch, final int fromIndex) {
    boolean esacped = false;
    boolean quoted = false;
    char n = '\0';
    for (int end = string.length() - 1, i = Math.min(fromIndex, end); i >= 0; --i) {
      final char c = string.charAt(i);
      if (c == '\\')
        esacped = true;
      else if (esacped)
        esacped = false;
      else if (n == ch && !quoted)
        return i + 1;
      else if (n == '"')
        quoted = !quoted;

      n = c;
    }

    return n == ch ? 0 : -1;
  }

  /**
   * Returns the index within the specified string of the last occurrence of the
   * provided character that is not within a quoted section of the string. A
   * quoted section of a string ends with a double-quote character ({@code '"'})
   * and starts with a double-quote character or the start of the string.
   *
   * @param string The string.
   * @param ch The character to find.
   * @return The index within the specified string of the first occurrence of
   *         the provided character that is not within a quoted section of the
   *         string, or {@code -1} if the character is not found in an unquoted
   *         section.
   * @throws NullPointerException If the specified string is null.
   */
  public static int lastIndexOfUnQuoted(final CharSequence string, final char ch) {
    return lastIndexOfUnQuoted(string, ch, string.length());
  }

  /**
   * Truncates the specified string to the provided maximum length, adding
   * ellipses ({@code "..."}) if the string is longer than maximum length.
   * <p>
   * Special conditions:
   * <ul>
   * <li>If {@code maxLength < 3}, this method throws an
   * {@link IllegalArgumentException}.</li>
   * <li>If {@code maxLength == 3}, this method returns {@code "..."}.</li>
   * <li>If {@code maxLength >= string.length()}, this method returns
   * {@code string}.</li>
   * <li>If {@code maxLength < string.length()}, this method returns:
   * <blockquote>{@code string.substring(0, maxLength - 3) + "..."}
   * </blockquote></li>
   * </ul>
   *
   * @param string The string to truncate.
   * @param maxLength The max length of the resulting string (must be
   *          {@code >= 3}).
   * @return The truncated string.
   * @throws IllegalArgumentException If the provided length is less than 3.
   * @throws NullPointerException If the specified string is null.
   */
  public static String truncate(final String string, final int maxLength) {
    if (maxLength < 3)
      throw new IllegalArgumentException("length must be >= 3: " + maxLength);

    return maxLength == 3 ? "..." : string.length() > maxLength ? string.substring(0, maxLength - 3).concat("...") : string;
  }

  /**
   * Flips the capitalization of the first character of the specified string.
   * If the string is in ALLCAPS from the second character to the end, this
   * method returns the original string. This method is reversible, as in: if
   * the resulting string is used as the input, the original input string will
   * be returned.
   * <p>
   * <blockquote>
   * <b>Example:</b>
   * <p>
   * <table>
   * <caption>Example</caption>
   * <tr><td><b>Input</b></td><td><b>Output</b></td></tr>
   * <tr><td>foo</td><td>Foo</td></tr>
   * <tr><td>fooBar</td><td>FooBar</td></tr>
   * <tr><td>BAR</td><td>BAR</td></tr>
   * <tr><td>fOO</td><td>fOO</td></tr>
   * <tr><td>baR</td><td>BaR</td></tr>
   * <tr><td>FooBar</td><td>fooBar</td></tr>
   * </table>
   * </blockquote>
   * @param string The string.
   * @return The specified string with its first character's capitalization
   *         flipped, as per the described rules.
   * @throws NullPointerException If {@code string} is null.
   */
  public static String flipFirstCap(final String string) {
    if (string.length() == 0)
      return string;

    boolean hasLower = false;
    boolean hasUpper = false;
    for (int i = 1; i < string.length(); ++i) {
      hasLower = hasLower || Character.isLowerCase(string.charAt(i));
      hasUpper = hasUpper || Character.isUpperCase(string.charAt(i));
      if (hasLower && hasUpper)
        break;
    }

    // If the string is ALLUPPER or aLLUPPER then don't modify it
    if (hasUpper && !hasLower)
      return string;

    final char ch = string.charAt(0);
    return (Character.isLowerCase(ch) ? Character.toUpperCase(ch) : Character.toLowerCase(ch)) + string.substring(1);
  }

  private static void appendElVar(final Map<String,String> variables, final StringBuilder builder, final StringBuilder var) {
    final String name = var.toString();
    final String value = variables.get(name);
    if (value != null)
      builder.append(value);
    else
      builder.append('$').append('{').append(name).append('}');

    var.setLength(0);
  }

  private static void appendElNoMatch(final StringBuilder builder, final StringBuilder var, final char close) {
    builder.append('$').append('{');
    if (var.length() > 0) {
      builder.append(var);
      var.setLength(0);
    }

    if (close != '\0')
      builder.append(close);
  }

  /**
   * Dereferences all Expression Language-encoded names, such as
   * <code>${foo}</code> or <code>${bar}</code>, in the specified string with
   * values in the specified map.
   * <p>
   * Names encoded in Expression Language follow the same rules as <a href=
   * "https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8">Java
   * Identifiers</a>.
   *
   * @param s The string in which EL-encoded names are to be dereferenced.
   * @param variables The map of name to value pairs.
   * @return The specified string with EL-encoded names replaced with their
   *         mapped values. If a name is missing from the specified map, or if a
   *         name does not conform to the rules of <a href=
   *         "https://docs.oracle.com/javase/specs/jls/se7/html/jls-3.html#jls-3.8">Java
   *         Identifiers</a>, or if the Expression Language encoding is
   *         malformed, it will remain in the string as-is.
   * @throws NullPointerException If {@code s} is null, or if {@code s} contains
   *           an EL-encoded name and {@code variables} is null.
   */
  public static String derefEL(final String s, final Map<String,String> variables) {
    if (s.length() < 4)
      return s;

    final StringBuilder builder = new StringBuilder();
    final StringBuilder var = new StringBuilder();
    boolean escape = false;
    final int len = s.length();
    for (int i = 0; i < len; ++i) {
      char ch = s.charAt(i);
      if (ch == '\\') {
        if (var.length() > 0) {
          builder.append('$').append('{').append(var);
          var.setLength(0);
        }

        if (!(escape = !escape))
          builder.append(ch);
      }
      else if (!escape) {
        if (ch == '$') {
          if (var.length() > 0) {
            appendElVar(variables, builder, var);
          }

          if (++i == len) {
            builder.append('$');
          }
          else {
            ch = s.charAt(i);
            if (ch != '{') {
              var.setLength(0);
              builder.append('$');
              if (ch != '\\')
                builder.append(ch);
            }
            else if (++i == len) {
              appendElNoMatch(builder, var, '\0');
            }
            else {
              ch = s.charAt(i);
              if ('a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || ch == '_')
                var.append(ch);
              else
                appendElNoMatch(builder, var, ch);
            }
          }
        }
        else if (var.length() > 0) {
          if ('a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || '0' <= ch && ch <= '9' || ch == '_') {
            var.append(ch);
          }
          else if (ch != '}') {
            appendElNoMatch(builder, var, ch);
          }
          else {
            appendElVar(variables, builder, var);
            if (ch != '}')
              builder.append(ch);
          }
        }
        else {
          builder.append(ch);
        }
      }
      else {
        if (var.length() > 0)
          appendElVar(variables, builder, var);

        builder.append(ch);
        escape = false;
      }
    }

    if (var.length() > 0)
      appendElNoMatch(builder, var, '\0');

    return builder.toString();
  }

  private static void appendEvVar(final Map<String,String> variables, final StringBuilder builder, final StringBuilder var) {
    final String variable = variables.get(var.toString());
    if (variable != null)
      builder.append(variable);

    var.setLength(0);
  }

  /**
   * Dereferences all POSIX-compliant Environment Variable names, such as
   * <code>$FOO</code> or <code>${BAR}</code>, in the specified string with
   * values in the specified map.
   * <p>
   * Names encoded in POSIX format follow the rules defined in the POSIX
   * standard on shells <a href=
   * "http://pubs.opengroup.org/onlinepubs/9699919799/utilities/V3_chap02.html#tag_18_10_02">IEEE
   * Std 1003.1-2017</a>.
   *
   * @param s The string in which POSIX-compliant names are to be dereferenced.
   * @param variables The map of name to value pairs.
   * @return The specified string with POSIX-compliant names replaced with their
   *         mapped values. If a name is missing from the specified map, it will
   *         remain in the string as-is.
   * @throws ParseException If the encoding of the environment variable name is
   *           malformed.
   * @throws NullPointerException If {@code s} is null, or if {@code s} contains
   *           an POSIX-compliant name and {@code variables} is null.
   */
  public static String derefEV(final String s, final Map<String,String> variables) throws ParseException {
    if (s.length() < 2)
      return s;

    final StringBuilder builder = new StringBuilder();
    final StringBuilder var = new StringBuilder();
    boolean escape = false;
    boolean bracket = false;
    final int len = s.length();
    for (int i = 0; i < len; ++i) {
      char ch = s.charAt(i);
      if (ch == '\\') {
        if (var.length() > 0)
          appendEvVar(variables, builder, var);

        if (!(escape = !escape))
          builder.append(ch);
      }
      else if (!escape) {
        if (ch == '$') {
          if (var.length() > 0)
            appendEvVar(variables, builder, var);

          if (++i == len) {
            builder.append('$');
          }
          else {
            ch = s.charAt(i);
            if (ch == '$')
              throw new ParseException("$$: not supported", i);

            if (ch == '{') {
              bracket = true;
              if (++i == len)
                throw new ParseException("${: bad substitution", i);

              ch = s.charAt(i);
            }

            if ('a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || ch == '_') {
              var.append(ch);
            }
            else if (!bracket) {
              builder.append('$');
              if (ch != '\\')
                builder.append(ch);
            }
            else {
              throw new ParseException("${" + ch + ": bad substitution", i);
            }
          }
        }
        else if (var.length() > 0) {
          if ('a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || '0' <= ch && ch <= '9' || ch == '_') {
            var.append(ch);
          }
          else if (bracket && ch != '}') {
            throw new ParseException("${" + var + ch + ": bad substitution", i);
          }
          else {
            appendEvVar(variables, builder, var);
            if (!bracket || ch != '}')
              builder.append(ch);
          }
        }
        else {
          builder.append(ch);
        }
      }
      else {
        if (var.length() > 0)
          appendEvVar(variables, builder, var);

        builder.append(ch);
        escape = false;
      }
    }

    if (var.length() > 0) {
      if (bracket)
        throw new ParseException("${" + var + ": bad substitution", len);

      appendEvVar(variables, builder, var);
    }

    return builder.toString();
  }

  /**
   * Tests if the specified string is fully comprised of whitespace characters.
   *
   * @param string The {@link String} to test.
   * @return {@code true} if the specified string is fully comprised of
   *         whitespace characters. This method returns {@code true} for empty
   *         strings and {@code false} for null.
   */
  public static boolean isWhitespace(final CharSequence string) {
    if (string == null)
      return false;

    final int len = string.length();
    for (int i = 0; i < len; ++i)
      if (!Character.isWhitespace(string.charAt(i)))
        return false;

    return true;
  }

  /**
   * Tests if the specified string represents a regular expression that <u>can
   * match more than 1 string (i.e. "abc" is technically a regular expression,
   * however it can only match a single string: "abc")</u>.
   *
   * @param string The {@link String} to test.
   * @return {@code true} if the specified string represents a regular
   *         expression that <u>can match more than 1 string</u>. This method
   *         returns {@code false} for a null input.
   */
  public static boolean isRegex(final String string) {
    if (string == null)
      return false;

    try {
      Pattern.compile(string);
    }
    catch (final PatternSyntaxException e) {
      return false;
    }

    final int len = string.length();
    boolean escaped = false;
    boolean hasOpenBracket = false;
    boolean hasOpenBrace = false;
    boolean hasOpenParentheses = false;
    for (int i = 0; i < len; ++i) {
      final char ch = string.charAt(i);
      if (i == 0 && ch == '^' || i == len - 1 && !escaped && ch == '$')
        return true;

      if (escaped) {
        if (ch == 'd' || ch == 'D' || ch == 's' || ch == 'S' || ch == 'w' || ch == 'W' || ch == 'b' || ch == 'B' || ch == 'A' || ch == 'G' || ch == 'Z' || ch == 'z' || ch == 'Q' || ch == 'E')
          return true;
      }
      else if (!hasOpenBracket && ch == '[')
        hasOpenBracket = true;
      else if (!hasOpenBrace && ch == '{')
        hasOpenBrace = true;
      else if (!hasOpenParentheses && ch == '(')
        hasOpenParentheses = true;
      else if (ch == '.' || (i > 0 && (ch == '?' || ch == '*' || ch == '+' || (hasOpenBracket && ch == ']') || (hasOpenBrace && ch == '}') || (hasOpenParentheses && ch == ')') || ch == '|'))) {
        return true;
      }

      escaped ^= ch == '\\';
    }

    return false;
  }

  /**
   * Determines whether all characters in the specified {@link CharSequence} are
   * in the general category type of {@link Character#LOWERCASE_LETTER}.
   *
   * @param builder The {@link CharSequence}.
   * @return {@code true} if all characters in the specified
   *         {@link CharSequence} are lower-case characters; otherwise
   *         {@code false}.
   * @throws IllegalArgumentException If the specified {@link CharSequence} is
   *           empty.
   * @throws NullPointerException If the specified {@link CharSequence} is null.
   */
  public static boolean isLowerCase(final CharSequence builder) {
    if (builder.length() == 0)
      throw new IllegalArgumentException("Empty string");

    for (int i = 0, len = builder.length(); i < len; ++i)
      if (!Character.isLowerCase(builder.charAt(i)))
        return false;

    return true;
  }

  /**
   * Determines whether all characters in the specified {@link CharSequence} are
   * in the general category type of {@link Character#UPPERCASE_LETTER}.
   *
   * @param builder The {@link CharSequence}.
   * @return {@code true} if all characters in the specified
   *         {@link CharSequence} are upper-case characters; otherwise
   *         {@code false}.
   * @throws IllegalArgumentException If the specified {@link CharSequence} is
   *           empty.
   * @throws NullPointerException If the specified {@link CharSequence} is null.
   */
  public static boolean isUpperCase(final CharSequence builder) {
    if (builder.length() == 0)
      throw new IllegalArgumentException("Empty string");

    for (int i = 0, len = builder.length(); i < len; ++i)
      if (!Character.isUpperCase(builder.charAt(i)))
        return false;

    return true;
  }

  public static String requireLettersOrDigits(final String token) {
    if (token == null)
      return null;

    for (int i = 0; i < token.length(); ++i) {
      final char ch = token.charAt(i);
      if (!Character.isLetterOrDigit(ch))
        throw new IllegalArgumentException(token);
    }

    return token;
  }

  public static long hash(final String str) {
    long hash = 0;
    for (int i = 0, len = str.length(); i < len; ++i)
      hash = 31 * hash + str.charAt(i);

    return hash;
  }

  public static StringBuilder indent(final String str, final int spaces) {
    final StringBuilder builder = new StringBuilder(str);
    return indent(builder, spaces);
  }

  public static StringBuilder indent(final StringBuilder builder, final int spaces) {
    final String replacement = "\n" + repeat(' ', spaces);
    Strings.replace(builder, "\n\n", "\7\n");
    Strings.replace(builder, "\n", replacement);
    Strings.replace(builder, "\7", "\n");
    return builder;
  }

  private Strings() {
  }
}