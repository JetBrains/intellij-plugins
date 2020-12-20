package com.intellij.tapestry.core;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.impl.java.stubs.index.JavaMethodNameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.tapestry.core.events.TapestryEventsManager;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaTypeCreator;
import com.intellij.tapestry.core.java.IJavaTypeFinder;
import com.intellij.tapestry.core.model.TapestryLibrary;
import com.intellij.tapestry.core.model.presentation.Mixin;
import com.intellij.tapestry.core.model.presentation.Page;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.model.presentation.components.BlockComponent;
import com.intellij.tapestry.core.model.presentation.components.BodyComponent;
import com.intellij.tapestry.core.model.presentation.components.ContainerComponent;
import com.intellij.tapestry.core.model.presentation.components.ParameterComponent;
import com.intellij.tapestry.core.resource.IResource;
import com.intellij.tapestry.core.resource.IResourceFinder;
import com.intellij.tapestry.core.util.LocalizationUtils;
import com.intellij.tapestry.intellij.facet.TapestryFacet;
import com.intellij.tapestry.intellij.facet.TapestryFacetConfiguration;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * A Tapestry project. Every IDE implementation must hold a reference to an instance of this class for each project.
 */
public final class TapestryProject {
  public static final Object[] JAVA_STRUCTURE_DEPENDENCY = {PsiModificationTracker.MODIFICATION_COUNT};
  public static final Object[] OUT_OF_CODE_BLOCK_DEPENDENCY = {PsiModificationTracker.MODIFICATION_COUNT};
  /**
   * The application library id.
   */
  public static final String APPLICATION_LIBRARY_ID = "application";
  /**
   * The Tapestry core library id.
   */
  public static final String CORE_LIBRARY_ID = "core";

  private final TapestryLibrary myCoreLibrary = new TapestryLibrary(CORE_LIBRARY_ID, TapestryConstants.CORE_LIBRARY_PACKAGE, this);
  private final Module myModule;
  private final IResourceFinder myResourceFinder;
  private Collection<TapestryLibrary> myCachedLibraries;
  private Map<String, List<String>> myCachedLibraryMapping;
  private volatile String myLastApplicationPackage;
  private String myLastApplicationFilterName;

  private final IJavaTypeFinder myJavaTypeFinder;
  private final IJavaTypeCreator myJavaTypeCreator;
  private final TapestryEventsManager myEventsManager;

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
    myLastApplicationFilterName = null;
  }

  /**
   * @return the application root package.
   */
  @Nullable
  public String getApplicationRootPackage() {
    TapestryFacetConfiguration myConfiguration = TapestryFacet.findFacetConfiguration(myModule);
    return myConfiguration == null ? null : myConfiguration.getApplicationPackage();
  }

  /**
   * @return the application filter name.
   */
  @Nullable
  public String getApplicationFilterName() {
    TapestryFacetConfiguration myConfiguration = TapestryFacet.findFacetConfiguration(myModule);
    return myConfiguration == null ? null : myConfiguration.getFilterName();
  }

  /**
   * @return the application pages root package.
   */
  @Nullable
  public String getPagesRootPackage() {
    return getElementsRootPackage(TapestryConstants.PAGES_PACKAGE);
  }

  /**
   * @return the application components root package.
   */
  @Nullable
  public String getComponentsRootPackage() {
    return getElementsRootPackage(TapestryConstants.COMPONENTS_PACKAGE);
  }

  /**
   * @return the application mixins root package.
   */
  @Nullable
  public String getMixinsRootPackage() {
    return getElementsRootPackage(TapestryConstants.MIXINS_PACKAGE);
  }

  @Nullable
  private String getElementsRootPackage(@NotNull String subpackage) {
    final String rootPackage = getApplicationRootPackage();
    if (rootPackage == null) return null;
    return rootPackage + "." + subpackage;
  }

  /**
   * Finds the available libraries of this project.
   *
   * @return a collection of all the available libraries to this project.
   */
  @NotNull
  public Collection<TapestryLibrary> getLibraries() {

    String applicationRootPackage = getApplicationRootPackage();
    String applicationFilterName = getApplicationFilterName();
    if (applicationRootPackage == null) return Collections.emptyList();
    final Map<String, List<String>> libraryMapping = findLibraryMapping();
    // volatile read
    if (StringUtil.isNotEmpty(myLastApplicationPackage) && StringUtil.isNotEmpty(myLastApplicationFilterName) && myCachedLibraries != null) {
      if (myLastApplicationPackage.equals(applicationRootPackage)
          && myLastApplicationFilterName.equals(applicationFilterName)
          && libraryMapping.equals(myCachedLibraryMapping)) {
        return myCachedLibraries;
      }
    }

    List<TapestryLibrary> cachedLibraries = new ArrayList<>();

    cachedLibraries.add(new TapestryLibrary(APPLICATION_LIBRARY_ID, applicationRootPackage, this));
    cachedLibraries.add(new TapestryLibrary(APPLICATION_LIBRARY_ID, applicationRootPackage + "." + applicationFilterName, this));
    cachedLibraries.add(myCoreLibrary);

    for (String libraryShortName : libraryMapping.keySet()) {
      for(String baseProject:libraryMapping.get(libraryShortName)) {
        final boolean coreLibrary = CORE_LIBRARY_ID.equals(libraryShortName);
        cachedLibraries.add(new TapestryLibrary(
          APPLICATION_LIBRARY_ID, baseProject, this, coreLibrary ? null : libraryShortName
        ));
      }
    }

    myCachedLibraries = cachedLibraries;
    myCachedLibraryMapping = libraryMapping;
    myLastApplicationFilterName = applicationFilterName;
    myLastApplicationPackage = applicationRootPackage; // volatile write

    return cachedLibraries;
  }

  /**
   * Finds the application library.
   *
   * @return the application library.
   */
  @Nullable
  public TapestryLibrary getApplicationLibrary() {
    Collection<TapestryLibrary> libraries = getLibraries();
    return libraries.size() == 0 ? null : libraries.iterator().next();
  }

  /**
   * Finds a page by name in the Tapestry application.
   *
   * @param pageName the page name to look.
   * @return the page with the given name, or {@code null} if the page isn't found.
   */
  @Nullable
  public Page findPage(String pageName) {
    return (Page)ourNameToPageMap.get(myModule).get(StringUtil.toLowerCase(pageName));
  }

  public String @NotNull [] getAvailablePageNames() {
    final Set<String> names = ourNameToPageMap.get(myModule).keySet();
    return ArrayUtilRt.toStringArray(names);
  }

  private static final ElementsCachedMap ourNameToPageMap = new ElementsCachedMap("ourNameToPageMap", false, true, false) {
    @Override
    protected String computeKey(PresentationLibraryElement element) {
      return StringUtil.toLowerCase(element.getName());
    }
  };


  /**
   * Finds a page by class in the Tapestry application.
   *
   * @param pageClass the page class to look.
   * @return the page of the given class, or {@code null} if the page isn't found.
   */
  @Nullable
  public Page findPage(@NotNull IJavaClassType pageClass) {
    return (Page)ourFqnToPageMap.get(myModule).get(pageClass.getFullyQualifiedName());
  }

  private static final ElementsCachedMap ourFqnToPageMap = new ElementsCachedMap("ourFqnToPageMap", false, true, false) {
    @Override
    protected String computeKey(PresentationLibraryElement element) {
      return element.getElementClass().getFullyQualifiedName();
    }
  };

  /**
   * Finds a component by name in the Tapestry application.
   *
   * @param componentName the component name to look.
   * @return the component with the given name, or {@code null} if the component isn't found.
   */
  @Nullable
  public TapestryComponent findComponent(@NotNull String componentName) {
    return (TapestryComponent)ourNameToComponentMap.get(myModule).get(StringUtil.toLowerCase(componentName));
  }

  /**
   * Finds a mixin by name in the Tapestry application.
   *
   * @param mixinName the component name to look.
   * @return the mixin component with the given name, or {@code null} if the mixin isn't found.
   */
  @Nullable
  public Mixin findMixin(String mixinName) {
    return (Mixin)ourNameToMixinMap.get(myModule).get(StringUtil.toLowerCase(mixinName));
  }

  @NotNull
  private Map<String, List<String>> findLibraryMapping() {
    Map<String, List<String>> result = new HashMap<>();

    GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);
    for (PsiMethod psiMethod : JavaMethodNameIndex.getInstance().get(
      "contributeComponentClassResolver",
      myModule.getProject(),
      scope
    )) {
      addFromMappingData(result, MappingDataCache.getMappingData(psiMethod.getContainingFile()));
    }

    // method annotated with @Contribute(ComponentClassResolver.class)
    Collection<PsiAnnotation> annotations = JavaAnnotationIndex.getInstance().get("Contribute", myModule.getProject(), scope);
    for(PsiAnnotation annotation:annotations) {
      PsiNameValuePair[] attributes = annotation.getParameterList().getAttributes();
      if (attributes.length != 1) continue;
      PsiAnnotationMemberValue value = attributes[0].getValue();
      if (value instanceof PsiClassObjectAccessExpression && "ComponentClassResolver".equals(((PsiClassObjectAccessExpression)value).getOperand().getText())) {
        addFromMappingData(result, MappingDataCache.getMappingData(annotation.getContainingFile()));
      }
    }

    return result;
  }

  private static void addFromMappingData(Map<String, List<String>> result, Map<String, String> computedMap) {
    for (String key : computedMap.keySet()) {
      List<String> strings = result.get(key);
      if (strings == null) result.put(key, strings = new ArrayList<>(2));
      strings.add(computedMap.get(key));
    }
  }

  public String @NotNull [] getAvailableComponentNames() {
    final Set<String> names = ourNameToComponentMap.get(myModule).keySet();
    return ArrayUtilRt.toStringArray(names);
  }

  private static final ElementsCachedMap ourNameToComponentMap = new ElementsCachedMap("ourNameToComponentMap", true, false, false) {
    @Override
    protected String computeKey(PresentationLibraryElement element) {
      return StringUtil.toLowerCase(element.getName());
    }
  };

  private static final ElementsCachedMap ourNameToMixinMap = new ElementsCachedMap("ourNameToMixinMap", false, false, true) {
    @Override
    protected String computeKey(PresentationLibraryElement element) {
      return StringUtil.toLowerCase(element.getName());
    }
  };

  public Collection<PresentationLibraryElement> getBuiltinComponents() {
    return Arrays.asList(BodyComponent.getInstance(this), BlockComponent.getInstance(this),
                         ParameterComponent.getInstance(this),
                         ContainerComponent.getInstance(this));
  }

  public Collection<PresentationLibraryElement> getBuiltinPages() {
    return Collections.emptyList();
  }

  /**
   * Finds a Tapestry element, either a component or page can be returned.
   *
   * @param elementClass the element class to find.
   * @return either the page or component to which the given class belongs to, or {@code null} if the element isn't found.
   */
  @Nullable
  public PresentationLibraryElement findElement(@NotNull IJavaClassType elementClass) {
    TapestryComponent component = findComponent(elementClass);
    return component != null ? component : findPage(elementClass);
  }

  /**
   * Finds a component by class in the Tapestry application.
   *
   * @param componentClass the component class to look.
   * @return the component of the given class, or {@code null} if the component isn't found.
   */
  @Nullable
  public TapestryComponent findComponent(@NotNull IJavaClassType componentClass) {
    return (TapestryComponent)ourFqnToComponentMap.get(myModule).get(componentClass.getFullyQualifiedName());
  }

  private static final ElementsCachedMap ourFqnToComponentMap = new ElementsCachedMap("ourFqnToComponentMap", true, false, false) {
    @Override
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
    String templatePath = new File(template.getOriginalFile().getViewProvider().getVirtualFile().getPath()).getAbsolutePath();
    return ourTemplateToElementMap.get(myModule).get(LocalizationUtils.unlocalizeFileName(templatePath));
  }

  private static final ElementsCachedMap ourTemplateToElementMap = new ElementsCachedMap("ourTemplateToElementMap", true, true, false, true) {
    @Override
    @Nullable
    protected String computeKey(PresentationLibraryElement element) {
      final IResource[] resources = element.getTemplate();
      return resources.length > 0 ? LocalizationUtils.unlocalizeFileName(resources[0].getFile().getAbsolutePath()) : null;
    }
  };

  @NotNull
  public Collection<PresentationLibraryElement> getAvailableElements() {
    return ourFqnToComponentMap.get(myModule).values();
  }

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
}
