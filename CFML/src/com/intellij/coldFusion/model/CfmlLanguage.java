/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.coldFusion.model;

import com.intellij.lang.Language;
import com.intellij.psi.templateLanguages.TemplateLanguage;

/**
 * Created by Lera Nikolaenko
 * Date: 30.09.2008
 */
public class CfmlLanguage extends Language implements TemplateLanguage {
  public static final CfmlLanguage INSTANCE = new CfmlLanguage();

  public static final String CF8 = "cf8_tags.xml";
  public static final String CF9 = "tags.xml";
  /* todo: add tags and functions
  *  http://help.adobe.com/en_US/ColdFusion/10.0/CFMLRef/WSe9cbe5cf462523a0dd03b2c1223a399518-8000.html
  *  http://help.adobe.com/en_US/ColdFusion/10.0/CFMLRef/WS890819DC-DE4D-4b24-A237-6E3483E9D6A1.html
  * */
  public static final String CF10 = "cf10_tags.xml";
  public static final String RAILO = "Railo_tags.xml";

  private CfmlLanguage() {
    super("CFML");
  }
}
