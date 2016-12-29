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
package org.osmorc.frameworkintegration;

import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class FrameworkInstanceDefinition extends UserDataHolderBase {
  private String myFrameworkIntegratorName;
  private String myName;
  private String myVersion;
  private String myBaseFolder;

  public String getFrameworkIntegratorName() {
    return myFrameworkIntegratorName;
  }

  public void setFrameworkIntegratorName(@NotNull String frameworkIntegratorName) {
    myFrameworkIntegratorName = frameworkIntegratorName;
  }

  public String getName() {
    return myName;
  }

  public void setName(@NotNull String name) {
    myName = name;
  }

  public String getVersion() {
    return myVersion;
  }

  public void setVersion(String version) {
    myVersion = version;
  }

  public String getBaseFolder() {
    return myBaseFolder;
  }

  public void setBaseFolder(String baseFolder) {
    myBaseFolder = baseFolder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    FrameworkInstanceDefinition that = (FrameworkInstanceDefinition)o;

    if (myName != null ? !myName.equals(that.myName) : that.myName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myName != null ? myName.hashCode() : 0;
  }

  @Override
  public String toString() {
    String description = myName != null ? myName : "undefined";
    String frameworkIntegrator = myFrameworkIntegratorName != null ? myFrameworkIntegratorName : "undefined";
    String version = !StringUtil.isEmptyOrSpaces(myVersion) ? " [" + myVersion + "]" : " [latest]";
    return description + " (" + frameworkIntegrator + ")" + version;
  }
}
