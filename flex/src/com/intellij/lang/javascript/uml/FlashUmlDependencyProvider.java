package com.intellij.lang.javascript.uml;

import com.intellij.javascript.flex.FlexReferenceContributor;
import com.intellij.lang.Language;
import com.intellij.lang.injection.InjectedLanguageManager;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSGenericSignature;
import com.intellij.lang.javascript.psi.ecmal4.JSImportStatement;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.uml.FlashUmlRelationship.Factory;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssElementVisitor;
import com.intellij.psi.css.CssFunction;
import com.intellij.psi.css.CssString;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FlashUmlDependencyProvider {
  private static final Language CSS = Language.findLanguageByID("CSS");

  private final JSClass myClazz;

  public FlashUmlDependencyProvider(final JSClass clazz) {
    myClazz = clazz;
  }

  public Collection<Pair<JSClass, FlashUmlRelationship>> computeUsedClasses() {
    final Collection<Pair<JSClass, FlashUmlRelationship>> result = new ArrayList<>();
    final JSElementVisitor visitor = new JSElementVisitor() {
      JSVariable myVariable;
      JSNewExpression myNewExpression;
      boolean myInField;
      boolean myInParameter;

      @Override
      public void visitJSReferenceExpression(final JSReferenceExpression node) {
        if (PsiTreeUtil.getParentOfType(node, JSImportStatement.class) != null) {
          return;
        }

        if (myVariable == null && myNewExpression == null && !myInParameter && isReturnTypeReference(node)) {
          return;
        }

        PsiElement resolved = node.resolve();
        if (myNewExpression != null && resolved instanceof JSFunction) {
          if (((JSFunction)resolved).isConstructor()) {
            resolved = JSResolveUtil.findParent(resolved);
          }
        }

        if (resolved instanceof JSClass) {
          FlashUmlRelationship relType;
          if (node.getParent() instanceof JSReferenceExpression) {
            relType = Factory.dependency(myInField ? myVariable.getName() : null, myVariable != null ? myVariable : node);
          }
          else if (myNewExpression != null) {
            if (node.getParent() instanceof JSGenericSignature) {
              relType = Factory.dependency(myInField ? myVariable.getName() : null, myVariable != null ? myVariable : node);
            }
            else {
              relType = Factory.create(myNewExpression);
            }
          }
          else if (myInField && node.getParent() instanceof JSGenericSignature) {
            assert myVariable != null;
            String qName = ((JSClass)resolved).getQualifiedName();
            if (FlashUmlVfsResolver.isVectorType(qName)) {
              relType = Factory.dependency(myVariable.getName(), myVariable);
            }
            else {
              relType = Factory.oneToMany(myVariable.getName(), myVariable);
            }
          }
          else if (myInField) {
            assert myVariable != null;
            String qName = ((JSClass)resolved).getQualifiedName();
            if (FlashUmlVfsResolver.isVectorType(qName)) {
              relType = Factory.dependency(myVariable.getName(), myVariable);
            }
            else {
              relType = Factory.oneToOne(myVariable.getName(), myVariable);
            }
          }
          else {
            relType = Factory.dependency(null, myVariable != null ? myVariable : node);
          }
          result.add(Pair.create((JSClass)resolved, relType));
        }

        super.visitJSReferenceExpression(node);
      }

      @Override
      public void visitJSVariable(final JSVariable node) {
        if (node instanceof JSParameter) {
          myInParameter = true;
        }
        else {
          myVariable = node;
        }
        myInField = JSResolveUtil.findParent(node) instanceof JSClass;
        try {
          super.visitJSVariable(node);
        }
        finally {
          myVariable = null;
          myInField = false;
          myInParameter = false;
        }
      }

      @Override
      public void visitJSNewExpression(final JSNewExpression node) {
        myNewExpression = node;
        try {
          super.visitJSNewExpression(node);
        }
        finally {
          myNewExpression = null;
        }
      }

      @Override
      public void visitElement(@NotNull final PsiElement element) {
        super.visitElement(element);
        element.acceptChildren(this);
      }
    };

    if (myClazz instanceof XmlBackedJSClassImpl) {
      // TODO process attributes
      ((XmlBackedJSClassImpl)myClazz).processInjectedFiles(jsFile -> {
        jsFile.accept(visitor);
        return true;
      });

      myClazz.getParent().acceptChildren(new XmlElementVisitor() { // don't visit parent tag
        private String myInClassAttributeName; // also to prevent extra references resolve

        @Override
        public void visitXmlTag(final @NotNull XmlTag tag) {
          XmlElementDescriptor descriptor = tag.getDescriptor();
          if (descriptor != null) {
            PsiElement declaration = descriptor.getDeclaration();
            if (declaration instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)declaration)) {
              declaration = XmlBackedJSClassFactory.getXmlBackedClass((XmlFile)declaration);
            }
            if (declaration instanceof JSClass) {
              XmlAttribute id = tag.getAttribute("id");
              FlashUmlRelationship type = id != null && StringUtil.isNotEmpty(id.getValue()) ?
                                              Factory.oneToOne(id.getValue(), id) : Factory.dependency(null, tag);
              result.add(Pair.create((JSClass)declaration, type));
            }
          }
          super.visitXmlTag(tag);
        }

        @Override
        public void visitXmlAttribute(final @NotNull XmlAttribute attribute) {
          XmlAttributeDescriptor descriptor = attribute.getDescriptor();
          if (descriptor instanceof AnnotationBackedDescriptor) {
            if (FlexReferenceContributor.isClassReferenceType(((AnnotationBackedDescriptor)descriptor).getType())) {
              myInClassAttributeName = StringUtil.notNullize(attribute.getName());
              try {
                super.visitXmlAttribute(attribute);
              }
              finally {
                myInClassAttributeName = null;
              }
            }
          }
        }

        @Override
        public void visitXmlAttributeValue(final @NotNull XmlAttributeValue value) {
          if (myInClassAttributeName != null) {
            processReferenceSet(value.getReferences(), result, Factory.dependency(myInClassAttributeName, value.getParent()));
          }
        }

        @Override
        public void visitXmlText(final @NotNull XmlText text) {
          List<Pair<PsiElement, TextRange>> injectedFiles = InjectedLanguageManager.getInstance(text.getProject()).getInjectedPsiFiles(text);
          if (injectedFiles != null) {
            for (Pair<PsiElement, TextRange> pair : injectedFiles) {
              if (CSS.is(pair.first.getLanguage())) {
                pair.first.accept(new CssElementVisitor() {
                  private boolean myInClassReference; // to prevent extra references resolve

                  @Override
                  public void visitElement(@NotNull final PsiElement element) {
                    super.visitElement(element);
                    element.acceptChildren(this);
                  }

                  @Override
                  public void visitCssFunction(final CssFunction _function) {
                    if (FlexReferenceContributor.CLASS_REFERENCE.equals(_function.getName())) {
                      myInClassReference = true;
                      try {
                        super.visitCssFunction(_function);
                      }
                      finally {
                        myInClassReference = false;
                      }
                    }
                  }

                  @Override
                  public void visitCssString(final CssString _string) {
                    if (myInClassReference) {
                      CssDeclaration declaration = PsiTreeUtil.getParentOfType(_string, CssDeclaration.class);
                      if (declaration != null) {
                        processReferenceSet(_string.getReferences(), result,
                                            Factory.dependency(declaration.getPropertyName(), declaration));
                      }
                    }
                  }
                });
              }
            }
          }
          super.visitXmlText(text);
        }

        @Override
        public void visitElement(@NotNull final PsiElement element) {
          super.visitElement(element);
          element.acceptChildren(this);
        }
      });
    }

    myClazz.processDeclarations(new PsiScopeProcessor() {
      @Override
      public boolean execute(@NotNull final PsiElement element, @NotNull final ResolveState state) {
        element.accept(visitor);
        return true;
      }
    }, ResolveState.initial(), myClazz, myClazz);
    return result;
  }

  private static boolean isReturnTypeReference(final JSReferenceExpression node) {
    PsiElement parent = JSResolveUtil.getTopReferenceParent(node);
    return parent instanceof JSFunction && PsiTreeUtil.isAncestor(((JSFunction)parent).getReturnTypeElement(), node, true);
  }

  private static void processReferenceSet(final PsiReference[] references,
                                          final Collection<Pair<JSClass, FlashUmlRelationship>> result,
                                          final FlashUmlRelationship relType) {
    if (references.length > 0) {
      PsiElement element = references[references.length - 1].resolve();
      if (element instanceof JSClass) {
        result.add(Pair.create((JSClass)element, relType));
      }
    }
  }
}
