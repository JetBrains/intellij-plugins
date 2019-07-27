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
package org.jetbrains.osgi.jps.model;

import aQute.bnd.osgi.Constants;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A library bundlification rule contains a ruleset that is being used
 * when converting a plain java library to an OSGi bundle using the Bnd tool.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thomä</a>
 */
public class LibraryBundlificationRule {
  private String myRuleRegex = ".*";
  private String myAdditionalProperties = Constants.IMPORT_PACKAGE + ": *;resolution:=optional";
  private boolean myDoNotBundle = false;
  private boolean myStopAfterThisRule = false;
  private long myLastModified = System.currentTimeMillis();

  public String getRuleRegex() {
    return myRuleRegex;
  }

  public void setRuleRegex(String ruleRegex) {
    myRuleRegex = ruleRegex;
  }

  public String getAdditionalProperties() {
    return myAdditionalProperties;
  }

  public void setAdditionalProperties(String additionalProperties) {
    myAdditionalProperties = additionalProperties;
  }

  public boolean isDoNotBundle() {
    return myDoNotBundle;
  }

  public void setDoNotBundle(boolean doNotBundle) {
    myDoNotBundle = doNotBundle;
  }

  public boolean isStopAfterThisRule() {
    return myStopAfterThisRule;
  }

  public void setStopAfterThisRule(boolean stopAfterThisRule) {
    myStopAfterThisRule = stopAfterThisRule;
  }

  public long getLastModified() {
    return myLastModified;
  }

  @SuppressWarnings("UnusedDeclaration")
  public void setLastModified(long lastModified) {
    myLastModified = lastModified;
  }

  public LibraryBundlificationRule copy() {
    LibraryBundlificationRule result = new LibraryBundlificationRule();
    result.myAdditionalProperties = myAdditionalProperties;
    result.myRuleRegex = myRuleRegex;
    result.myDoNotBundle = myDoNotBundle;
    result.myStopAfterThisRule = myStopAfterThisRule;
    return result;
  }

  /**
   * Returns a map with properties to be added to the bundle manifest definition of all libraries which this rule applies to.
   */
  @Transient
  public Map<String, String> getAdditionalPropertiesMap() {
    try {
      Properties p = new Properties();
      p.load(new ByteArrayInputStream(myAdditionalProperties.getBytes(StandardCharsets.UTF_8)));

      Map<String, String> result = new HashMap<>();
      for (Map.Entry<Object, Object> entry : p.entrySet()) {
        result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
      }
      return result;
    }
    catch (IOException ignored) { }
    return Collections.emptyMap();
  }

  /**
   * Checks, if this rule applies to the given library name.
   */
  public boolean appliesTo(@NotNull String libraryName) {
    try {
      return ".*".equals(myRuleRegex) || (!StringUtil.isEmptyOrSpaces(myRuleRegex) && libraryName.matches(myRuleRegex));
    }
    catch (PatternSyntaxException ignored) { }
    return false;
  }

  public void validate() throws IllegalArgumentException {
    if (StringUtil.isEmptyOrSpaces(myRuleRegex)) {
      throw new IllegalArgumentException("Empty regex");
    }
    if (!".*".equals(myRuleRegex)) {
      //noinspection ResultOfMethodCallIgnored
      Pattern.compile(myRuleRegex);
    }

    if (!StringUtil.isEmptyOrSpaces(myAdditionalProperties)) {
      try {
        new Properties().load(new ByteArrayInputStream(myAdditionalProperties.getBytes(StandardCharsets.UTF_8)));
      }
      catch (Exception e) {
        throw new IllegalArgumentException("Malformed manifest entries");
      }
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    LibraryBundlificationRule rule = (LibraryBundlificationRule)o;

    if (myDoNotBundle != rule.myDoNotBundle) return false;
    if (myStopAfterThisRule != rule.myStopAfterThisRule) return false;
    if (!myAdditionalProperties.equals(rule.myAdditionalProperties)) return false;
    if (!myRuleRegex.equals(rule.myRuleRegex)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myRuleRegex.hashCode();
    result = 31 * result + myAdditionalProperties.hashCode();
    result = 31 * result + (myDoNotBundle ? 1 : 0);
    result = 31 * result + (myStopAfterThisRule ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Rule: " + myRuleRegex;
  }
}