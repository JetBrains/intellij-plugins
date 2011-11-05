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

package com.intellij.struts2;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.paths.PathReference;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.NullableFunction;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Application-level support.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsApplicationComponent implements ApplicationComponent {

  @NonNls
  @NotNull
  public String getComponentName() {
    return "Struts2ApplicationComponent";
  }

  public void initComponent() {
    // TODO remove, this should not be needed --> DOM unique name highlighting not working
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      @Override
      public String fun(final Object o) {
        if (o instanceof Result) {
          final String resultName = ((Result) o).getName().getStringValue();
          return resultName != null ? resultName : Result.DEFAULT_NAME;
        }
        return null;
      }
    });

    registerDocumentationProviders();
  }

  public void disposeComponent() {
  }

  private static void registerDocumentationProviders() {
    ElementPresentationManager.registerDocumentationProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (o instanceof Action) {
          final Action action = (Action) o;
          final StrutsPackage strutsPackage = action.getStrutsPackage();

          final DocumentationBuilder builder = new DocumentationBuilder();
          final PsiClass actionClass = action.searchActionClass();
          builder.addLine("Action", action.getName().getStringValue())
                 .addLine("Class", actionClass != null ? actionClass.getQualifiedName() : null)
                 .addLine("Method", action.getMethod().getStringValue())
                 .addLine("Package", strutsPackage.getName().getStringValue())
                 .addLine("Namespace", strutsPackage.searchNamespace());

          return builder.getText();
        }

        if (o instanceof Result) {
          final Result result = (Result) o;
          final PathReference pathReference = result.getValue();
          final String displayPath = pathReference != null ? pathReference.getPath() : "???";
          final ResultType resultType = result.getEffectiveResultType();
          final String resultTypeValue = resultType != null ? resultType.getName().getStringValue() : "???";

          final DocumentationBuilder builder = new DocumentationBuilder();
          builder.addLine("Path", displayPath)
                 .addLine("Type", resultTypeValue);
          return builder.getText();
        }

        return null;
      }
    });
  }

  /**
   * Builds HTML-table based descriptions for use in documentation, tooltips.
   *
   * @author Yann C&eacute;bron
   */
  private static class DocumentationBuilder {

    @NonNls
    private final StringBuilder builder = new StringBuilder("<html><table>");

    /**
     * Adds a labeled content line.
     *
     * @param label   Content description.
     * @param content Content text, {@code null} or empty text will be replaced with '-'.
     * @return this instance.
     */
    private DocumentationBuilder addLine(@NotNull @NonNls final String label, @Nullable @NonNls final String content) {
      builder.append("<tr><td><strong>").append(label).append(":</strong></td>")
             .append("<td>").append(StringUtil.isNotEmpty(content) ? content : "-").append("</td></tr>");
      return this;
    }

    private String getText() {
      builder.append("</table></html>");
      return builder.toString();
    }
  }
}
