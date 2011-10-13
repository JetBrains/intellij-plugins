/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea;

import com.intellij.CommonBundle;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * Provides localized messages via the appropriate Idea message bundle loader.
 * @author alexeagle@google.com (Alex Eagle)
 */
public class MessageBundle {
  private static Reference<ResourceBundle> bundle;

  @NonNls
  private static final String BUNDLE = "com.google.jstestdriver.idea.MessageBundle";

  private MessageBundle() {
  }

  public static String message(@NonNls @PropertyKey(resourceBundle = BUNDLE) String key,
                               Object... params) {
    return CommonBundle.message(MessageBundle.getBundle(), key, params);
  }

  public static ResourceBundle getBundle() {
    ResourceBundle bundle = null;
    if (MessageBundle.bundle != null) {
      bundle = MessageBundle.bundle.get();
    }
    if (bundle == null) {
      bundle = ResourceBundle.getBundle(MessageBundle.BUNDLE);
      MessageBundle.bundle = new SoftReference<ResourceBundle>(bundle);
    }
    return bundle;
  }
}
