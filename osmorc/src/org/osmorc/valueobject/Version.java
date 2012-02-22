/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.osmorc.valueobject;

import org.jetbrains.annotations.Nullable;

/**
 * Author: Robert F. Beeger (robert@beeger.net)
 */
public class Version implements Comparable<Version> {
  public Version(int major, int minor, int micro, @Nullable String qualifier) {

    _major = major;
    _minor = minor;
    _micro = micro;
    _qualifier = qualifier != null ? qualifier : "";
  }

  public int getMajor() {
    return _major;
  }

  public int getMinor() {
    return _minor;
  }

  public int getMicro() {
    return _micro;
  }

  public String getQualifier() {
    return _qualifier;
  }

  public int compareTo(Version o) {
    int result = getMajor() - o.getMajor();
    if (result == 0) {
      result = getMinor() - o.getMinor();
    }
    if (result == 0) {
      result = getMicro() - o.getMicro();
    }
    if (result == 0) {
      result = getQualifier().compareTo(o.getQualifier());
    }

    return result;
  }

  public String toString() {
    return _major + "." + _minor + "." + _micro + (_qualifier.length() > 0 ? "." + _qualifier : "");
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Version version = (Version)o;

    if (_major != version._major) {
      return false;
    }
    if (_micro != version._micro) {
      return false;
    }
    if (_minor != version._minor) {
      return false;
    }
    return _qualifier.equals(version._qualifier);
  }

  public int hashCode() {
    int result;
    result = _major;
    result = 31 * result + _minor;
    result = 31 * result + _micro;
    result = 31 * result + _qualifier.hashCode();
    return result;
  }

  private final int _major;
  private final int _minor;
  private final int _micro;
  private final String _qualifier;
}
