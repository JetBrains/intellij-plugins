// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import org.angular2.entities.metadata.psi.*;
import org.angular2.entities.source.Angular2SourceComponent;
import org.angular2.entities.source.Angular2SourceDirective;
import org.angular2.entities.source.Angular2SourceModule;
import org.angular2.entities.source.Angular2SourcePipe;
import org.angular2.index.*;
import org.angular2.lang.Angular2LangUtil;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.util.containers.ContainerUtil.concat;
import static com.intellij.util.containers.ContainerUtil.newHashSet;
import static java.util.stream.Collectors.toMap;
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.index.Angular2IndexingHandler.NG_MODULE_INDEX_NAME;

public class Angular2EntitiesProvider {

  static final String TRANSFORM_METHOD = "transform";

  @Nullable
  public static Angular2Entity getEntity(@Nullable PsiElement element) {
    Angular2Entity result;
    if ((result = getDirective(element)) != null) {
      return result;
    }
    if ((result = getPipe(element)) != null) {
      return result;
    }
    if ((result = getModule(element)) != null) {
      return result;
    }
    return null;
  }

  @Nullable
  public static Angular2Component getComponent(@Nullable PsiElement element) {
    return ObjectUtils.tryCast(getDirective(element), Angular2Component.class);
  }

  @Nullable
  public static Angular2Directive getDirective(@Nullable PsiElement element) {
    return DIRECTIVE_GETTER.get(element);
  }

  @Nullable
  public static Angular2Pipe getPipe(@Nullable PsiElement element) {
    if (element instanceof TypeScriptFunction
        && TRANSFORM_METHOD.equals(((TypeScriptFunction)element).getName())
        && element.getContext() instanceof TypeScriptClass) {
      element = element.getContext();
    }
    return PIPE_GETTER.get(element);
  }

  @Nullable
  public static Angular2MetadataFunction findMetadataFunction(@NotNull JSFunction function) {
    TypeScriptClass parent = ObjectUtils.tryCast(function.getContext(), TypeScriptClass.class);
    if (function.getName() == null || parent == null) {
      return null;
    }
    Ref<Angular2MetadataFunction> result = new Ref<>();
    StubIndex.getInstance().processElements(
      Angular2MetadataFunctionIndex.KEY, function.getName(), function.getProject(),
      GlobalSearchScope.allScope(function.getProject()),
      Angular2MetadataFunction.class, (f) -> {
        Angular2MetadataClassBase parentClass = ObjectUtils.tryCast(f.getContext(), Angular2MetadataClassBase.class);
        if (parentClass != null && parent.equals(parentClass.getTypeScriptClass())) {
          result.set(f);
          return false;
        }
        return true;
      });
    return result.get();
  }

  @Nullable
  public static Angular2Module getModule(@Nullable PsiElement element) {
    return MODULE_GETTER.get(element);
  }

  @NotNull
  public static List<Angular2Directive> findElementDirectivesCandidates(@NotNull Project project, @NotNull String elementName) {
    return findDirectivesCandidates(project, Angular2EntityUtils.getElementDirectiveIndexName(elementName));
  }

  @NotNull
  public static List<Angular2Directive> findAttributeDirectivesCandidates(@NotNull Project project, @NotNull String attributeName) {
    return findDirectivesCandidates(project, Angular2EntityUtils.getAttributeDirectiveIndexName(attributeName));
  }

  @Nullable
  public static Angular2Pipe findPipe(@NotNull Project project, @NotNull String name) {
    JSImplicitElement pipe = AngularIndexUtil.resolve(project, Angular2SourcePipeIndex.KEY, name);
    if (pipe != null) {
      return getPipe(pipe);
    }
    Ref<Angular2Pipe> res = new Ref<>();
    processMetadataEntities(project, name, Angular2MetadataPipe.class, Angular2MetadataPipeIndex.KEY, p -> {
      res.set(p);
      return false;
    });
    return res.get();
  }

  @NotNull
  public static List<Angular2Directive> findDirectives(@NotNull Angular2DirectiveSelectorPsiElement selector) {
    if (selector.isElementSelector()) {
      return findElementDirectivesCandidates(selector.getProject(), selector.getName());
    }
    else if (selector.isAttributeSelector()) {
      return findAttributeDirectivesCandidates(selector.getProject(), selector.getName());
    }
    return Collections.emptyList();
  }

  @Nullable
  public static Angular2Component findComponent(@NotNull Angular2DirectiveSelectorPsiElement selector) {
    return (Angular2Component)ContainerUtil.find(findDirectives(selector), Angular2Directive::isComponent);
  }

  @NotNull
  public static Map<String, List<Angular2Directive>> getAllElementDirectives(@NotNull Project project) {
    return Stream.concat(
      AngularIndexUtil.getAllKeys(Angular2SourceDirectiveIndex.KEY, project).stream(),
      AngularIndexUtil.getAllKeys(Angular2MetadataDirectiveIndex.KEY, project).stream())
      .filter(name -> Angular2EntityUtils.isElementDirectiveIndexName(name)
                      && !Angular2EntityUtils.getElementName(name).isEmpty())
      .collect(toMap(name -> Angular2EntityUtils.getElementName(name),
                     name -> findDirectivesCandidates(project, name),
                     ContainerUtil::concat));
  }

  @NotNull
  public static Collection<String> getAllPipeNames(@NotNull Project project) {
    return newHashSet(concat(AngularIndexUtil.getAllKeys(Angular2SourcePipeIndex.KEY, project),
                             AngularIndexUtil.getAllKeys(Angular2MetadataPipeIndex.KEY, project)));
  }

  public static boolean isPipeTransformMethod(@Nullable PsiElement element) {
    return element instanceof TypeScriptFunction
           && TRANSFORM_METHOD.equals(((TypeScriptFunction)element).getName())
           && getPipe(element) != null;
  }

  public static MultiMap<Angular2Declaration, Angular2Module> getExportedDeclarationToModuleMap(@NotNull Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      MultiMap<Angular2Declaration, Angular2Module> result = new MultiMap<>();
      getAllModules(project).forEach(
        module -> module.getAllExportedDeclarations().forEach(
          decl -> result.putValue(decl, module)));
      return create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  public static MultiMap<Angular2Declaration, Angular2Module> getDeclarationToModuleMap(@NotNull Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      MultiMap<Angular2Declaration, Angular2Module> result = new MultiMap<>();
      getAllModules(project).forEach(
        module -> module.getDeclarations().forEach(
          decl -> result.putValue(decl, module)));
      return create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  public static List<Angular2Module> getAllModules(@NotNull Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
      List<Angular2Module> result = new ArrayList<>();
      StubIndex.getInstance().processElements(Angular2SourceModuleIndex.KEY, NG_MODULE_INDEX_NAME,
                                              project, GlobalSearchScope.allScope(project),
                                              JSImplicitElementProvider.class, (module) -> {
          ContainerUtil.addIfNotNull(result, getModule(module));
          return true;
        });
      StubIndex.getInstance().processElements(Angular2MetadataModuleIndex.KEY, NG_MODULE_INDEX_NAME,
                                              project, GlobalSearchScope.allScope(project),
                                              Angular2MetadataModule.class, (module) -> {
          result.add(module);
          return true;
        });
      return create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  @NotNull
  private static List<Angular2Directive> findDirectivesCandidates(@NotNull Project project, @NotNull String indexLookupName) {
    List<Angular2Directive> result = new ArrayList<>();
    StubIndex.getInstance().processElements(
      Angular2SourceDirectiveIndex.KEY, indexLookupName, project, GlobalSearchScope.allScope(project), JSImplicitElementProvider.class,
      provider -> {
        final JSElementIndexingData indexingData = provider.getIndexingData();
        if (indexingData != null) {
          final Collection<JSImplicitElement> elements = indexingData.getImplicitElements();
          if (elements != null) {
            for (JSImplicitElement element : elements) {
              if (element.isValid()) {
                Angular2Directive directive = getDirective(element);
                if (directive != null) {
                  result.add(directive);
                  return true;
                }
              }
            }
          }
        }
        return true;
      }
    );
    processMetadataEntities(project, indexLookupName, Angular2MetadataDirectiveBase.class, Angular2MetadataDirectiveIndex.KEY, el -> {
      result.add(el);
      return true;
    });
    return result;
  }

  private static <T extends Angular2MetadataEntity> void processMetadataEntities(@NotNull Project project,
                                                                                 @NotNull String name,
                                                                                 @NotNull Class<T> entityClass,
                                                                                 @NotNull StubIndexKey<String, T> key,
                                                                                 @NotNull Processor<T> processor) {
    StubIndex.getInstance().processElements(key, name, project, GlobalSearchScope.allScope(project), entityClass, el -> {
      if (el.isValid()) {
        return processor.process(el);
      }
      return true;
    });
  }

  private static final EntityGetter<Angular2Directive> DIRECTIVE_GETTER = new EntityGetter<>(
    Angular2Directive.class, Angular2IndexingHandler::isDirective,
    (dec, el) -> DIRECTIVE_DEC.equals(dec.getDecoratorName())
                 ? new Angular2SourceDirective(dec, el)
                 : new Angular2SourceComponent(dec, el),
    DIRECTIVE_DEC, COMPONENT_DEC);

  private static final EntityGetter<Angular2Pipe> PIPE_GETTER = new EntityGetter<>(
    Angular2Pipe.class, Angular2IndexingHandler::isPipe, Angular2SourcePipe::new, PIPE_DEC);

  private static final EntityGetter<Angular2Module> MODULE_GETTER = new EntityGetter<>(
    Angular2Module.class, Angular2IndexingHandler::isModule, Angular2SourceModule::new, MODULE_DEC);

  private static class EntityGetter<T extends Angular2Entity> {

    private final Class<T> myEntityClass;
    private final Condition<JSImplicitElement> myImplicitElementTester;
    private final BiFunction<ES6Decorator, JSImplicitElement, T> myEntityConstructor;
    private final String[] myDecoratorNames;

    private EntityGetter(@NotNull Class<T> aClass,
                         @NotNull Condition<JSImplicitElement> tester,
                         @NotNull BiFunction<ES6Decorator, JSImplicitElement, T> constructor,
                         @NotNull String... names) {
      myEntityClass = aClass;
      myImplicitElementTester = tester;
      myEntityConstructor = constructor;
      myDecoratorNames = names;
    }

    public T get(@Nullable PsiElement element) {
      if (element instanceof JSImplicitElement) {
        if (!myImplicitElementTester.value((JSImplicitElement)element)) {
          return null;
        }
        element = element.getContext();
      }
      if (element instanceof TypeScriptClass
          && Angular2LangUtil.isAngular2Context(element)) {
        ES6Decorator decorator = findDecorator((TypeScriptClass)element, myDecoratorNames);
        if (decorator != null) {
          element = decorator;
        }
        else {
          TypeScriptClass typeScriptClass = (TypeScriptClass)element;
          String className = typeScriptClass.getName();
          if (className == null
              ////performance check
              //|| !className.endsWith("Pipe")
              //check classes only from d.ts files
              || !Objects.requireNonNull(typeScriptClass.getAttributeList()).hasModifier(JSAttributeList.ModifierType.DECLARE)) {
            return null;
          }
          Ref<T> result = new Ref<>();
          StubIndex.getInstance().processElements(
            Angular2MetadataEntityClassNameIndex.KEY, className, typeScriptClass.getProject(),
            GlobalSearchScope.allScope(typeScriptClass.getProject()), Angular2MetadataEntity.class,
            e -> {
              if (e.isValid() && myEntityClass.isInstance(e) && e.getTypeScriptClass() == typeScriptClass) {
                //noinspection unchecked
                result.set((T)e);
                return false;
              }
              return true;
            });
          return result.get();
        }
      }
      if (element instanceof ES6Decorator) {
        ES6Decorator dec = (ES6Decorator)element;
        if (!ArrayUtil.contains(dec.getDecoratorName(), myDecoratorNames)
            || !Angular2LangUtil.isAngular2Context(element)) {
          return null;
        }
        return CachedValuesManager.getCachedValue(dec, () -> {
          JSImplicitElement entityElement = null;
          if (dec.getIndexingData() != null) {
            entityElement = ContainerUtil.find(ObjectUtils.notNull(dec.getIndexingData().getImplicitElements(), Collections::emptyList),
                                               myImplicitElementTester);
          }
          return create(entityElement != null ? myEntityConstructor.apply(dec, entityElement) : null, dec);
        });
      }
      return null;
    }
  }
}
