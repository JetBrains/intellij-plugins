/*
 * Copyright (c) OSGi Alliance (2004, 2012). All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.osgi.framework;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Version identifier for capabilities such as bundles and packages.
 * <p>
 * Version identifiers have four components.
 * <ol>
 * <li>Major version. A non-negative integer.</li>
 * <li>Minor version. A non-negative integer.</li>
 * <li>Micro version. A non-negative integer.</li>
 * <li>Qualifier. A text string. See {@code Version(String)} for the format of the qualifier string.
 * </li>
 * </ol>
 * <p>
 * {@code Version} objects are immutable.
 * 
 * @since 1.3
 * @Immutable
 * @version $Id: a0b5a865f7fbf2b3dcb77a13b2e99da0b64702bb $
 */

public class Version implements Comparable<Version> {
  /**
   * Parses a version identifier from the specified string.
   * <p>
   * See {@code Version(String)} for the format of the version string.
   * 
   * @param version String representation of the version identifier. Leading and trailing whitespace
   *          will be ignored.
   * @return A {@code Version} object representing the version identifier. If {@code version} is
   *         {@code null} or the empty string then {@code emptyVersion} will be returned.
   * @throws IllegalArgumentException If {@code version} is improperly formatted.
   */
  public static Version parseVersion(String version) {
    if (version == null) {
      return emptyVersion;
    }

    version = version.trim();
    if (version.length() == 0) {
      return emptyVersion;
    }

    return new Version(version);
  }

  /**
   * Parse numeric component into an int.
   * 
   * @param value Numeric component
   * @param version Complete version string for exception message, if any
   * @return int value of numeric component
   */
  private static int parseInt(String value, String version) {
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      IllegalArgumentException iae = new IllegalArgumentException("invalid version \"" + version
          + "\": non-numeric \"" + value + "\"");
      iae.initCause(e);
      throw iae;
    }
  }

  private final int major;
  private final int minor;
  private final int micro;
  private final String qualifier;
  private static final String SEPARATOR = ".";

  private transient String versionString /* default to null */;

  private transient int hash /* default to 0 */;

  /**
   * The empty version "0.0.0".
   */
  public static final Version emptyVersion = new Version(0, 0, 0);

  /**
   * Creates a version identifier from the specified numerical components.
   * <p>
   * The qualifier is set to the empty string.
   * 
   * @param major Major component of the version identifier.
   * @param minor Minor component of the version identifier.
   * @param micro Micro component of the version identifier.
   * @throws IllegalArgumentException If the numerical components are negative.
   */
  public Version(int major, int minor, int micro) {
    this(major, minor, micro, null);
  }

  /**
   * Creates a version identifier from the specified components.
   * 
   * @param major Major component of the version identifier.
   * @param minor Minor component of the version identifier.
   * @param micro Micro component of the version identifier.
   * @param qualifier Qualifier component of the version identifier. If {@code null} is specified,
   *          then the qualifier will be set to the empty string.
   * @throws IllegalArgumentException If the numerical components are negative or the qualifier
   *           string is invalid.
   */
  public Version(int major, int minor, int micro, String qualifier) {
    if (qualifier == null) {
      qualifier = "";
    }

    this.major = major;
    this.minor = minor;
    this.micro = micro;
    this.qualifier = qualifier;
    validate();
  }

  /**
   * Creates a version identifier from the specified string.
   * <p>
   * Version string grammar:
   * 
   * <pre>
	 * version ::= major('.'minor('.'micro('.'qualifier)?)?)?
	 * major ::= digit+
	 * minor ::= digit+
	 * micro ::= digit+
	 * qualifier ::= (alpha|digit|'_'|'-')+
	 * digit ::= [0..9]
	 * alpha ::= [a..zA..Z]
	 * </pre>
   * 
   * @param version String representation of the version identifier. There must be no whitespace in
   *          the argument.
   * @throws IllegalArgumentException If {@code version} is improperly formatted.
   */
  public Version(String version) {
    int maj = 0;
    int min = 0;
    int mic = 0;
    String qual = "";

    try {
      StringTokenizer st = new StringTokenizer(version, SEPARATOR, true);
      maj = parseInt(st.nextToken(), version);

      if (st.hasMoreTokens()) { // minor
        st.nextToken(); // consume delimiter
        min = parseInt(st.nextToken(), version);

        if (st.hasMoreTokens()) { // micro
          st.nextToken(); // consume delimiter
          mic = parseInt(st.nextToken(), version);

          if (st.hasMoreTokens()) { // qualifier separator
            st.nextToken(); // consume delimiter
            qual = st.nextToken(""); // remaining string

            if (st.hasMoreTokens()) { // fail safe
              throw new IllegalArgumentException("invalid version \"" + version
                  + "\": invalid format");
            }
          }
        }
      }
    } catch (NoSuchElementException e) {
      IllegalArgumentException iae = new IllegalArgumentException("invalid version \"" + version
          + "\": invalid format");
      iae.initCause(e);
      throw iae;
    }

    major = maj;
    minor = min;
    micro = mic;
    qualifier = qual;
    validate();
  }

  /**
   * Compares this {@code Version} object to another {@code Version}.
   * <p>
   * A version is considered to be <b>less than</b> another version if its major component is less
   * than the other version's major component, or the major components are equal and its minor
   * component is less than the other version's minor component, or the major and minor components
   * are equal and its micro component is less than the other version's micro component, or the
   * major, minor and micro components are equal and it's qualifier component is less than the other
   * version's qualifier component (using {@code String.compareTo}).
   * <p>
   * A version is considered to be <b>equal to</b> another version if the major, minor and micro
   * components are equal and the qualifier component is equal (using {@code String.compareTo}).
   * 
   * @param other The {@code Version} object to be compared.
   * @return A negative integer, zero, or a positive integer if this version is less than, equal to,
   *         or greater than the specified {@code Version} object.
   * @throws ClassCastException If the specified object is not a {@code Version} object.
   */
  @Override
  public int compareTo(Version other) {
    if (other == this) { // quicktest
      return 0;
    }

    int result = major - other.major;
    if (result != 0) {
      return result;
    }

    result = minor - other.minor;
    if (result != 0) {
      return result;
    }

    result = micro - other.micro;
    if (result != 0) {
      return result;
    }

    return qualifier.compareTo(other.qualifier);
  }

  /**
   * Compares this {@code Version} object to another object.
   * <p>
   * A version is considered to be <b>equal to </b> another version if the major, minor and micro
   * components are equal and the qualifier component is equal (using {@code String.equals}).
   * 
   * @param object The {@code Version} object to be compared.
   * @return {@code true} if {@code object} is a {@code Version} and is equal to this object;
   *         {@code false} otherwise.
   */
  @Override
  public boolean equals(Object object) {
    if (object == this) { // quicktest
      return true;
    }

    if (!(object instanceof Version)) {
      return false;
    }

    Version other = (Version) object;
    return (major == other.major) && (minor == other.minor) && (micro == other.micro)
        && qualifier.equals(other.qualifier);
  }

  /**
   * Returns the major component of this version identifier.
   * 
   * @return The major component.
   */
  public int getMajor() {
    return major;
  }

  /**
   * Returns the micro component of this version identifier.
   * 
   * @return The micro component.
   */
  public int getMicro() {
    return micro;
  }

  /**
   * Returns the minor component of this version identifier.
   * 
   * @return The minor component.
   */
  public int getMinor() {
    return minor;
  }

  /**
   * Returns the qualifier component of this version identifier.
   * 
   * @return The qualifier component.
   */
  public String getQualifier() {
    return qualifier;
  }

  /**
   * Returns a hash code value for the object.
   * 
   * @return An integer which is a hash code value for this object.
   */
  @Override
  public int hashCode() {
    if (hash != 0) {
      return hash;
    }
    int h = 31 * 17;
    h = 31 * h + major;
    h = 31 * h + minor;
    h = 31 * h + micro;
    h = 31 * h + qualifier.hashCode();
    return hash = h;
  }

  /**
   * Returns the string representation of this version identifier.
   * <p>
   * The format of the version string will be {@code major.minor.micro} if qualifier is the empty
   * string or {@code major.minor.micro.qualifier} otherwise.
   * 
   * @return The string representation of this version identifier.
   */
  @Override
  public String toString() {
    return toString0();
  }

  /**
   * Internal toString behavior
   * 
   * @return The string representation of this version identifier.
   */
  String toString0() {
    if (versionString != null) {
      return versionString;
    }
    int q = qualifier.length();
    StringBuffer result = new StringBuffer(20 + q);
    result.append(major);
    result.append(SEPARATOR);
    result.append(minor);
    result.append(SEPARATOR);
    result.append(micro);
    if (q > 0) {
      result.append(SEPARATOR);
      result.append(qualifier);
    }
    return versionString = result.toString();
  }

  /**
   * Called by the Version constructors to validate the version components.
   * 
   * @throws IllegalArgumentException If the numerical components are negative or the qualifier
   *           string is invalid.
   */
  private void validate() {
    if (major < 0) {
      throw new IllegalArgumentException("invalid version \"" + toString0()
          + "\": negative number \"" + major + "\"");
    }
    if (minor < 0) {
      throw new IllegalArgumentException("invalid version \"" + toString0()
          + "\": negative number \"" + minor + "\"");
    }
    if (micro < 0) {
      throw new IllegalArgumentException("invalid version \"" + toString0()
          + "\": negative number \"" + micro + "\"");
    }
    for (char ch : qualifier.toCharArray()) {
      if (('A' <= ch) && (ch <= 'Z')) {
        continue;
      }
      if (('a' <= ch) && (ch <= 'z')) {
        continue;
      }
      if (('0' <= ch) && (ch <= '9')) {
        continue;
      }
      if ((ch == '_') || (ch == '-')) {
        continue;
      }
      throw new IllegalArgumentException("invalid version \"" + toString0()
          + "\": invalid qualifier \"" + qualifier + "\"");
    }
  }
}
