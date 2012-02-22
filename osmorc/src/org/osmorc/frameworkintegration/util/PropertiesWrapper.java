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

package org.osmorc.frameworkintegration.util;

import com.jgoodies.binding.beans.Model;
import org.jetbrains.annotations.NotNull;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A generic property handler which fires property change events. The handler also takes care of the property type
 * (String or boolean).
 *
 * @author <a href="mailto:janthomae@janthomae.de">Jan Thom&auml;</a>
 * @version $Id:$
 */
public class PropertiesWrapper extends Model {

  public PropertiesWrapper(Map<String, String> properties) {
    this._properties = new HashMap<String, String>();
    // load the default values
    loadDefaults();
    // then overwrite them with custom settings
    _properties.putAll(properties);
  }


  protected String getProperty(@NotNull String name) {
    return _properties.get(name);
  }

  protected void putProperty(@NotNull String name, String value) {
    if (isBooleanProperty(name)) {
      putBooleanProperty(name, Boolean.parseBoolean(value));
    }
    else {
      String old = getProperty(name);
      _properties.put(name, value);
      firePropertyChange(name, old, value);
    }
  }

  protected boolean getBooleanProperty(@NotNull String name) {
    return Boolean.parseBoolean(getProperty(name));
  }

  protected void putBooleanProperty(@NotNull String name, boolean value) {
    boolean old = getBooleanProperty(name);
    _properties.put(name, String.valueOf(value));
    firePropertyChange(name, old, value);
  }

  public void load(Map<String, String> additionalProperties) {
    for (String key : additionalProperties.keySet()) {
      putProperty(key, additionalProperties.get(key));
    }
  }

  public Map<String, String> getProperties() {
    return Collections.unmodifiableMap(_properties);
  }

  private boolean isBooleanProperty(String name) {
    try {
      PropertyDescriptor pd = new PropertyDescriptor(name, getClass());
      return Boolean.class.isAssignableFrom(pd.getPropertyType()) ||
             Boolean.TYPE.isAssignableFrom(pd.getPropertyType());
    }
    catch (IntrospectionException e) {
      return false;
    }
  }

  protected void loadDefaults() {
  }

  private final Map<String, String> _properties;
}
