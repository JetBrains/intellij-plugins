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

package com.intellij.struts2;

import com.intellij.codeInspection.InspectionToolProvider;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.ide.IconProvider;
import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.intellij.javaee.ExternalResourceManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.jsp.JspFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.Param;
import com.intellij.struts2.dom.inspection.Struts2ModelInspection;
import com.intellij.struts2.dom.inspection.ValidatorConfigModelInspection;
import com.intellij.struts2.dom.inspection.ValidatorModelInspection;
import com.intellij.struts2.dom.struts.Bean;
import com.intellij.struts2.dom.struts.Constant;
import com.intellij.struts2.dom.struts.Include;
import com.intellij.struts2.dom.struts.StrutsRoot;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.ExceptionMapping;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.struts.strutspackage.*;
import com.intellij.struts2.dom.validator.Field;
import com.intellij.struts2.dom.validator.FieldValidator;
import com.intellij.struts2.dom.validator.Message;
import com.intellij.struts2.dom.validator.Validators;
import com.intellij.struts2.dom.validator.config.ValidatorConfig;
import com.intellij.struts2.dom.validator.config.ValidatorsConfig;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.struts2.facet.StrutsFacetType;
import com.intellij.ui.LayeredIcon;
import com.intellij.util.Icons;
import com.intellij.util.NullableFunction;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.ElementPresentationManager;
import com.intellij.util.xml.TypeNameManager;
import gnu.trove.TIntObjectHashMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Application-level support.
 * <p/>
 * <ul>
 * <li>StrutsFacet</li>
 * <li>External resources (DTDs)</li>
 * <li>DOM Icons/presentation</li>
 * <li>Inspections</li>
 * <li>Icons for classes/config files</li>
 * </ul>
 *
 * @author Yann CŽbron
 */
public class StrutsApplicationComponent implements ApplicationComponent,
                                                   FileTemplateGroupDescriptorFactory,
                                                   InspectionToolProvider,
                                                   IconProvider {

  @NonNls
  @NotNull
  public String getComponentName() {
    return "Struts2ApplicationComponent";
  }

  public void initComponent() {
    FacetTypeRegistry.getInstance().registerFacetType(StrutsFacetType.INSTANCE);

    initExternalResources();

    registerStrutsDomPresentation();
    registerValidationDomPresentation();
  }

  public void disposeComponent() {
  }

  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor("Struts 2", StrutsIcons.ACTION);
    group.addTemplate(new FileTemplateDescriptor(StrutsConstants.STRUTS_DEFAULT_FILENAME,
                                                 StrutsIcons.STRUTS_CONFIG_FILE_ICON));
    group.addTemplate(new FileTemplateDescriptor("validator.xml", StrutsIcons.VALIDATION_CONFIG_FILE_ICON));
    return group;
  }

  public Class[] getInspectionClasses() {
    return new Class[]{Struts2ModelInspection.class, ValidatorModelInspection.class, ValidatorConfigModelInspection.class};
  }

  // IconProvider -------------------------------------------------------------
  // original code posted by Sascha Weinreuter

  private boolean active;
  private static final Key<TIntObjectHashMap<Icon>> ICON_KEY = Key.create("STRUTS2_OVERLAY_ICON");

  @Nullable
  public Icon getIcon(@NotNull final PsiElement element, final int flags) {

    // for getting the original icon from IDEA
    if (active) {
      return null;
    }

    if (element instanceof JspFile) {
      return null;
    }
    if (!(element instanceof PsiClass || element instanceof XmlFile)) {
      return null;
    }

    // IconProvider queries non-physical PSI as well (e.g. completion items); check validity
    if (!element.isPhysical() ||
        !element.isValid()) {
      return null;
    }

    // no icons when no facet present
    final StrutsFacet strutsFacet = StrutsFacet.getInstance(element);
    if (strutsFacet == null) {
      return null;
    }

    active = true;

    try {
      TIntObjectHashMap<Icon> icons = element.getUserData(ICON_KEY);
      if (icons != null) {
        final Icon icon = icons.get(flags);
        if (icon != null) {
          return icon;
        }
      }

      Icon strutsIcon = null;
      LayeredIcon icon = null;

      // handle XML files
      if (element instanceof XmlFile) {
        final XmlFile xmlFile = (XmlFile) element;
        final DomManager domManager = DomManager.getDomManager(xmlFile.getProject());

        if (domManager.getFileElement(xmlFile, StrutsRoot.class) != null) {
          strutsIcon = StrutsIcons.ACTION_SMALL;
        } else if (domManager.getFileElement(xmlFile, Validators.class) != null) {
          strutsIcon = StrutsIcons.VALIDATOR_SMALL;
        } else if (domManager.getFileElement(xmlFile, ValidatorsConfig.class) != null) {
          strutsIcon = StrutsIcons.VALIDATOR_SMALL;
        }
      }
      // handle JAVA classes
      else {
        final PsiClass psiClass = (PsiClass) element;
        final Module module = ModuleUtil.findModuleForPsiElement(psiClass);
        final StrutsModel strutsModel = StrutsManager.getInstance(psiClass.getProject()).getCombinedModel(module);
        if (strutsModel != null &&
            !strutsModel.findActionsByClass(psiClass).isEmpty()) {
          strutsIcon = StrutsIcons.ACTION_SMALL;
        }
      }

      // match? build new layered icon
      if (strutsIcon != null) {
        icon = new LayeredIcon(2);
        final Icon original = element.getIcon(flags & ~Iconable.ICON_FLAG_VISIBILITY);
        icon.setIcon(original, 0);
        icon.setIcon(strutsIcon, 1, 0, StrutsIcons.SMALL_ICON_Y_OFFSET);
      }

      // cache built icon
      if (icon != null) {
        if (icons == null) {
          element.putUserData(ICON_KEY, icons = new TIntObjectHashMap<Icon>(3));
        }
        icons.put(flags, icon);
      }

      return icon;
    } finally {
      active = false;
    }

  }

  private static void registerStrutsDomPresentation() {
    // <struts>
    ElementPresentationManager.registerIcon(StrutsRoot.class, StrutsIcons.STRUTS_CONFIG_FILE_ICON);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        return o instanceof StrutsRoot ? ((StrutsRoot) o).getRoot().getFile().getName() : null;
      }
    });

    // <exception-mapping>
    ElementPresentationManager.registerIcon(ExceptionMapping.class, StrutsIcons.EXCEPTION_MAPPING);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (o instanceof ExceptionMapping) {
          final PsiClass exceptionClass = ((ExceptionMapping) o).getExceptionClass().getValue();
          if (exceptionClass != null) {
            return exceptionClass.getName();
          }
          return ((ExceptionMapping) o).getName().getStringValue();
        }
        return null;
      }
    });

    // global <exception-mapping>
    ElementPresentationManager.registerIcon(GlobalExceptionMapping.class, StrutsIcons.GLOBAL_EXCEPTION_MAPPING);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (o instanceof GlobalExceptionMapping) {
          final PsiClass exceptionClass = ((GlobalExceptionMapping) o).getExceptionClass().getValue();
          if (exceptionClass != null) {
            return exceptionClass.getName();
          }
          return ((GlobalExceptionMapping) o).getName().getStringValue();
        }
        return null;
      }
    });

    // <interceptor-ref>
    ElementPresentationManager.registerIconProvider(new NullableFunction<Object, Icon>() {
      public Icon fun(final Object o) {
        if (o instanceof InterceptorRef) {
          final InterceptorOrStackBase interceptorOrStackBase = ((InterceptorRef) o).getName().getValue();
          if (interceptorOrStackBase instanceof Interceptor) {
            return StrutsIcons.INTERCEPTOR;
          } else if (interceptorOrStackBase instanceof InterceptorStack) {
            return StrutsIcons.INTERCEPTOR_STACK;
          }
        }
        return null;
      }
    });

    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        return o instanceof InterceptorRef ? ((InterceptorRef) o).getName().getStringValue() : null;
      }
    });

    ElementPresentationManager.registerIcon(DefaultInterceptorRef.class, StrutsIcons.DEFAULT_INTERCEPTOR_REF);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        return o instanceof DefaultInterceptorRef ? ((DefaultInterceptorRef) o).getName().getStringValue() : null;
      }
    });

    // <include>
    ElementPresentationManager.registerIcon(Include.class, StrutsIcons.INCLUDE);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        return o instanceof Include ? ((Include) o).getFile().getStringValue() : null;
      }
    });

    // <result>
    ElementPresentationManager.registerIcon(Result.class, StrutsIcons.RESULT);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (o instanceof Result) {
          final String resultName = ((Result) o).getName().getStringValue();
          return resultName != null ? resultName : "success";
        }
        return null;
      }
    });

    // <global-result>
    ElementPresentationManager.registerIcon(GlobalResult.class, StrutsIcons.GLOBAL_RESULT);
    TypeNameManager.registerTypeName(GlobalResult.class, "global result");
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (o instanceof GlobalResult) {
          final String globalResultName = ((GlobalResult) o).getName().getStringValue();
          return globalResultName != null ? globalResultName : "success";
        }
        return null;
      }
    });

    // <default-action-ref>
    ElementPresentationManager.registerIcon(DefaultActionRef.class, StrutsIcons.DEFAULT_ACTION_REF);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (o instanceof DefaultActionRef) {
          return ((DefaultActionRef) o).getName().getStringValue();
        }
        return null;
      }
    });

    // <default-class-ref>
    ElementPresentationManager.registerIcon(DefaultClassRef.class, StrutsIcons.DEFAULT_CLASS_REF);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (o instanceof DefaultClassRef) {
          return ((DefaultClassRef) o).getDefaultClass().getStringValue();
        }
        return null;
      }
    });

    ElementPresentationManager.registerIcon(Action.class, StrutsIcons.ACTION);
    ElementPresentationManager.registerIcon(Bean.class, StrutsIcons.BEAN);
    ElementPresentationManager.registerIcon(Constant.class, Icons.PARAMETER_ICON);
    ElementPresentationManager.registerIcon(Interceptor.class, StrutsIcons.INTERCEPTOR);
    ElementPresentationManager.registerIcon(InterceptorStack.class, StrutsIcons.INTERCEPTOR_STACK);
    ElementPresentationManager.registerIcon(Param.class, StrutsIcons.PARAM);
    ElementPresentationManager.registerIcon(ResultType.class, StrutsIcons.RESULT_TYPE);
    ElementPresentationManager.registerIcon(StrutsPackage.class, StrutsIcons.PACKAGE);
  }

  private static void registerValidationDomPresentation() {
    ElementPresentationManager.registerIcon(ValidatorConfig.class, StrutsIcons.VALIDATOR);

    ElementPresentationManager.registerIcon(Validators.class, StrutsIcons.VALIDATION_CONFIG_FILE_ICON);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        return o instanceof Validators ? ((Validators) o).getRoot().getFile().getName() : null;
      }
    });

    // <field>
    ElementPresentationManager.registerIcon(Field.class, Icons.FIELD_ICON);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        return o instanceof Field ? ((Field) o).getName().getStringValue() : null;
      }
    });

    // <field-validator>
    ElementPresentationManager.registerIcon(FieldValidator.class, StrutsIcons.VALIDATOR);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (!(o instanceof FieldValidator)) {
          return null;
        }
        final ValidatorConfig validatorConfig = ((FieldValidator) o).getType().getValue();
        return validatorConfig != null ? validatorConfig.getName().getStringValue() : null;
      }
    });

    // <message>
    ElementPresentationManager.registerIcon(Message.class, StrutsIcons.MESSAGE);
    ElementPresentationManager.registerNameProvider(new NullableFunction<Object, String>() {
      public String fun(final Object o) {
        if (!(o instanceof Message)) {
          return null;
        }

        final Message message = (Message) o;
        final String key = message.getKey().getStringValue();
        return !StringUtil.isEmpty(key) ? key : message.getValue();
      }
    });
  }

  /**
   * Adds all Struts2-related DTDs to the available external resources.
   */
  private static void initExternalResources() {
    addDTDResource(StrutsConstants.STRUTS_2_0_DTD_URI,
                   StrutsConstants.STRUTS_2_0_DTD_ID,
                   "/resources/dtds/struts-2.0.dtd");

    addDTDResource(StrutsConstants.XWORK_DTD_URI,
                   StrutsConstants.XWORK_DTD_ID,
                   "/resources/dtds/xwork-2.0.dtd");

    addDTDResource(StrutsConstants.VALIDATOR_1_00_DTD_URI,
                   StrutsConstants.VALIDATOR_1_00_DTD_ID,
                   "/resources/dtds/xwork-validator-1.0.dtd");

    addDTDResource(StrutsConstants.VALIDATOR_1_02_DTD_URI,
                   StrutsConstants.VALIDATOR_1_02_DTD_ID,
                   "/resources/dtds/xwork-validator-1.0.2.dtd");

    addDTDResource(StrutsConstants.VALIDATOR_CONFIG_DTD_URI,
                   StrutsConstants.VALIDATOR_CONFIG_DTD_ID,
                   "/resources/dtds/xwork-validator-config-1.0.dtd");
  }

  /**
   * Adds a DTD resource from local classpath.
   *
   * @param uri       Resource URI.
   * @param id        Resource ID.
   * @param localFile Local path to resource.
   */
  private static void addDTDResource(final String uri, final String id, final String localFile) {
    ExternalResourceManager.getInstance().addStdResource(uri, localFile, StrutsApplicationComponent.class);
    ExternalResourceManager.getInstance().addStdResource(id, localFile, StrutsApplicationComponent.class);
  }

}