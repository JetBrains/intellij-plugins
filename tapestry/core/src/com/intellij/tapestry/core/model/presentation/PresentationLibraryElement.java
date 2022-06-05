package com.intellij.tapestry.core.model.presentation;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.exceptions.NotTapestryElementException;
import com.intellij.tapestry.core.java.IJavaAnnotation;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.model.externalizable.ExternalizableToClass;
import com.intellij.tapestry.core.model.externalizable.ExternalizableToDocumentation;
import com.intellij.tapestry.core.model.externalizable.documentation.generationchain.DocumentationGenerationChain;
import com.intellij.tapestry.core.model.externalizable.toclasschain.ExternalizeToClassChain;
import com.intellij.tapestry.core.resource.CoreXmlRecursiveElementVisitor;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.resource.xml.XmlAttribute;
import com.intellij.tapestry.core.resource.xml.XmlTag;
import com.intellij.tapestry.core.util.ComponentUtils;
import com.intellij.tapestry.core.util.LocalizationUtils;
import com.intellij.tapestry.core.util.PathUtils;
import com.intellij.tapestry.intellij.lang.descriptor.TapestryXmlExtension;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Base class for every presentation element that can be in a Tapestry library.
 */
public abstract class PresentationLibraryElement implements ExternalizableToDocumentation, ExternalizableToClass {

  /**
   * The element type.
   */
  public enum ElementType {

    /**
     * A page.
     */
    PAGE,

    /**
     * A component.
     */
    COMPONENT,

    /**
     * A mixin.
     */
    MIXIN
  }

  private final IJavaClassType _class;
  private final TapestryProject _project;
  private String _name;
  private final TapestryLibrary _library;
  private String _documentationCache;
  private IResource[] _messageCatalogCache;
  private long _documentationTimestamp;
  private final ElementType _elementType;

  static final String PARAMETER_ANNOTATION = "org.apache.tapestry5.annotations.Parameter";

  PresentationLibraryElement(TapestryLibrary library, IJavaClassType elementClass, TapestryProject project) {
    _class = elementClass;
    _project = project;
    _library = library;

    if (library != null && library.getId() != null) {
      _name = getElementNameFromClass(library.getBasePackage());
      _elementType = getElementType(_class, library.getBasePackage());
    }
    else {
      try {
        _name = getElementNameFromClass(null);
      }
      catch (NotTapestryElementException e) {
        // ignore
      }
      _elementType = ElementType.COMPONENT;
    }
  }

  /**
   * Creates an instance of a presentation element.
   *
   * @param library      the library the element belonds to.
   * @param elementClass the class of the element.
   * @param project      the project the element belongs to.
   * @return the instance of the presentation element.
   * @throws NotTapestryElementException if the given parameters do not correspond to a Tapestry element.
   */
  public static PresentationLibraryElement createElementInstance(TapestryLibrary library, IJavaClassType elementClass, TapestryProject project)
    throws NotTapestryElementException {
    switch (getElementType(elementClass, library.getBasePackage())) {
      case COMPONENT:
        return new TapestryComponent(library, elementClass, project);
      case PAGE:
        return new Page(library, elementClass, project);
      case MIXIN:
        return new Mixin(library, elementClass, project);
      default:
        throw new NotTapestryElementException(elementClass.getFullyQualifiedName() + " is not a Tapestry class.");
    }
  }

  /**
   * Creates an instance of a presentation element of the current project library.
   *
   * @param elementClass the class of the element.
   * @param project      the project the element belongs to.
   * @return the instance of the presentation element.
   * @throws NotTapestryElementException if the given parameters do not correspond to a Tapestry element.
   */
  public static @Nullable PresentationLibraryElement createProjectElementInstance(IJavaClassType elementClass, TapestryProject project)
    throws NotTapestryElementException {
    TapestryLibrary library = project == null ? null : project.getApplicationLibrary();
    return library == null ? null : createElementInstance(library, elementClass, project);
  }

  /**
   * @return {@code true} if this element allows a template, {@code false} otherwise.
   */
  public abstract boolean allowsTemplate();

  /**
   * Finds the templates associated with this element and returns them.
   * It returns an array because an element can have more than one localized template.
   *
   * @return the templates in no special order. This never returns {@code null}
   */
  public abstract IResource[] getTemplate();

  public IResource[] getTemplateConsiderSuperClass() {
    IResource[] resources = getTemplate();
    if (resources.length > 0) return resources;
    IJavaClassType superClass = getElementClass().getSuperClassType();
    if (superClass == null) return IResource.EMPTY_ARRAY;
    PresentationLibraryElement superElement = getProject().findElement(superClass);
    return superElement == null ? IResource.EMPTY_ARRAY : superElement.getTemplateConsiderSuperClass();
  }

  /**
   * Finds the message catalogs associated with this element and returns then.
   * It returns an array because an element can have more than one localized message catalog.
   *
   * @return the catalogs in no special order. This never returns {@code null}
   */
  public IResource[] getMessageCatalog() {
    if (_messageCatalogCache != null && checkAllValidResources(_messageCatalogCache)) {
      return _messageCatalogCache;
    }

    String packageName = getElementClass().getFullyQualifiedName().substring(0, getElementClass().getFullyQualifiedName().lastIndexOf('.'));

    // Search in the classpath
    Collection<IResource> resources = getProject().getResourceFinder().findLocalizedClasspathResource(
      PathUtils.packageIntoPath(packageName, true) + PathUtils.getLastPathElement(getName()) + TapestryConstants.PROPERTIES_FILE_EXTENSION,
      true);

    if (resources.size() > 0) {
      List<IResource> catalogs = new ArrayList<>();

      for (IResource catalog : resources) {
        if (LocalizationUtils.unlocalizeFileName(catalog.getName())
          .equals(PathUtils.getLastPathElement(getName()) + TapestryConstants.PROPERTIES_FILE_EXTENSION)) {
          catalogs.add(catalog);
        }
      }

      _messageCatalogCache = catalogs.toArray(IResource.EMPTY_ARRAY);

      return _messageCatalogCache;
    }
    else {
      _messageCatalogCache = IResource.EMPTY_ARRAY;
    }

    return _messageCatalogCache;
  }

  /**
   * Returns the element documentation.
   *
   * @return the element documentation.
   */
  public String getDescription() {
    if (_documentationCache != null && getElementClass().getFile().getFile().lastModified() <= _documentationTimestamp) {
      return _documentationCache;
    }

    _documentationCache = getElementClass().getDocumentation();
    _documentationTimestamp = getElementClass().getFile().getFile().lastModified();

    return _documentationCache;
  }

  public IJavaClassType getElementClass() {
    return _class;
  }

  public String getName() {
    return _name;
  }

  public TapestryProject getProject() {
    return _project;
  }

  public TapestryLibrary getLibrary() {
    return _library;
  }

  /**
   * {@inheritDoc}
   */
  public boolean equals(Object obj) {
    return (obj instanceof PresentationLibraryElement) && getName().equals(((PresentationLibraryElement)obj).getName());
  }

  /**
   * {@inheritDoc}
   */
  public int hashCode() {
    return getName().hashCode();
  }

  /**
   * Finds all declared embedded components.
   *
   * @return the declared embedded components.
   */
  public List<TemplateElement> getEmbeddedComponents() {

    final List<TemplateElement> embeddedComponentsTemplate = new ArrayList<>();
    final List<TemplateElement> embeddedComponents = new ArrayList<>();

    for (IJavaField field : _class.getFields(true).values()) {
      if (field.isValid() &&
          field.getAnnotations().containsKey(TapestryConstants.COMPONENT_ANNOTATION) &&
          field.getType() instanceof IJavaClassType) {
        IJavaAnnotation annotation = field.getAnnotations().get(TapestryConstants.COMPONENT_ANNOTATION);
        final TapestryComponent component;
        if (annotation.getParameters().containsKey("type")) {
          component = _project.findComponent(annotation.getParameters().get("type")[0]);
        }
        else {
          component = _project.findComponent((IJavaClassType)field.getType());
        }
        embeddedComponents.add(new TemplateElement(new InjectedElement(field, component), "class"));
      }
    }

    final List<TemplateElement> embeddedComponentsClass = new ArrayList<>(embeddedComponents);

    for (int i = 0; i < getTemplate().length; i++) {

      getTemplate()[i].accept(new CoreXmlRecursiveElementVisitor() {
        @Override
        public void visitTag(XmlTag tag) {
          if (!ComponentUtils._isComponentTag(tag)) return;
          boolean hasAttributeType = false;
          InjectedElement injectedElement = null;
          TapestryComponent component = null;

          for (XmlAttribute attribute : tag.getAttributes()) {
            if (attribute.getLocalName().equals("type") && TapestryXmlExtension.isTapestryTemplateNamespace(attribute.getNamespace())) {
              String value = attribute.getValue();
              if (value != null) {
                component = _project.findComponent(value);
                injectedElement = new InjectedElement(tag, component);
                hasAttributeType = true;
              }
            }
          }

          if (!hasAttributeType) {
            component = _project.findComponent(tag.getLocalName());
            injectedElement = new InjectedElement(tag, component);
          }

          if (embeddedComponents.isEmpty() || component == null) return;
          for (TemplateElement element : embeddedComponents) {
            final String injectedElementId = injectedElement.getElementId();
            if (injectedElementId == null) continue;
            final String elementId = element.getElement().getElementId();
            final PresentationLibraryElement libraryElement = element.getElement().getElement();
            if (libraryElement != null &&
                elementId != null &&
                elementId.equalsIgnoreCase(injectedElementId) &&
                libraryElement.getName().equalsIgnoreCase(injectedElement.getTag().getLocalName()) &&
                injectedElement.getParameters().size() != 1) {
              if (!embeddedComponentsClass.isEmpty()) embeddedComponentsClass.remove(element);
            }
          }
        }
      });
    }
    embeddedComponentsClass.addAll(embeddedComponentsTemplate);

    return embeddedComponentsClass;
  }

  /**
   * Finds all component declarations in templates.
   *
   * @return the declared embedded components in templates.
   */
  public List<TemplateElement> getEmbeddedComponentsTemplate() {

    final List<TemplateElement> embeddedComponentsTemplate = new ArrayList<>();
    final List<TemplateElement> embeddedComponentsClass = getEmbeddedComponents();

    for (int i = 0; i < getTemplate().length; i++) {
      final String _resource = getTemplate()[i].getName();

      getTemplate()[i].accept(new CoreXmlRecursiveElementVisitor() {
        @Override
        public void visitTag(XmlTag tag) {
          if (ComponentUtils._isComponentTag(tag)) {

            boolean hasAttributeType = false;
            InjectedElement injectedElement = null;
            TapestryComponent component = null;

            for (XmlAttribute attribute : tag.getAttributes()) {
              if (attribute.getLocalName().equals("type") && TapestryXmlExtension.isTapestryTemplateNamespace(attribute.getNamespace())) {
                component = _project.findComponent(attribute.getValue());
                injectedElement = new InjectedElement(tag, component);

                hasAttributeType = true;
              }
            }

            if (!hasAttributeType) {
              component = _project.findComponent(tag.getLocalName());
              injectedElement = new InjectedElement(tag, component);
            }

            if (!embeddedComponentsClass.isEmpty() && component != null) {
              TemplateElement element = new TemplateElement(injectedElement, "class");

              if (!embeddedComponentsClass.contains(element)) {
                embeddedComponentsTemplate.add(new TemplateElement(injectedElement, _resource));
              }

            }
            else if (component != null) embeddedComponentsTemplate.add(new TemplateElement(injectedElement, _resource));

          }
        }
      });
    }
    return embeddedComponentsTemplate;
  }

  /**
   * Finds all declared embedded components.
   *
   * @return the declared embedded components.
   */
  public List<InjectedElement> getInjectedPages() {
    List<InjectedElement> injectedPages = new ArrayList<>();

    for (IJavaField field : _class.getFields(true).values()) {
      if (field.isValid() && field.getAnnotations().containsKey(TapestryConstants.INJECT_PAGE_ANNOTATION)) {
        IJavaAnnotation annotation = field.getAnnotations().get(TapestryConstants.INJECT_PAGE_ANNOTATION);
        if (annotation.getParameters().containsKey("value")) {
          final Page page = _project.findPage(annotation.getParameters().get("value")[0]);
          if (page != null) {
            injectedPages.add(new InjectedElement(field, page));
          }
        }
        else {
          IJavaClassType type = (IJavaClassType)field.getType();
          if (type != null) {
            injectedPages.add(new InjectedElement(field, _project.findPage(type)));
          }
        }
      }
    }

    return injectedPages;
  }

  public ElementType getElementType() {
    return _elementType;
  }

  @Override
  public String getDocumentation() throws Exception {
    return DocumentationGenerationChain.getInstance().generate(this);
  }

  @Override
  public String getClassRepresentation(IJavaClassType targetClass) throws Exception {
    return ExternalizeToClassChain.getInstance().externalize(this, targetClass);
  }

  /**
   * Constructs the element name from it's class and library root package.
   *
   * @param libraryRootPackage the library root package.
   * @return the element name.
   * @throws NotTapestryElementException if this is not a Tapestry element.
   */
  protected String getElementNameFromClass(String libraryRootPackage) throws NotTapestryElementException {
    if (!_class.isPublic() || !_class.hasDefaultConstructor()) {
      throw new NotTapestryElementException(_class.getFullyQualifiedName() + " is not a valid Tapestry class.");
    }

    if (libraryRootPackage == null) {
      throw new NotTapestryElementException(_class.getFullyQualifiedName() + " is not a valid Tapestry class.");
    }

    String elementClassFqn = _class.getFullyQualifiedName();
    String elementName;

    elementName = elementClassFqn.substring(libraryRootPackage.length() + 1);
    if (elementName.startsWith(TapestryConstants.COMPONENTS_PACKAGE)) {
      elementName = PathUtils.packageIntoPath(elementName.substring(TapestryConstants.COMPONENTS_PACKAGE.length() + 1), false);
    }
    else if (elementName.startsWith(TapestryConstants.BASE_PACKAGE)) {
      elementName = PathUtils.packageIntoPath(elementName.substring(TapestryConstants.BASE_PACKAGE.length() + 1), false);
    }
    else if (elementName.startsWith(TapestryConstants.PAGES_PACKAGE)) {
      elementName = PathUtils.packageIntoPath(elementName.substring(TapestryConstants.PAGES_PACKAGE.length() + 1), false);
    }
    else if (elementName.startsWith(TapestryConstants.MIXINS_PACKAGE)) {
      elementName = PathUtils.packageIntoPath(elementName.substring(TapestryConstants.MIXINS_PACKAGE.length() + 1), false);
    }
    else {
      throw new NotTapestryElementException(_class.getFullyQualifiedName() + " is not under a Tapestry base package.");
    }

    return elementName;
  }

  /**
   * Checks if the files in a group of resources are all valid.
   *
   * @param resources the resources to check.
   * @return {@code true} if all the resources are valid, {@code false} otherwise.
   */
  protected static boolean checkAllValidResources(IResource[] resources) {
    for (IResource resource : resources) {
      if (resource.getFile() == null || !resource.getFile().exists()) {
        return false;
      }
    }

    return true;
  }

  private static ElementType getElementType(IJavaClassType elementClass, String basePackage) throws NotTapestryElementException {
    String elementName;
    try {
      elementName = elementClass.getFullyQualifiedName().substring(basePackage.length() + 1);
    }
    catch (IndexOutOfBoundsException ex) {
      throw new NotTapestryElementException(elementClass.getFullyQualifiedName() + " is not under a Tapestry base package.");
    }

    if (elementName.startsWith(TapestryConstants.COMPONENTS_PACKAGE) || elementName.startsWith(TapestryConstants.BASE_PACKAGE)) {
      return ElementType.COMPONENT;
    }
    else if (elementName.startsWith(TapestryConstants.PAGES_PACKAGE)) {
      return ElementType.PAGE;
    }
    else if (elementName.startsWith(TapestryConstants.MIXINS_PACKAGE)) {
      return ElementType.MIXIN;
    }
    else {
      throw new NotTapestryElementException(elementClass.getFullyQualifiedName() + " is not under a Tapestry base package.");
    }
  }
}
