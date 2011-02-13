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
import com.intellij.struts2.dom.struts.Include;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.ExceptionMapping;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.strutspackage.*;
import com.intellij.struts2.dom.validator.Field;
import com.intellij.struts2.dom.validator.FieldValidator;
import com.intellij.struts2.dom.validator.Message;
import com.intellij.struts2.dom.validator.Validators;
import com.intellij.struts2.dom.validator.config.ValidatorConfig;
import com.intellij.util.NullableFunction;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

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
    registerStrutsDomPresentation();
    registerValidationDomPresentation();

    registerDocumentationProviders();
  }

  public void disposeComponent() {
  }

  /**
   * Provides display name for subclass(es) of given DomElement-type.
   *
   * @param <T> DomElement-type to provide names for.
   */
  private abstract static class TypedNameProvider<T extends DomElement> {

    private final Class<T> clazz;

    private TypedNameProvider(final Class<T> clazz) {
      this.clazz = clazz;
    }

    private Class<T> getClazz() {
      return clazz;
    }

    @Nullable
    protected abstract String getDisplayName(T t);

  }

  /**
   * Provides registry and mapping for multiple {@link TypedNameProvider}s.
   */
  private static class TypedNameProviderRegistry implements NullableFunction<Object, String> {

    private final Map<Class, TypedNameProvider> typedNameProviderSet = new HashMap<Class, TypedNameProvider>();

    private void addTypedNameProvider(final TypedNameProvider nameProvider) {
      typedNameProviderSet.put(nameProvider.getClazz(), nameProvider);
    }

    public String fun(final Object o) {
      for (final Map.Entry<Class, TypedNameProvider> entry : typedNameProviderSet.entrySet()) {
        if (entry.getKey().isAssignableFrom(o.getClass())) {
          //noinspection unchecked
          return entry.getValue().getDisplayName((DomElement) o);
        }
      }

      return null;
    }

  }

  private static void registerStrutsDomPresentation() {
    final TypedNameProviderRegistry nameProviderRegistry = new TypedNameProviderRegistry();

    // <struts>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<StrutsRoot>(StrutsRoot.class) {
      protected String getDisplayName(final StrutsRoot strutsRoot) {
        return DomUtil.getFile(strutsRoot).getName();
      }
    });


    // <exception-mapping>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<ExceptionMapping>(ExceptionMapping.class) {
      protected String getDisplayName(final ExceptionMapping exceptionMapping) {
        final PsiClass exceptionClass = exceptionMapping.getExceptionClass().getValue();
        if (exceptionClass != null) {
          return exceptionClass.getName();
        }
        return exceptionMapping.getName().getStringValue();
      }
    });

    // global <exception-mapping>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<GlobalExceptionMapping>(GlobalExceptionMapping.class) {
      protected String getDisplayName(final GlobalExceptionMapping globalExceptionMapping) {
        final PsiClass exceptionClass = globalExceptionMapping.getExceptionClass().getValue();
        if (exceptionClass != null) {
          return exceptionClass.getName();
        }
        return globalExceptionMapping.getName().getStringValue();
      }
    });

    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<InterceptorRef>(InterceptorRef.class) {
      protected String getDisplayName(final InterceptorRef interceptorRef) {
        return interceptorRef.getName().getStringValue();
      }
    });

    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<DefaultInterceptorRef>(DefaultInterceptorRef.class) {
      protected String getDisplayName(final DefaultInterceptorRef defaultInterceptorRef) {
        return defaultInterceptorRef.getName().getStringValue();
      }
    });

    // <include>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<Include>(Include.class) {
      protected String getDisplayName(final Include include) {
        return include.getFile().getStringValue();
      }
    });

    // <result>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<Result>(Result.class) {
      protected String getDisplayName(final Result result) {
        final String resultName = result.getName().getStringValue();
        return resultName != null ? resultName : Result.DEFAULT_NAME;
      }
    });

    // <global-result>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<GlobalResult>(GlobalResult.class) {
      protected String getDisplayName(final GlobalResult globalResult) {
        final String globalResultName = globalResult.getName().getStringValue();
        return globalResultName != null ? globalResultName : Result.DEFAULT_NAME;
      }
    });

    // <default-action-ref>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<DefaultActionRef>(DefaultActionRef.class) {
      protected String getDisplayName(final DefaultActionRef defaultActionRef) {
        return defaultActionRef.getName().getStringValue();
      }
    });

    // <default-class-ref>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<DefaultClassRef>(DefaultClassRef.class) {
      protected String getDisplayName(final DefaultClassRef defaultClassRef) {
        return defaultClassRef.getDefaultClass().getStringValue();
      }
    });

    // register central name provider
    ElementPresentationManager.registerNameProvider(nameProviderRegistry);
  }

  private static void registerValidationDomPresentation() {
    final TypedNameProviderRegistry nameProviderRegistry = new TypedNameProviderRegistry();

    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<Validators>(Validators.class) {
      protected String getDisplayName(final Validators validators) {
        return DomUtil.getFile(validators).getName();
      }
    });

    // <field>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<Field>(Field.class) {
      protected String getDisplayName(final Field field) {
        return field.getName().getStringValue();
      }
    });

    // <field-validator>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<FieldValidator>(FieldValidator.class) {
      protected String getDisplayName(final FieldValidator fieldValidator) {
        final ValidatorConfig validatorConfig = fieldValidator.getType().getValue();
        return validatorConfig != null ? validatorConfig.getName().getStringValue() : null;
      }
    });

    // <message>
    nameProviderRegistry.addTypedNameProvider(new TypedNameProvider<Message>(Message.class) {
      protected String getDisplayName(final Message message) {
        final String key = message.getKey().getStringValue();
        return StringUtil.isNotEmpty(key) ? key : message.getValue();
      }
    });

    // register central name provider
    ElementPresentationManager.registerNameProvider(nameProviderRegistry);
  }

  private static void registerDocumentationProviders() {
    ElementPresentationManager.registerDocumentationProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (o instanceof Action) {
          final Action action = (Action) o;
          final StrutsPackage strutsPackage = action.getStrutsPackage();

          final DocumentationBuilder builder = new DocumentationBuilder();
          builder.addLine("Action", action.getName().getStringValue())
              .addLine("Class", action.getActionClass().getStringValue())
              .addLine("Method", action.getMethod().getStringValue())
              .addLine("Package", strutsPackage.getName().getStringValue())
              .addLine("Namespace", strutsPackage.getNamespace().getStringValue());

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
