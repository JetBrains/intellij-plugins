package com.intellij.tapestry.intellij.util;

import com.intellij.facet.FacetManager;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.xml.*;
import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.model.presentation.InjectedElement;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.TemplateElement;
import com.intellij.tapestry.core.util.PathUtils;
import com.intellij.tapestry.intellij.TapestryModuleSupportLoader;
import com.intellij.tapestry.intellij.core.java.IntellijJavaClassType;
import com.intellij.tapestry.intellij.facet.TapestryFacetType;
import com.intellij.tapestry.intellij.lang.descriptor.TapestryXmlExtension;
import com.intellij.tapestry.lang.TmlFileType;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility methods related to Tapestry.
 */
public final class TapestryUtils {

  private static final Logger _logger = Logger.getInstance(TapestryUtils.class.getName());

  /**
   * Checks if a module is a Tapestry module.
   *
   * @param module the module to check.
   * @return {@code true} if the module is a Tapestry module, {@code false} otherwise.
   */
  public static boolean isTapestryModule(Module module) {
    return module != null && FacetManager.getInstance(module).getFacetsByType(TapestryFacetType.ID).size() > 0;
  }

  /**
   * Finds all module with Tapestry support in a project.
   *
   * @param project the project to look for Tapestry modules in.
   * @return all modules in the given project with Tapestry support.
   */
  public static Module[] getAllTapestryModules(Project project) {
    final Module[] modules = ModuleManager.getInstance(project).getModules();
    List<Module> result = new ArrayList<>();

    for (Module module : modules) {
      if (isTapestryModule(module)) {
        result.add(module);
      }
    }

    return result.toArray(Module.EMPTY_ARRAY);
  }

  /**
   * Finds the element in a Tapestry component tag that identifies the type of component.
   *
   * @param tag the component tag.
   * @return the attribute that identifies the type of component.
   */
  @Nullable
  public static XmlElement getComponentIdentifier(@Nullable final XmlTag tag) {
    return tag == null ? null : TapestryXmlExtension.isTapestryTemplateNamespace(tag.getNamespace()) // embedded components
                                ? IdeaUtils.getNameElement(tag) // using invisible instrumentation
                                : getIdentifyingAttribute(tag);
  }

  @Nullable
  public static XmlAttribute getIdentifyingAttribute(@NotNull XmlTag tag) {
    XmlAttribute typeAttribute = getTTypeAttribute(tag);
    return typeAttribute != null ? typeAttribute : getTIdAttribute(tag);
  }

  @Nullable
  public static XmlAttribute getTIdAttribute(XmlTag tag) {
    return tag.getAttribute("id", TapestryXmlExtension.getTapestryNamespace(tag));
  }

  @Nullable
  public static XmlAttribute getTTypeAttribute(XmlTag tag) {
    return tag.getAttribute("type", TapestryXmlExtension.getTapestryNamespace(tag));
  }

  /**
   * Verify the existence of parameter declaration in elementClass
   *
   * @param paramName    the parameter name to check
   * @param elementClass the class to get the fields
   * @param tag          the component to get the parameters
   * @return {@code true} if the parameter is defined in the class, {@code false} otherwise.
   */
  public static boolean parameterDefinedInClass(String paramName, IntellijJavaClassType elementClass, XmlTag tag) {

    IJavaField field = findIdentifyingField(elementClass, tag);
    if (field == null) return false;

    final IJavaAnnotation annotation = field.getAnnotations().get(TapestryConstants.COMPONENT_ANNOTATION);
    String[] fieldParameters = annotation.getParameters().get("parameters");
    if (fieldParameters == null) return false;
    for (String fieldParameter : fieldParameters) {
      final String[] paramNameValue = fieldParameter.split("=");
      if (paramNameValue.length == 2 && paramNameValue[0].equals(paramName)) return true;
    }
    return false;
  }

  @Nullable
  public static String getFieldId(IJavaField field) {
    final IJavaAnnotation annotation = field.getAnnotations().get(TapestryConstants.COMPONENT_ANNOTATION);
    if (annotation == null) return null;
    String[] fieldIds = annotation.getParameters().get("id");
    return fieldIds != null && fieldIds.length > 0 && fieldIds[0] != null && fieldIds[0].length() > 0 ? fieldIds[0] : field.getName();
  }

  @Nullable
  public static IJavaField findIdentifyingField(XmlTag tag) {
    final TapestryProject tapestryProject = getTapestryProject(tag);
    if (tapestryProject == null) return null;
    PresentationLibraryElement element = tapestryProject.findElementByTemplate(tag.getContainingFile());
    return element != null ? findIdentifyingField((IntellijJavaClassType)element.getElementClass(), tag) : null;
  }

  @NotNull
  public static List<String> getEmbeddedComponentIds(XmlTag tag) {
    final TapestryProject tapestryProject = getTapestryProject(tag);
    if (tapestryProject == null) return Collections.emptyList();
    PresentationLibraryElement element = tapestryProject.findElementByTemplate(tag.getContainingFile());
    if (element == null) return Collections.emptyList();
    List<String> embeddedIds = new ArrayList<>();
    for (TemplateElement injectedElement : element.getEmbeddedComponents()) {
      ContainerUtil.addIfNotNull(embeddedIds, injectedElement.getElement().getElementId());
    }
    return embeddedIds;
  }

  @Nullable
  private static IJavaField findIdentifyingField(IntellijJavaClassType elementClass, XmlTag tag) {
    final String tagId = tag.getAttributeValue("id", TapestryXmlExtension.getTapestryNamespace(tag));
    if (tagId == null) return null;
    for (IJavaField field : elementClass.getFields(false).values()) {
      if (tagId.equals(getFieldId(field))) return field;
    }
    return null;
  }

  @Nullable
  public static TapestryProject getTapestryProject(PsiElement psiElement) {
    Module module = ModuleUtilCore.findModuleForPsiElement(psiElement);
    if (module == null) return null;
    return TapestryModuleSupportLoader.getTapestryProject(module);
  }

  @Nullable
  public static XmlAttribute getTapestryAttribute(XmlTag tag, String attrName) {
    XmlAttribute attribute = tag.getAttribute(attrName, TapestryXmlExtension.getTapestryNamespace(tag));
    return attribute != null ? attribute : tag.getAttribute(attrName, "");
  }

  /**
   * Creates a new component.
   *
   * @param module                  the module to create the page in.
   * @param classSourceDirectory    the source root where to create the page class.
   * @param templateSourceDirectory the source root where to create the page template.
   * @param pageName                the page name.
   * @param replaceExistingFiles    should an existing page file be replaced.
   * @throws IllegalStateException if the page file already existed and {@code replaceExistingFiles = false}
   */
  public static void createComponent(Module module,
                                     PsiDirectory classSourceDirectory,
                                     PsiDirectory templateSourceDirectory,
                                     String pageName,
                                     boolean replaceExistingFiles) throws IllegalStateException {
    String errorMsg = "";
    try {
      createClass(classSourceDirectory, TapestryModuleSupportLoader.getTapestryProject(module).getComponentsRootPackage(), pageName,
                  replaceExistingFiles, TapestryConstants.COMPONENT_CLASS_TEMPLATE_NAME);

      if (templateSourceDirectory != null) {
        createTemplate(module, templateSourceDirectory, TapestryModuleSupportLoader.getTapestryProject(module).getComponentsRootPackage(),
                       pageName, replaceExistingFiles, TapestryConstants.COMPONENT_TEMPLATE_TEMPLATE_NAME);
      }
    }
    catch (IncorrectOperationException ex) {
      errorMsg = "An error occurred creating the component!\n\n";

      _logger.error(ex);
    }
    catch (FileAlreadyExistsException ex) {
      errorMsg = "Some component file already exists, the existing version was kept!\n\n";
    }

    if (errorMsg.length() > 0) {
      throw new IllegalStateException(errorMsg);
    }
  }

  /**
   * Creates a new page.
   *
   * @param module                  the module to create the page in.
   * @param classSourceDirectory    the source root where to create the page class.
   * @param templateSourceDirectory the source root where to create the page template.
   * @param pageName                the page name.
   * @param replaceExistingFiles    should an existing page file be replaced.
   * @throws IllegalStateException if the page file already existed and {@code replaceExistingFiles = false}
   */
  public static void createPage(Module module,
                                PsiDirectory classSourceDirectory,
                                PsiDirectory templateSourceDirectory,
                                String pageName,
                                boolean replaceExistingFiles) throws IllegalStateException {
    String errorMsg = "";
    try {
      createClass(classSourceDirectory, TapestryModuleSupportLoader.getTapestryProject(module).getPagesRootPackage(), pageName,
                  replaceExistingFiles, TapestryConstants.PAGE_CLASS_TEMPLATE_NAME);

      if (templateSourceDirectory != null) {
        createTemplate(module, templateSourceDirectory, TapestryModuleSupportLoader.getTapestryProject(module).getPagesRootPackage(),
                       pageName, replaceExistingFiles, TapestryConstants.PAGE_TEMPLATE_TEMPLATE_NAME);
      }
    }
    catch (IncorrectOperationException ex) {
      errorMsg = "An error occurred creating the page!\n\n";

      _logger.error(ex);
    }
    catch (FileAlreadyExistsException e) {
      errorMsg = "Some page file already exists, the existing version was kept!\n\n";
    }


    if (errorMsg.length() > 0) {
      throw new IllegalStateException(errorMsg);
    }
  }

  /**
   * Creates a new mixin.
   *
   * @param module               the module to create the mixin in.
   * @param classSourceDirectory the source root where to create the mixin class.
   * @param mixinName            the mixin name.
   * @param replaceExistingFiles should an existing mixin file be replaced.
   * @throws IllegalStateException if the mixin file already existed and {@code replaceExistingFiles = false}
   */
  public static void createMixin(Module module, PsiDirectory classSourceDirectory, String mixinName, boolean replaceExistingFiles)
    throws IllegalStateException {
    String errorMsg = "";
    try {
      createClass(classSourceDirectory, TapestryModuleSupportLoader.getTapestryProject(module).getMixinsRootPackage(), mixinName,
                  replaceExistingFiles, TapestryConstants.MIXIN_CLASS_TEMPLATE_NAME);
    }
    catch (IncorrectOperationException ex) {
      errorMsg = "An error occurred creating the mixin!\n\n";

      _logger.error(ex);
    }
    catch (FileAlreadyExistsException e) {
      errorMsg = "Some mixin file already exists, the existing version was kept!\n\n";
    }

    if (errorMsg.length() > 0) {
      throw new IllegalStateException(errorMsg);
    }
  }

  /**
   * Builds the component object that corresponds to a HTML tag.
   *
   * @param tag the component tag.
   * @return the component that the given tag represents.
   */
  @Nullable
  public static TapestryComponent getTypeOfTag(XmlTag tag) {
    return CachedValuesManager.getProjectPsiDependentCache(tag, t -> {
      Module module = ModuleUtilCore.findModuleForPsiElement(tag);
      return module == null ? null : getTypeOfTag(module, tag);
    });
  }

  /**
   * Builds the component object that corresponds to a HTML tag.
   *
   * @param module the module to find the component in.
   * @param tag    the component tag.
   * @return the component that the given tag represents.
   */
  @Nullable
  private static TapestryComponent getTypeOfTag(@NotNull Module module, @NotNull XmlTag tag) {
    TapestryProject tapestryProject = TapestryModuleSupportLoader.getTapestryProject(module);
    if (tapestryProject == null) return null;
    XmlElement identifier = getComponentIdentifier(tag);
    if (identifier == null) return null;

    if (identifier instanceof XmlAttribute) {
      final String attrName = ((XmlAttribute)identifier).getLocalName();
      final String attrValue = ((XmlAttribute)identifier).getValue();
      if (attrValue == null) return null;
      if (attrName.equals("type")) {
        return tapestryProject.findComponent(attrValue);
      }
      if (attrName.equals("id")) {
        PresentationLibraryElement element = tapestryProject.findElementByTemplate(tag.getContainingFile());
        if (element != null) {
          for (TemplateElement embeddedComponent : element.getEmbeddedComponents()) {
            final InjectedElement element1 = embeddedComponent.getElement();
            if (attrValue.equals(element1.getElementId())) return (TapestryComponent)element1.getElement();
          }
        }
      }
      return null;
    }
    final String tagLocalName = StringUtil.toLowerCase(tag.getLocalName()).replace('.', '/');
    // element names are delimited by slashes but tag names may not contain slashes
    return tapestryProject.findComponent(tagLocalName);
  }

  /**
   * Finds the Tapestry namespace prefix declared in a template.
   *
   * @param template the template to search for the prefix;
   * @return the Tapestry namespace prefix declared in the given template or {@code null} if none is found.
   */
  @Nullable
  public static String getTapestryNamespacePrefix(XmlFile template) {
    XmlDocument doc = template.getDocument();
    if (doc == null) return null;
    final XmlTag rootTag = doc.getRootTag();
    if (rootTag == null) return null;
    for (XmlAttribute attribute : rootTag.getAttributes()) {
      if (attribute.getName().startsWith("xmlns:") &&
          TapestryXmlExtension.isTapestryTemplateNamespace(attribute.getValue())) {
        return attribute.getName().substring(6);
      }
    }
    return null;
  }

  /**
   * Creates a class.
   *
   * @param sourceDirectory      the source root where to create the class.
   * @param basePackage          the base package to create the class in.
   * @param pageName             the page name.
   * @param replaceExistingFiles should an existing class be replaced.
   * @param templateName         the name of the template to use for the class.
   * @throws FileAlreadyExistsException  if the page class already existed and {@code replaceExistingFiles = false}
   * @throws IncorrectOperationException if an error occurs creating the class.
   */
  private static void createClass(PsiDirectory sourceDirectory,
                                  String basePackage,
                                  String pageName,
                                  boolean replaceExistingFiles,
                                  String templateName) throws FileAlreadyExistsException, IncorrectOperationException {
    PsiDirectory classDirectory =
      IdeaUtils.findOrCreateDirectoryForPackage(sourceDirectory, PathUtils.getFullComponentPackage(basePackage, pageName));

    String fileName = PathUtils.getComponentFileName(pageName);
    PsiFile file = classDirectory.findFile(fileName + ".java");
    if (file != null) {
      if (!replaceExistingFiles) {
        throw new FileAlreadyExistsException();
      }
      else {
        file.delete();
      }
    }
    JavaDirectoryService.getInstance().createClass(classDirectory, PathUtils.getComponentFileName(pageName), templateName);
  }

  /**
   * Creates a template.
   *
   * @param module               the module to create the page template in.
   * @param sourceDirectory      the source root where to create the page template.
   * @param basePackage          the base package to create the template in.
   * @param pageName             the page name.
   * @param replaceExistingFiles should an existing page class be replaced.
   * @param template             the template to use.
   * @throws FileAlreadyExistsException  if the page template already existed and {@code replaceExistingFiles = false}
   * @throws IncorrectOperationException if an error occurs creating the template.
   */
  private static void createTemplate(Module module,
                                     PsiDirectory sourceDirectory,
                                     String basePackage,
                                     String pageName,
                                     boolean replaceExistingFiles,
                                     String template) throws FileAlreadyExistsException, IncorrectOperationException {
    PsiDirectory templateDirectory;
    if (IdeaUtils.isWebRoot(module, sourceDirectory.getVirtualFile())) basePackage = "";
    templateDirectory =
      IdeaUtils.findOrCreateDirectoryForPackage(sourceDirectory, PathUtils.getFullComponentPackage(basePackage, pageName));

    String fileName = PathUtils.getComponentFileName(pageName) + "." + TapestryConstants.TEMPLATE_FILE_EXTENSION;
    final PsiFile psiFile = templateDirectory.findFile(fileName);
    if (psiFile != null) {
      if (!replaceExistingFiles) throw new FileAlreadyExistsException();
      psiFile.delete();
    }

    PsiFile pageTemplate = PsiFileFactory.getInstance(module.getProject())
      .createFileFromText(fileName, TmlFileType.INSTANCE, FileTemplateManager.getInstance(module.getProject()).getInternalTemplate(template).getText());
    templateDirectory.add(pageTemplate);
  }

  static class FileAlreadyExistsException extends Exception {

  }
}
