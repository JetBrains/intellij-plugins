/**
 * $RCSfile$
 * $Revision$
 * $Date$
 *
 * Copyright 2003-2004 Jive Software.
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.smack.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * A collection of utility methods for String objects.
 */
public final class StringUtils {

    private static final char[] QUOTE_ENCODE = "&quot;".toCharArray();
    private static final char[] AMP_ENCODE = "&amp;".toCharArray();
    private static final char[] LT_ENCODE = "&lt;".toCharArray();
    private static final char[] GT_ENCODE = "&gt;".toCharArray();

    /**
     * Returns the name portion of a XMPP address. For example, for the
     * address "matt@jivesoftware.com/Smack", "matt" would be returned. If no
     * username is present in the address, the empty string will be returned.
     *
     * @param XMPPAddress the XMPP address.
     * @return the name portion of the XMPP address.
     */
    public static String parseName(String XMPPAddress) {
        if (XMPPAddress == null) {
            return null;
        }
        int atIndex = XMPPAddress.indexOf("@");
        if (atIndex <= 0) {
            return "";
        }
        else {
            return XMPPAddress.substring(0, atIndex);
        }
    }

    /**
     * Returns the server portion of a XMPP address. For example, for the
     * address "matt@jivesoftware.com/Smack", "jivesoftware.com" would be returned.
     * If no server is present in the address, the empty string will be returned.
     *
     * @param XMPPAddress the XMPP address.
     * @return the server portion of the XMPP address.
     */
    public static String parseServer(String XMPPAddress) {
        if (XMPPAddress == null) {
            return null;
        }
        int atIndex = XMPPAddress.indexOf("@");
        // If the String ends with '@', return the empty string.
        if (atIndex + 1 > XMPPAddress.length()) {
            return "";
        }
        int slashIndex = XMPPAddress.indexOf("/");
        if (slashIndex > 0) {
            return XMPPAddress.substring(atIndex + 1, slashIndex);
        }
        else {
            return XMPPAddress.substring(atIndex + 1);
        }
    }

    /**
     * Returns the resource portion of a XMPP address. For example, for the
     * address "matt@jivesoftware.com/Smack", "Smack" would be returned. If no
     * resource is present in the address, the empty string will be returned.
     *
     * @param XMPPAddress the XMPP address.
     * @return the resource portion of the XMPP address.
     */
    public static String parseResource(String XMPPAddress) {
        if (XMPPAddress == null) {
            return null;
        }
        int slashIndex = XMPPAddress.indexOf("/");
        if (slashIndex + 1 > XMPPAddress.length() || slashIndex < 0) {
            return "";
        }
        else {
            return XMPPAddress.substring(slashIndex + 1);
        }
    }

    /**
     * Returns the XMPP address with any resource information removed. For example,
     * for the address "matt@jivesoftware.com/Smack", "matt@jivesoftware.com" would
     * be returned.
     *
     * @param XMPPAddress the XMPP address.
     * @return the bare XMPP address without resource information.
     */
    public static String parseBareAddress(String XMPPAddress) {
        if (XMPPAddress == null) {
            return null;
        }
        int slashIndex = XMPPAddress.indexOf("/");
        if (slashIndex < 0) {
            return XMPPAddress;
        }
        else if (slashIndex == 0) {
            return "";
        }
        else {
            return XMPPAddress.substring(0, slashIndex);
        }
    }

    /**
     * Escapes all necessary characters in the String so that it can be used
     * in an XML doc.
     *
     * @param string the string to escape.
     * @return the string with appropriate characters escaped.
     */
    public static String escapeForXML(String string) {
        if (string == null) {
            return null;
        }
        char ch;
        int i=0;
        int last=0;
        char[] input = string.toCharArray();
        int len = input.length;
        StringBuilder out = new StringBuilder((int)(len*1.3));
        for (; i < len; i++) {
            ch = input[i];
            if (ch > '>') {
            }
            else if (ch == '<') {
                if (i > last) {
                    out.append(input, last, i - last);
                }
                last = i + 1;
                out.append(LT_ENCODE);
            }
            else if (ch == '>') {
                if (i > last) {
                    out.append(input, last, i - last);
                }
                last = i + 1;
                out.append(GT_ENCODE);
            }

            else if (ch == '&') {
                if (i > last) {
                    out.append(input, last, i - last);
                }
                // Do nothing if the string is of the form &#235; (unicode value)
                if (!(len > i + 5
                    && input[i + 1] == '#'
                    && Character.isDigit(input[i + 2])
                    && Character.isDigit(input[i + 3])
                    && Character.isDigit(input[i + 4])
                    && input[i + 5] == ';')) {
                        last = i + 1;
                        out.append(AMP_ENCODE);
                    }
            }
            else if (ch == '"') {
                if (i > last) {
                    out.append(input, last, i - last);
                }
                last = i + 1;
                out.append(QUOTE_ENCODE);
            }
        }
        if (last == 0) {
            return string;
        }
        if (i > last) {
            out.append(input, last, i - last);
        }
        return out.toString();
    }

    /**
     * Used by the hash method.
     */
    private static MessageDigest digest = null;

    /**
     * Hashes a String using the SHA-1 algorithm and returns the result as a
     * String of hexadecimal numbers. This method is synchronized to avoid
     * excessive MessageDigest object creation. If calling this method becomes
     * a bottleneck in your code, you may wish to maintain a pool of
     * MessageDigest objects instead of using this method.
     * <p>
     * A hash is a one-way function -- that is, given an
     * input, an output is easily computed. However, given the output, the
     * input is almost impossible to compute. This is useful for passwords
     * since we can store the hash and a hacker will then have a very hard time
     * determining the original password.
     *
     * @param data the String to compute the hash of.
     * @return a hashed version of the passed-in String
     */
    public synchronized static String hash(String data) {
        if (digest == null) {
            try {
                digest = MessageDigest.getInstance("SHA-1");
            }
            catch (NoSuchAlgorithmException nsae) {
                System.err.println("Failed to load the SHA-1 MessageDigest. " +
                "Jive will be unable to function normally.");
            }
        }
        // Now, compute hash.
        try {
            digest.update(data.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            System.err.println(e);
        }
        return encodeHex(digest.digest());
    }

    /**
     * Encodes an array of bytes as String representation of hexadecimal.
     *
     * @param bytes an array of bytes to convert to a hex string.
     * @return generated hex string.
     */
    public static String encodeHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);

        for (byte aByte : bytes) {
            if (((int) aByte & 0xff) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toString((int) aByte & 0xff, 16));
        }

        return hex.toString();
    }

    /**
     * Encodes a String as a base64 String.
     *
     * @param data a String to encode.
     * @return a base64 encoded String.
     */
    public static String encodeBase64(String data) {
        byte [] bytes = null;
        try {
            bytes = data.getBytes("ISO-8859-1");
        }
        catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        return encodeBase64(bytes);
    }

    /**
     * Encodes a byte array into a base64 String.
     *
     * @param data a byte array to encode.
     * @return a base64 encode String.
     */
    public static String encodeBase64(byte[] data) {
        return encodeBase64(data, false);
    }

    /**
     * Encodes a byte array into a bse64 String.
     *
     * @param data The byte arry to encode.
     * @param lineBreaks True if the encoding should contain line breaks and false if it should not.
     * @return A base64 encoded String.
     */
    public static String encodeBase64(byte[] data, boolean lineBreaks) {
        return encodeBase64(data, 0, data.length, lineBreaks);
    }

    /**
     * Encodes a byte array into a bse64 String.
     *
     * @param data The byte arry to encode.
     * @param offset the offset of the bytearray to begin encoding at.
     * @param len the length of bytes to encode.
     * @param lineBreaks True if the encoding should contain line breaks and false if it should not.
     * @return A base64 encoded String.
     */
    public static String encodeBase64(byte[] data, int offset, int len, boolean lineBreaks) {
        return Base64.encodeBytes(data, offset, len, (lineBreaks ?  Base64.NO_OPTIONS : Base64.DONT_BREAK_LINES));
    }

    /**
     * Decodes a base64 String.
     *
     * @param data a base64 encoded String to decode.
     * @return the decoded String.
     */
    public static byte[] decodeBase64(String data) {
        return Base64.decode(data);
    }

    /**
     * Pseudo-random number generator object for use with randomString().
     * The Random class is not considered to be cryptographically secure, so
     * only use these random Strings for low to medium security applications.
     */
    private static Random randGen = new Random();

    /**
     * Array of numbers and letters of mixed case. Numbers appear in the list
     * twice so that there is a more equal chance that a number will be picked.
     * We can use the array to get a random number or letter by picking a random
     * array index.
     */
    private static char[] numbersAndLetters = ("0123456789abcdefghijklmnopqrstuvwxyz" +
                    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ").toCharArray();

    /**
     * Returns a random String of numbers and letters (lower and upper case)
     * of the specified length. The method uses the Random class that is
     * built-in to Java which is suitable for low to medium grade security uses.
     * This means that the output is only pseudo random, i.e., each number is
     * mathematically generated so is not truly random.<p>
     *
     * The specified length must be at least one. If not, the method will return
     * null.
     *
     * @param length the desired length of the random String to return.
     * @return a random String of numbers and letters of the specified length.
     */
    public static String randomString(int length) {
        if (length < 1) {
            return null;
        }
        // Create a char buffer to put random letters and numbers in.
        char [] randBuffer = new char[length];
        for (int i=0; i<randBuffer.length; i++) {
            randBuffer[i] = numbersAndLetters[randGen.nextInt(71)];
        }
        return new String(randBuffer);
    }

    private StringUtils() {
        // Not instantiable.
    }
}