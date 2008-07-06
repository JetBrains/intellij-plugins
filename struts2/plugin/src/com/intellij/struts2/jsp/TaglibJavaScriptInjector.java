/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.jsp;

import com.intellij.lang.Language;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.reference.ReferenceFilters;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Adds JavaScript support for Struts UI tags.
 *
 * @author Yann C&eacute;bron
 */
@SuppressWarnings({"ComponentNotRegistered"})
public class TaglibJavaScriptInjector extends AbstractProjectComponent implements MultiHostInjector {

  private Language myJavascriptLanguage;

  public TaglibJavaScriptInjector(final Project project) {
    super(project);
  }

  public void getLanguagesToInject(@NotNull final MultiHostRegistrar registrar, @NotNull final PsiElement host) {
    // operate only in JSP(X) files
    final FileType fileType = host.getContainingFile().getFileType();
    if (fileType != StdFileTypes.JSP &&
        fileType != StdFileTypes.JSPX) {
      return;
    }

    if (ReferenceFilters.NAMESPACE_TAGLIB_STRUTS_UI.isAcceptable(host.getParent().getParent(), null)) {
      @NonNls final String name = ((XmlAttribute) host.getParent()).getName();
      if (name.startsWith("on")) {
        final Language language = myJavascriptLanguage;
        final TextRange range = new TextRange(1, host.getTextLength() - 1);
        registrar.startInjecting(language)
          .addPlace(null, null, (PsiLanguageInjectionHost) host, range)
          .doneInjecting();
      }
    }
  }

  @NotNull
  public List<? extends Class<? extends PsiElement>> elementsToInjectIn() {
    return Arrays.asList(XmlAttributeValue.class);
  }

  public void initComponent() {
    final FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension("js");
    if (!StdFileTypes.UNKNOWN.equals(fileType) && fileType instanceof LanguageFileType) {
      myJavascriptLanguage = ((LanguageFileType) fileType).getLanguage();
      InjectedLanguageManager.getInstance(myProject).registerMultiHostInjector(this);
    } 
  }

}