// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.entities;

import com.intellij.lang.javascript.psi.JSImplicitElementProvider;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import one.util.streamex.StreamEx;
import org.angular2.entities.ivy.Angular2IvyUtil;
import org.angular2.entities.metadata.Angular2MetadataUtil;
import org.angular2.entities.metadata.psi.Angular2MetadataDirectiveBase;
import org.angular2.entities.metadata.psi.Angular2MetadataEntity;
import org.angular2.entities.metadata.psi.Angular2MetadataModule;
import org.angular2.entities.metadata.psi.Angular2MetadataPipe;
import org.angular2.entities.source.*;
import org.angular2.index.*;
import org.angular2.lang.selector.Angular2DirectiveSimpleSelector;
import org.angularjs.index.AngularIndexUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.intellij.psi.util.CachedValueProvider.Result.create;
import static com.intellij.util.ObjectUtils.tryCast;
import static java.util.stream.Collectors.toMap;
import static org.angular2.Angular2DecoratorUtil.*;
import static org.angular2.entities.Angular2EntityUtils.*;
import static org.angular2.entities.ivy.Angular2IvyUtil.getIvyEntity;
import static org.angular2.entities.ivy.Angular2IvyUtil.hasIvyMetadata;
import static org.angular2.index.Angular2IndexingHandler.*;
import static org.angular2.lang.Angular2LangUtil.isAngular2Context;

public final class Angular2EntitiesProvider {

  public static final String TRANSFORM_METHOD = "transform";

  public static Angular2Entity getEntity(@Nullable PsiElement element) {
    if (element == null) {
      return null;
    }
    Angular2Entity result = getSourceEntity(element);
    if (result != null) {
      return result;
    }
    return Angular2EntitiesProvider.<Angular2Entity, PsiElement>withJsonMetadataFallback(
      element, Angular2IvyUtil::getIvyEntity, Angular2MetadataUtil::getMetadataEntity);
  }

  public static <R, E extends PsiElement> @Nullable R withJsonMetadataFallback(E element,
                                                                               Function<E, R> ivy,
                                                                               Function<TypeScriptClass, R> jsonFallback) {
    R result = ivy.apply(element);
    if (result == null
        && element instanceof TypeScriptClass
        && !hasIvyMetadata(element)
        && isAngular2Context(element)) {
      return jsonFallback.apply((TypeScriptClass)element);
    }
    return result;
  }

  public static @Nullable Angular2Declaration getDeclaration(@Nullable PsiElement element) {
    return tryCast(getEntity(element), Angular2Declaration.class);
  }

  public static @Nullable Angular2Component getComponent(@Nullable PsiElement element) {
    return tryCast(getEntity(element), Angular2Component.class);
  }

  public static @Nullable Angular2Directive getDirective(@Nullable PsiElement element) {
    return tryCast(getEntity(element), Angular2Directive.class);
  }

  public static @Nullable Angular2Pipe getPipe(@Nullable PsiElement element) {
    if (element instanceof TypeScriptFunction
        && TRANSFORM_METHOD.equals(((TypeScriptFunction)element).getName())
        && element.getContext() instanceof TypeScriptClass) {
      element = element.getContext();
    }
    return tryCast(getEntity(element), Angular2Pipe.class);
  }

  public static @Nullable Angular2Module getModule(@Nullable PsiElement element) {
    return tryCast(getEntity(element), Angular2Module.class);
  }

  public static @NotNull List<Angular2Directive> findElementDirectivesCandidates(@NotNull Project project, @NotNull String elementName) {
    return findDirectivesCandidates(project, getElementDirectiveIndexName(elementName));
  }

  public static @NotNull List<Angular2Directive> findAttributeDirectivesCandidates(@NotNull Project project,
                                                                                   @NotNull String attributeName) {
    return findDirectivesCandidates(project, getAttributeDirectiveIndexName(attributeName));
  }

  public static @NotNull List<Angular2Pipe> findPipes(@NotNull Project project, @NotNull String name) {
    List<Angular2Pipe> result = new SmartList<>();
    AngularIndexUtil.multiResolve(
      project, Angular2SourcePipeIndex.KEY, name, pipe -> {
        ContainerUtil.addIfNotNull(result, tryCast(getSourceEntity(pipe), Angular2Pipe.class));
        return true;
      });
    processIvyEntities(project, name, Angular2IvyPipeIndex.KEY, Angular2Pipe.class, result::add);
    processMetadataEntities(project, name, Angular2MetadataPipe.class, Angular2MetadataPipeIndex.KEY, result::add);
    return result;
  }

  public static @NotNull List<Angular2Directive> findDirectives(@NotNull Angular2DirectiveSelectorSymbol selector) {
    if (selector.isElementSelector()) {
      return findElementDirectivesCandidates(selector.getProject(), selector.getName());
    }
    else if (selector.isAttributeSelector()) {
      return findAttributeDirectivesCandidates(selector.getProject(), selector.getName());
    }
    return Collections.emptyList();
  }

  public static @Nullable Angular2Component findComponent(@NotNull Angular2DirectiveSelectorSymbol selector) {
    return (Angular2Component)ContainerUtil.find(findDirectives(selector), Angular2Directive::isComponent);
  }

  public static @NotNull Map<String, List<Angular2Directive>> getAllElementDirectives(@NotNull Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> create(
      StreamEx.of(findDirectivesCandidates(project, getAnyElementDirectiveIndexName()))
        .flatCollection(directive -> {
          List<Pair<String, Angular2Directive>> result = new SmartList<>();
          Consumer<Angular2DirectiveSimpleSelector> selectorProcessor = sel -> {
            String elementName = sel.getElementName();
            if (!StringUtil.isEmpty(elementName) && !"*".equals(elementName)) {
              result.add(Pair.pair(elementName, directive));
            }
          };
          for (Angular2DirectiveSimpleSelector sel : directive.getSelector().getSimpleSelectors()) {
            selectorProcessor.accept(sel);
            sel.getNotSelectors().forEach(selectorProcessor);
          }
          return result;
        })
        .groupingBy(p -> p.first, Collectors.mapping(p -> p.second, Collectors.toList())),
      PsiModificationTracker.MODIFICATION_COUNT)
    );
  }

  public static @NotNull Map<String, List<Angular2Pipe>> getAllPipes(@NotNull Project project) {
    return CachedValuesManager.getManager(project).getCachedValue(project, () -> create(
      StreamEx.of(AngularIndexUtil.getAllKeys(Angular2SourcePipeIndex.KEY, project))
        .append(AngularIndexUtil.getAllKeys(Angular2MetadataPipeIndex.KEY, project))
        .append(AngularIndexUtil.getAllKeys(Angular2IvyPipeIndex.KEY, project))
        .distinct()
        .collect(toMap(Function.identity(),
                       name -> findPipes(project, name))),
      PsiModificationTracker.MODIFICATION_COUNT)
    );
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
          if (module.isValid()) {
            ContainerUtil.addIfNotNull(result, tryCast(getSourceEntity(module), Angular2Module.class));
          }
          return true;
        });
      processIvyEntities(project, NG_MODULE_INDEX_NAME, Angular2IvyModuleIndex.KEY, Angular2Module.class, result::add);
      processMetadataEntities(project, NG_MODULE_INDEX_NAME, Angular2MetadataModule.class,
                              Angular2MetadataModuleIndex.KEY, result::add);
      return create(result, PsiModificationTracker.MODIFICATION_COUNT);
    });
  }

  public static Angular2SourceEntity getSourceEntity(@NotNull PsiElement element) {
    if (element instanceof JSImplicitElement) {
      if (!isEntityImplicitElement((JSImplicitElement)element)) {
        return null;
      }
      element = element.getContext();
    }
    if (element instanceof TypeScriptClass) {
      element = findDecorator((TypeScriptClass)element, PIPE_DEC, COMPONENT_DEC, MODULE_DEC, DIRECTIVE_DEC);
      if (element == null) {
        return null;
      }
    }
    else if (!(element instanceof ES6Decorator)
             || !isAngularEntityDecorator((ES6Decorator)element, PIPE_DEC, COMPONENT_DEC, MODULE_DEC, DIRECTIVE_DEC)) {
      return null;
    }
    ES6Decorator dec = (ES6Decorator)element;
    return CachedValuesManager.getCachedValue(dec, () -> {
      JSImplicitElement entityElement = null;
      if (dec.getIndexingData() != null) {
        entityElement = ContainerUtil.find(ObjectUtils.notNull(dec.getIndexingData().getImplicitElements(),
                                                               Collections::emptyList),
                                           Angular2EntitiesProvider::isEntityImplicitElement);
      }
      String decoratorName = dec.getDecoratorName();
      Angular2SourceEntity entity = null;
      if (entityElement != null && decoratorName != null) {
        if (decoratorName.equals(COMPONENT_DEC)) {
          entity = new Angular2SourceComponent(dec, entityElement);
        }
        else if (decoratorName.equals(DIRECTIVE_DEC)) {
          entity = new Angular2SourceDirective(dec, entityElement);
        }
        else if (decoratorName.equals(MODULE_DEC)) {
          entity = new Angular2SourceModule(dec, entityElement);
        }
        else if (decoratorName.equals(PIPE_DEC)) {
          entity = new Angular2SourcePipe(dec, entityElement);
        }
      }
      return create(entity, dec);
    });
  }

  public static boolean isDeclaredClass(@NotNull TypeScriptClass typeScriptClass) {
    return Objects.requireNonNull(typeScriptClass.getAttributeList()).hasModifier(JSAttributeList.ModifierType.DECLARE);
  }

  private static boolean isEntityImplicitElement(JSImplicitElement element) {
    return isDirective(element) || isPipe(element) || isModule(element);
  }

  private static @NotNull List<Angular2Directive> findDirectivesCandidates(@NotNull Project project, @NotNull String indexLookupName) {
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
                Angular2Directive directive = tryCast(getSourceEntity(element), Angular2Directive.class);
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
    processIvyEntities(project, indexLookupName, Angular2IvyDirectiveIndex.KEY, Angular2Directive.class, result::add);
    processMetadataEntities(project, indexLookupName, Angular2MetadataDirectiveBase.class,
                            Angular2MetadataDirectiveIndex.KEY, result::add);
    return result;
  }

  private static <T extends Angular2MetadataEntity<?>> void processMetadataEntities(@NotNull Project project,
                                                                                    @NotNull String name,
                                                                                    @NotNull Class<T> entityClass,
                                                                                    @NotNull StubIndexKey<String, T> key,
                                                                                    @NotNull Consumer<? super T> consumer) {
    StubIndex.getInstance().processElements(key, name, project, GlobalSearchScope.allScope(project), entityClass, el -> {
      if (el.isValid() && !hasIvyMetadata(el)) {
        consumer.accept(el);
      }
      return true;
    });
  }

  private static <T extends Angular2Entity> void processIvyEntities(@NotNull Project project,
                                                                    @NotNull String name,
                                                                    @NotNull StubIndexKey<String, TypeScriptClass> key,
                                                                    @NotNull Class<T> entityClass,
                                                                    @NotNull Consumer<? super T> consumer) {
    StubIndex.getInstance().processElements(key, name, project, GlobalSearchScope.allScope(project), TypeScriptClass.class, el -> {
      if (el.isValid()) {
        T entity = tryCast(getIvyEntity(el), entityClass);
        if (entity != null) {
          consumer.accept(entity);
        }
      }
      return true;
    });
  }
}
