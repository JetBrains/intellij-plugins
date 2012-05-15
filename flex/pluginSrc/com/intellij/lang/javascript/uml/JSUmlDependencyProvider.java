package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationships;
import com.intellij.javascript.flex.FlexReferenceContributor;
import com.intellij.lang.Language;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSGenericSignature;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.css.CssElementVisitor;
import com.intellij.psi.css.CssFunction;
import com.intellij.psi.css.CssString;
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil;
import com.intellij.psi.scope.BaseScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.*;
import com.intellij.util.Processor;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: ksafonov
 */
public class JSUmlDependencyProvider {
  private static final Language CSS = Language.findLanguageByID("CSS");

  private final JSClass myClazz;

  public JSUmlDependencyProvider(final JSClass clazz) {
    myClazz = clazz;
  }

  public Collection<Pair<JSClass, DiagramRelationshipInfo>> computeUsedClasses() {
    final Collection<Pair<JSClass, DiagramRelationshipInfo>> result = new ArrayList<Pair<JSClass, DiagramRelationshipInfo>>();
    final JSElementVisitor visitor = new JSElementVisitor() {
      boolean myInVariable;
      boolean myInNewExpression;
      boolean myInField;
      boolean myInParameter;

      @Override
      public void visitJSReferenceExpression(final JSReferenceExpression node) {
        if (!myInVariable && !myInNewExpression && !myInParameter && isReturnTypeReference(node)) {
          return;
        }

        PsiElement resolved = node.resolve();
        if (myInNewExpression && resolved instanceof JSFunction) {
          if (((JSFunction)resolved).isConstructor()) {
            resolved = JSResolveUtil.findParent(resolved);
          }
        }

        if (resolved instanceof JSClass) {
          DiagramRelationshipInfo relType;
          if (myInNewExpression) {
            if (node.getParent() instanceof JSGenericSignature) {
              relType = DiagramRelationships.DEPENDENCY;
            }
            else {
              relType = DiagramRelationships.CREATE;
            }
          }
          else if (myInField && node.getParent() instanceof JSGenericSignature) {
            relType = DiagramRelationships.TO_MANY;
          }
          else if (myInField) {
            relType = DiagramRelationships.TO_ONE;
          }
          else {
            relType = DiagramRelationships.DEPENDENCY;
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
          myInVariable = true;
        }
        myInField = JSResolveUtil.findParent(node) instanceof JSClass;
        try {
          super.visitJSVariable(node);
        }
        finally {
          myInVariable = false;
          myInField = false;
          myInParameter = false;
        }
      }

      @Override
      public void visitJSNewExpression(final JSNewExpression node) {
        myInNewExpression = true;
        try {
          super.visitJSNewExpression(node);
        }
        finally {
          myInNewExpression = false;
        }
      }

      @Override
      public void visitElement(final PsiElement element) {
        super.visitElement(element);
        element.acceptChildren(this);
      }
    };

    if (myClazz instanceof XmlBackedJSClassImpl) {
      // TODO process attributes
      ((XmlBackedJSClassImpl)myClazz).processInjectedFiles(new Processor<JSFile>() {
        @Override
        public boolean process(final JSFile jsFile) {
          jsFile.accept(visitor);
          return true;
        }
      });

      myClazz.getParent().acceptChildren(new XmlElementVisitor() { // don't visit parent tag
        private boolean myInClassAttribute; // used to prevent extra references resolve

        @Override
        public void visitXmlTag(final XmlTag tag) {
          XmlElementDescriptor descriptor = tag.getDescriptor();
          if (descriptor != null) {
            PsiElement declaration = descriptor.getDeclaration();
            if (declaration instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile((PsiFile)declaration)) {
              declaration = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)declaration);
            }
            if (declaration instanceof JSClass) {
              DiagramRelationshipInfo type =
                StringUtil.isNotEmpty(tag.getAttributeValue("id")) ? DiagramRelationships.TO_ONE : DiagramRelationships.DEPENDENCY;
              result.add(Pair.create((JSClass)declaration, type));
            }
          }
          super.visitXmlTag(tag);
        }

        @Override
        public void visitXmlAttribute(final XmlAttribute attribute) {
          XmlAttributeDescriptor descriptor = attribute.getDescriptor();
          if (descriptor instanceof AnnotationBackedDescriptor) {
            if (FlexReferenceContributor.isClassReferenceType(((AnnotationBackedDescriptor)descriptor).getType())) {
              myInClassAttribute = true;
              try {
                super.visitXmlAttribute(attribute);
              }
              finally {
                myInClassAttribute = false;
              }
            }
          }
        }

        @Override
        public void visitXmlAttributeValue(final XmlAttributeValue value) {
          if (myInClassAttribute) {
            processReferenceSet(value.getReferences(), result, DiagramRelationships.DEPENDENCY);
          }
        }

        @Override
        public void visitXmlText(final XmlText text) {
          List<Pair<PsiElement, TextRange>> injectedFiles = InjectedLanguageUtil.getInjectedPsiFiles(text);
          if (injectedFiles != null) {
            for (Pair<PsiElement, TextRange> pair : injectedFiles) {
              if (CSS.is(pair.first.getLanguage())) {
                pair.first.accept(new CssElementVisitor() {
                  private boolean myInClassReference; // used to prevent extra references resolve

                  @Override
                  public void visitElement(final PsiElement element) {
                    super.visitElement(element);
                    element.acceptChildren(this);
                  }

                  @Override
                  public void visitCssFunction(final CssFunction _function) {
                    if (FlexReferenceContributor.CLASS_REFERENCE.equals(_function.getFunctionName())) {
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
                      processReferenceSet(_string.getReferences(), result, DiagramRelationships.DEPENDENCY);
                    }
                  }
                });
              }
            }
          }
          super.visitXmlText(text);
        }

        @Override
        public void visitElement(final PsiElement element) {
          super.visitElement(element);
          element.acceptChildren(this);
        }
      });
    }

    myClazz.processDeclarations(new BaseScopeProcessor() {
      @Override
      public boolean execute(final PsiElement element, final ResolveState state) {
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

  public Collection<Pair<JSClass, DiagramRelationshipInfo>> computeUsingClasses() {
    final Collection<Pair<JSClass, DiagramRelationshipInfo>> result = new ArrayList<Pair<JSClass, DiagramRelationshipInfo>>();
    return result;
  }

  private static void processReferenceSet(final PsiReference[] references,
                                          final Collection<Pair<JSClass, DiagramRelationshipInfo>> result,
                                          final DiagramRelationshipInfo relType) {
    if (references.length > 0) {
      PsiElement element = references[references.length - 1].resolve();
      if (element instanceof JSClass) {
        result.add(Pair.create((JSClass)element, relType));
      }
    }
  }
}
