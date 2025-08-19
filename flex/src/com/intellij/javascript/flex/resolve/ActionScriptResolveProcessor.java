// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.resolve;

import com.intellij.css.util.CssClassUtil;
import com.intellij.lang.javascript.psi.JSNamedElementBase;
import com.intellij.lang.javascript.psi.JSNewExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveProcessorBase;
import com.intellij.lang.javascript.psi.resolve.JSResolveProcessorEx;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.css.CssClassMarker;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public abstract class ActionScriptResolveProcessor extends JSResolveProcessorBase implements JSResolveProcessorEx {
  public static final Key<String> ASKING_FOR_QUALIFIED_IMPORT = Key.create("asking.for.import.of.qname");
  public static final Key<Boolean> LOOKING_FOR_USE_NAMESPACES = Key.create("looking.for.use.directive");
  private final Set<JSClass> visitedClasses = new HashSet<>();
  private final Set<JSClass> visitedClassesStatic = new HashSet<>();

  private boolean toProcessHierarchy;
  private boolean toSkipClassDeclarationOnce;
  private boolean toProcessMembers = true;
  private boolean toProcessActionScriptImplicits = true;
  private boolean encounteredDynamicClasses;
  private boolean encounteredDynamicClassesSet;

  private boolean myTypeContext;
  private boolean localResolve;
  protected boolean isWalkingUpTree;

  protected final PsiElement place;

  private boolean myNeedsAllVariants;
  private boolean myForceImportsForPlace;

  public ActionScriptResolveProcessor(final @Nullable String name) {
    this(name, null);
  }

  public ActionScriptResolveProcessor(final @Nullable String name, @Nullable PsiElement _place) {
    super(name);
    place = _place;
    accessibilityProcessingHandler = new ActionScriptAccessibilityProcessingHandler(place, false);

    if (place != null) {
      ResolveProcessor.ProcessingOptions
        processingOptionsOverride = place.getContainingFile().getOriginalFile().getUserData(ResolveProcessor.PROCESSING_OPTIONS);
      if (processingOptionsOverride != null) {
        setProcessingOptions(processingOptionsOverride);
      }
    }
  }

  public static boolean completeConstructorName(PsiElement place) {
    PsiElement placeParent;
    return (((placeParent = place.getParent()) instanceof JSNewExpression) &&
            ((JSNewExpression)placeParent).getMethodExpression() == place);
  }

  public void prefixResolved() {
  }

  public static @Nullable String getName(final PsiElement element) {
    if (element instanceof JSNamedElementBase) {
      return ((JSNamedElementBase)element).getName();
    }
    if (element instanceof XmlTag) return ((XmlTag)element).getAttributeValue("name");
    if (element instanceof XmlToken) return element.getText();
    if (element instanceof CssClassMarker) {
      String name = ((PsiNamedElement)element).getName();
      if (name != null) {
        return CssClassUtil.kebabToCamelCase(name);
      }
    }
    if (element instanceof PsiNamedElement) {
      return ((PsiNamedElement)element).getName();
    }
    return null;
  }

  @Override
  public void handleEvent(@NotNull Event event, Object associated) {
    if (event == Event.SET_DECLARATION_HOLDER) {
      boolean toProcessParent = true;

      if (associated instanceof JSClass jsClass) {

        if (!toSkipClassDeclarationOnce) {
          if (!encounteredDynamicClassesSet) {
            final JSAttributeList attributeList = jsClass.getAttributeList();
            if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.DYNAMIC)) {
              encounteredDynamicClasses = true;
            }
            encounteredDynamicClassesSet = true;
          }
        }
        else {
          toProcessParent = false;
        }
      }

      if (toProcessParent) {
        startingParent((PsiElement)associated);
      }
    }
    else if (event == ResolveProcessor.INHERITED_CLASSES_STARTED) {
      accessibilityProcessingHandler.setProcessingInheritedClasses(true);
    }
    else if (event == ResolveProcessor.INHERITED_CLASSES_FINISHED) {
      accessibilityProcessingHandler.setProcessingInheritedClasses(false);
    }
  }

  protected void startingParent(PsiElement associated) {
    accessibilityProcessingHandler.startingParent(associated);
  }

  @Override
  public boolean isToProcessHierarchy() {
    return toProcessHierarchy;
  }

  @Override
  public void setToProcessHierarchy(final boolean toProcessHierarchy) {
    this.toProcessHierarchy = toProcessHierarchy;
  }

  @Override
  public boolean isToProcessActionScriptImplicits() {
    return toProcessActionScriptImplicits;
  }

  @Override
  public void setToProcessActionScriptImplicits(boolean toProcessActionScriptImplicits) {
    this.toProcessActionScriptImplicits = toProcessActionScriptImplicits;
  }

  @Override
  public boolean isToSkipClassDeclarationOnce() {
    return toSkipClassDeclarationOnce;
  }

  @Override
  public void setToSkipClassDeclarationsOnce(final boolean toSkipClassDeclarationOnce) {
    this.toSkipClassDeclarationOnce = toSkipClassDeclarationOnce;
  }

  @Override
  public void setTypeContext(final boolean b) {
    myTypeContext = b;
  }

  @Override
  public boolean isTypeContext() {
    return myTypeContext;
  }

  @Override
  public boolean isToProcessMembers() {
    return toProcessMembers;
  }

  @Override
  public void setToProcessMembers(final boolean toProcessMembers) {
    this.toProcessMembers = toProcessMembers;
  }

  @Override
  public boolean checkVisited(@NotNull JSClass clazz) {
    Set<JSClass> visited = accessibilityProcessingHandler.isProcessStatics() ? visitedClassesStatic : visitedClasses;
    return !visited.add(clazz);
  }

  @Override
  public boolean isLocalResolve() {
    return localResolve;
  }

  @Override
  public boolean setWalkingUpTree(boolean isWalkingUpTree) {
    boolean wasWalkingUpTree = this.isWalkingUpTree;
    this.isWalkingUpTree = isWalkingUpTree;
    return wasWalkingUpTree;
  }

  @Override
  public void setLocalResolve(final boolean localResolve) {
    this.localResolve = localResolve;
  }

  public boolean specificallyAskingToResolveQualifiedNames() {
    return getUserData(ASKING_FOR_QUALIFIED_IMPORT) != null;
  }

  @Override
  public String getQualifiedNameToImport() {
    return getUserData(ASKING_FOR_QUALIFIED_IMPORT);
  }

  @Override
  public boolean lookingForUseNamespaces() {
    return getUserData(LOOKING_FOR_USE_NAMESPACES) != null;
  }

  @Override
  public boolean isEncounteredDynamicClasses() {
    return encounteredDynamicClasses;
  }

  public static void setSkipPackageLocalCheck(PsiElement el, boolean state) {
    el.putUserData(ResolveProcessor.skipResolveKey, state ? Boolean.TRUE : null);
  }

  public static boolean toSkipPackageLocalCheck(PsiElement el) {
    return el.getUserData(ResolveProcessor.skipResolveKey) != null;
  }

  public void setNeedsAllVariants() {
    myNeedsAllVariants = true;
  }

  @Override
  public boolean needsAllVariants() {
    return myNeedsAllVariants || specificallyAskingToResolveQualifiedNames();
  }

  @Override
  public boolean isForceImportsForPlace() {
    return myForceImportsForPlace;
  }

  public void setForceImportsForPlace(boolean forceImportsForPlace) {
    myForceImportsForPlace = forceImportsForPlace;
  }


  @Override
  public boolean needPackages() {
    return myProcessingOptions.needPackages();
  }

  private ResolveProcessor.ProcessingOptions myProcessingOptions = ResolveProcessor.DEFAULT_RESOLVE;

  @Override
  public @NotNull
  ResolveProcessor.ProcessingOptions getProcessingOptions() {
    return myProcessingOptions;
  }

  public void setProcessingOptions(@NotNull ResolveProcessor.ProcessingOptions processingOptions) {
    myProcessingOptions = processingOptions;
  }

  protected final @NotNull ActionScriptAccessibilityProcessingHandler accessibilityProcessingHandler;

  public @NotNull ActionScriptAccessibilityProcessingHandler getAccessibilityProcessingHandler() {
    return accessibilityProcessingHandler;
  }

  @Override
  public void configureClassScope(JSClass clazzOfContext) {
    accessibilityProcessingHandler.configureClassScope(clazzOfContext);
  }

  @Override
  public void setAllowUnqualifiedStaticsFromInstance(boolean b) {
    accessibilityProcessingHandler.setAllowUnqualifiedStaticsFromInstance(b);
  }

  @Override
  public void setTypeName(String qualifiedName) {
    accessibilityProcessingHandler.setTypeName(qualifiedName);
  }

  @Override
  public boolean skipTopLevelItems() {
    assert myName != null || place == null;    // if name=null and place!=null, the completion should go through CompletionResultSink
    return false;
  }

  @Override
  public boolean needTopLevelClassName(String name) {
    assert myName != null || place == null;    // if name=null and place!=null, the completion should go through CompletionResultSink
    return true;
  }

  @Override
  public boolean isProcessStatics() {
    return getAccessibilityProcessingHandler().isProcessStatics();
  }

  @Override
  public void setProcessStatics(boolean value) {
    getAccessibilityProcessingHandler().setProcessStatics(value);
  }

  @Override
  public @Nullable PsiElement getPlace() {
    return place;
  }
}
