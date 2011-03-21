/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.dom.struts;

import com.intellij.ide.presentation.Presentation;
import com.intellij.struts2.Struts2DomIconProvider;
import com.intellij.struts2.dom.StrutsDomConstants;
import com.intellij.struts2.dom.struts.constant.Constant;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * <code>struts</code> root-element for struts.xml-files.
 *
 * @author Yann C&eacute;bron
 */
@Namespace(StrutsDomConstants.STRUTS_NAMESPACE_KEY)
@Presentation(typeName = "Struts Root", iconProviderClass = Struts2DomIconProvider.class)
public interface StrutsRoot extends DomElement {

  @NonNls
  String TAG_NAME = "struts";

  GenericAttributeValue<Integer> getOrder();

  @SubTagList(value = "package")
  @NotNull
  List<StrutsPackage> getPackages();

  @SubTagList(value = "include")
  List<Include> getIncludes();

  @SubTagList(value = "bean")
  @NotNull
  List<Bean> getBeans();

  @SubTagList(value = "constant")
  @NotNull
  List<Constant> getConstants();

  @Nullable
  UnknownHandlerStack getUnknownHandlerStack();

}