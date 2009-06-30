package com.intellij.tapestry.core;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;
import com.intellij.tapestry.core.events.TapestryEventsManager;
import com.intellij.tapestry.core.exceptions.CoreException;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaTypeCreator;
import com.intellij.tapestry.core.java.IJavaTypeFinder;
import com.intellij.tapestry.core.model.Library;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.Page;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.resource.IResourceFinder;
import com.intellij.tapestry.core.util.LocalizationUtils;
import static com.intellij.tapestry.core.util.StringUtils.isNotEmpty;
import com.intellij.tapestry.core.util.WebDescriptorUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A Tapestry project. Every IDE implementation must hold a reference to an instance of this class for each project.
 */
public class TapestryProject {

  /**
   * The application library id.
   */
  public static final String APPLICATION_LIBRARY_ID = "application";
  /**
   * The Tapestry core library id.
   */
  public static final String CORE_LIBRARY_ID = "core";

  private static final String WEBXML_PATH = "/WEB-INF/web.xml";

  private static final Collection<Library> EMPTY_LIBRARY_LIST = new ArrayList<Library>();

  private static DocumentBuilder myDocumentBuilder;
  private final Library myCoreLibrary = new Library(CORE_LIBRARY_ID, TapestryConstants.CORE_LIBRARY_PACKAGE, this);
  private final Module myModule;
  //private final Library _iocLibrary = new Library("ioc", TapestryConstants.IOC_LIBRARY_PACKAGE, this);
  private final IResourceFinder myResourceFinder;
  private long myWebApplicationDescriptorTimestamp;
  private Document myWebApplicationDescriptorDocument;
  private String myCachedRootPackage;
  private String myCachedFilterName;
  private Collection<Library> myCachedLibraries;
  private String myLastApplicationPackage;

  private final IJavaTypeFinder myJavaTypeFinder;
  private final IJavaTypeCreator myJavaTypeCreator;
  private final TapestryEventsManager myEventsManager;


  static {
    try {
      myDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    catch (ParserConfigurationException ex) {
      throw new CoreException(ex);
    }
  }

  public TapestryProject(@NotNull Module module,
                         @NotNull IResourceFinder resourceFinder,
                         @NotNull IJavaTypeFinder javaTypeFinder,
                         @NotNull IJavaTypeCreator javaTypeCreator) {
    myModule = module;
    myResourceFinder = resourceFinder;
    myJavaTypeFinder = javaTypeFinder;
    myJavaTypeCreator = javaTypeCreator;

    myEventsManager = new TapestryEventsManager();
    myLastApplicationPackage = null;
  }

  /**
   * Finds the configured Tapestry filter name.
   *
   * @return the configured Tapestry filter name.
   * @throws NotFoundException when for some reason the filter name couldn't be found.
   */
  @NotNull
  public String getTapestryFilterName() throws NotFoundException {
    boolean updated;

    try {
      updated = updateDocument();
    }
    catch (Exception ex) {
      throw new NotFoundException(ex);
    }

    if (!updated && myCachedFilterName != null) {
      return myCachedFilterName;
    }

    myCachedFilterName = WebDescriptorUtils.getTapestryFilterName(myWebApplicationDescriptorDocument);
    if (myCachedFilterName == null) {
      throw new NotFoundException();
    }

    return myCachedFilterName;
  }

  /**
   * @return the application root package.
   * @throws NotFoundException when for some reason the application package couldn't be found.
   */
  @NotNull
  public String getApplicationRootPackage() throws NotFoundException {
    boolean updated;

    try {
      updated = updateDocument();
    }
    catch (Exception ex) {
      throw new NotFoundException(ex);
    }

    if (!updated && myCachedRootPackage != null) {
      return myCachedRootPackage;
    }

    myCachedRootPackage = WebDescriptorUtils.getApplicationPackage(myWebApplicationDescriptorDocument);
    if (myCachedRootPackage == null) {
      throw new NotFoundException();
    }

    return myCachedRootPackage;
  }

  /**
   * @return the application pages root package.
   * @throws NotFoundException when the pages root package can't be found.
   */
  @NotNull
  public String getPagesRootPackage() throws NotFoundException {
    return getApplicationRootPackage() + "." + TapestryConstants.PAGES_PACKAGE;
  }

  /**
   * @return the application components root package.
   * @throws NotFoundException when the components root package can't be found.
   */
  @NotNull
  public String getComponentsRootPackage() throws NotFoundException {
    return getApplicationRootPackage() + "." + TapestryConstants.COMPONENTS_PACKAGE;
  }

  /**
   * @return the application mixins root package.
   * @throws NotFoundException when the mixins root package can't be found.
   */
  @NotNull
  public String getMixinsRootPackage() throws NotFoundException {
    return getApplicationRootPackage() + "." + TapestryConstants.MIXINS_PACKAGE;
  }

  /**
   * Finds the available libraries of this project.
   *
   * @return a collection of all the available libraries to this project.
   */
  @NotNull
  public Collection<Library> getLibraries() {

    String applicationRootPackage;
    try {
      applicationRootPackage = getApplicationRootPackage();
    }
    catch (NotFoundException e) {
      return EMPTY_LIBRARY_LIST;
    }

    if (myCachedLibraries != null && isNotEmpty(myLastApplicationPackage)) {
      if (myLastApplicationPackage.equals(applicationRootPackage)) {
        return myCachedLibraries;
      }
    }

    myCachedLibraries = new ArrayList<Library>();
    myLastApplicationPackage = applicationRootPackage;

    myCachedLibraries.add(new Library(APPLICATION_LIBRARY_ID, applicationRootPackage, this));
    myCachedLibraries.add(myCoreLibrary);
    //myCachedLibraries.add(_iocLibrary);

    return myCachedLibraries;
  }

  /**
   * Finds the application library.
   *
   * @return the application library.
   */
  @Nullable
  public Library getApplicationLibrary() {
    Collection<Library> libraries = getLibraries();
    return libraries.size() == 0 ? null : libraries.iterator().next();
  }

  @NotNull
  public PresentationLibraryElement[] getAllAvailableComponents() {
    List<PresentationLibraryElement> components = new ArrayList<PresentationLibraryElement>();
    for (Library library : getLibraries()) {
      components.addAll(library.getComponents().values());
    }
    return components.toArray(new PresentationLibraryElement[components.size()]);
  }

  /**
   * Finds a page by name in the Tapestry application.
   *
   * @param pageName the page name to look.
   * @return the page with the given name, or <code>null</code> if the page isn't found.
   */
  @Nullable
  public Page findPage(String pageName) {
    for (Library library : getLibraries()) {
      Map<String, PresentationLibraryElement> libraryPages = library.getPages();
      if (libraryPages.containsKey(pageName)) {
        return (Page)libraryPages.get(pageName);
      }
    }

    return null;
  }

  /**
   * Finds a page by class in the Tapestry application.
   *
   * @param pageClass the page class to look.
   * @return the page of the given class, or <code>null</code> if the page isn't found.
   */
  @Nullable
  public Page findPage(@NotNull IJavaClassType pageClass) {
    return (Page)ourFqnToPageMap.get(myModule).get(pageClass.getFullyQualifiedName());
  }

  private static final ElementsCachedMap ourFqnToPageMap = new ElementsCachedMap("ourFqnToPageMap", false, true) {
    protected String computeKey(PresentationLibraryElement element) {
      return element.getElementClass().getFullyQualifiedName();
    }
  };

  /**
   * Finds a component by name in the Tapestry application.
   *
   * @param componentName the component name to look.
   * @return the component with the given name, or <code>null</code> if the component isn't found.
   */
  @Nullable
  public Component findComponent(@NotNull String componentName) {
    for (Library library : getLibraries()) {
      Map<String, PresentationLibraryElement> libraryComponents = library.getComponents();
      if (libraryComponents.containsKey(componentName)) {
        return (Component)libraryComponents.get(componentName);
      }
    }
    return null;
  }

  /**
   * Finds a Tapestry element, either a component or page can be returned.
   *
   * @param elementClass the element class to find.
   * @return either the page or component to which the given class belongs to, or <code>null</code> if the element isn't found.
   */
  @Nullable
  public PresentationLibraryElement findElement(@NotNull IJavaClassType elementClass) {
    Component component = findComponent(elementClass);
    return component != null ? component : findPage(elementClass);
  }

  /**
   * Finds a component by class in the Tapestry application.
   *
   * @param componentClass the component class to look.
   * @return the component of the given class, or <code>null</code> if the component isn't found.
   */
  @Nullable
  public Component findComponent(@NotNull IJavaClassType componentClass) {
    return (Component)ourFqnToComponentMap.get(myModule).get(componentClass.getFullyQualifiedName());
  }

  private static final ElementsCachedMap ourFqnToComponentMap = new ElementsCachedMap("ourFqnToComponentMap", true, false) {
    protected String computeKey(PresentationLibraryElement element) {
      return element.getElementClass().getFullyQualifiedName();
    }
  };

  /**
   * Finds the component class from it's template.
   *
   * @param template the component template.
   * @return the component class.
   */
  @Nullable
  public PresentationLibraryElement findElementByTemplate(@NotNull PsiFile template) {
    String templatePath = new File(template.getViewProvider().getVirtualFile().getPath()).getAbsolutePath();
    return ourTemplateToElementMap.get(myModule).get(LocalizationUtils.unlocalizeFileName(templatePath));
  }

  private static final ElementsCachedMap ourTemplateToElementMap = new ElementsCachedMap("ourTemplateToElementMap", true, true) {
    @Nullable
    protected String computeKey(PresentationLibraryElement element) {
      final IResource[] resources = element.getTemplate();
      return resources.length > 0 ? LocalizationUtils.unlocalizeFileName(resources[0].getFile().getAbsolutePath()) : null;
    }
  };

  @NotNull
  public IJavaTypeFinder getJavaTypeFinder() {
    return myJavaTypeFinder;
  }

  @NotNull
  public IJavaTypeCreator getJavaTypeCreator() {
    return myJavaTypeCreator;
  }

  public IResourceFinder getResourceFinder() {
    return myResourceFinder;
  }

  @NotNull
  public TapestryEventsManager getEventsManager() {
    return myEventsManager;
  }

  @Nullable
  public String getWebXmlPath() {
    IResource resource = myResourceFinder.findContextResource(WEBXML_PATH);
    return resource != null ? resource.getFile().getAbsolutePath() : null;
  }

  /**
   * Checks if the file as been changed since last access. If so, builds a new document of it into memory.
   *
   * @return <code>true</code> if the file was changed since last access, <code>false</code> otherwise.
   * @throws Exception when an error occurs parsing the web.xml file.
   */
  private boolean updateDocument() throws Exception {
    File file = myResourceFinder.findContextResource(WEBXML_PATH).getFile();

    if (file.lastModified() > myWebApplicationDescriptorTimestamp) {
      myWebApplicationDescriptorDocument = myDocumentBuilder.parse(file);
      myWebApplicationDescriptorTimestamp = file.lastModified();
      return true;
    }

    return false;
  }

}
