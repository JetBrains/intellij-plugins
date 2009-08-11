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

import com.intellij.util.xmlb.annotations.Transient;
import com.jgoodies.binding.beans.Model;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A library bundlification rule contains a ruleset that is being used when converting a plain java library to an osgi
 * bundle using the bnd tool.
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class LibraryBundlificationRule extends Model
{
  public LibraryBundlificationRule()
  {
    addPropertyChangeListener(new PropertyChangeListener()
    {
      public void propertyChange(PropertyChangeEvent event)
      {
        // track last modification of the rule, so dependent packages
        // can be rebuilt when the rule has been changed.
        _lastModified = System.currentTimeMillis();
      }
    });
  }


  /**
   * @return a map containing additional manifest properties to be added to the bundle manifest definition of all
   *         libraries which this rule applies to.
   */
  @Transient
  public Map<String, String> getAdditionalPropertiesMap()
  {
    Map<String, String> result = new HashMap<String, String>();
    Properties p = new Properties();
    ByteArrayInputStream bais = new ByteArrayInputStream(getAdditionalProperties().getBytes());
    try
    {
      p.load(bais);
    }
    catch (IOException e)
    {
      // XXX: some real erorr msg here..
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }

    for (Enumeration e = p.propertyNames(); e.hasMoreElements();)
    {
      String name = (String) e.nextElement();
      result.put(name, p.getProperty(name));
    }
    return result;
  }

  public String getRuleRegex()
  {
    return _ruleRegex;
  }

  public void setRuleRegex(String ruleRegex)
  {
    String oldRegex = _ruleRegex;
    this._ruleRegex = ruleRegex;
    firePropertyChange("ruleRegex", oldRegex, _ruleRegex);
  }


  /**
   * Checks, if this rule applies to the  given library name.
   *
   * @param libraryName the name of the library to check against.
   * @return true, if this rule applies to the library, false otherwise.
   */
  public boolean appliesTo(@NotNull String libraryName)
  {
    try
    {
      // we skip compiling the pattern for .* regex as they match anything.
      return ".*".equals(getRuleRegex()) ||
          (!"".equals(getRuleRegex()) && Pattern.compile(getRuleRegex()).matcher(libraryName).matches());
    }
    catch (PatternSyntaxException e)
    {

      //XXX: not sure if it is good to silently ignore this here, might confuse the users
      return false;
    }
  }

  public String getAdditionalProperties()
  {
    return _additionalProperties;
  }

  public void setAdditionalProperties(String additionalProperties)
  {
    String old = _additionalProperties;
    _additionalProperties = additionalProperties;
    firePropertyChange("additionalProperties", old, _additionalProperties);
  }

  @Override
  public String toString()
  {
    return "Rule: " + _ruleRegex;
  }

  public long getLastModified()
  {
    return _lastModified;
  }

  public void setLastModified(long lastModified)
  {
    long old = _lastModified;
    _lastModified = lastModified;
    firePropertyChange("lastModified", old, lastModified);
  }

  public boolean isDoNotBundle()
  {
    return _doNotBundle;
  }

  public void setDoNotBundle(boolean doNotBundle)
  {
    boolean old = _doNotBundle;
    _doNotBundle = doNotBundle;
    firePropertyChange("doNotBundle", old, doNotBundle);
  }

  public void setStopAfterThisRule(boolean stopAfterThisRule) {
    boolean old = _stopAfterThisRule;
    _stopAfterThisRule = stopAfterThisRule;
    firePropertyChange("stopAfterThisRule", old, stopAfterThisRule);
  }

  public boolean isStopAfterThisRule() {
    return _stopAfterThisRule;
  }

  public LibraryBundlificationRule copy()
  {
    LibraryBundlificationRule result = new LibraryBundlificationRule();
    result._additionalProperties = _additionalProperties;
    result._ruleRegex = _ruleRegex;
    result._doNotBundle = _doNotBundle;
    result._stopAfterThisRule = _stopAfterThisRule;
    return result;
  }

  private String _additionalProperties = Constants.IMPORT_PACKAGE + ": *;resolution:=optional";
  private String _ruleRegex = ".*";
  private long _lastModified = System.currentTimeMillis();
  private boolean _doNotBundle = false;
  private boolean _stopAfterThisRule = false;
}