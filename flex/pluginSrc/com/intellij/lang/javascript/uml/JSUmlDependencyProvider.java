package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationships;
import com.intellij.javascript.flex.FlexReferenceContributor;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSGenericSignature;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.*;
import com.intellij.psi.scope.BaseScopeProcessor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;

import java.util.*;

/**
 * User: ksafonov
 */
public class JSUmlDependencyProvider {
  private final JSClass myClazz;

  public JSUmlDependencyProvider(final JSClass clazz) {
    myClazz = clazz;
  }

  public Collection<Pair<JSClass, DiagramRelationshipInfo>> computeUsedClasses() {
    final Map<JSClass, DiagramRelationshipInfo> result = new HashMap<JSClass, DiagramRelationshipInfo>();
    final JSElementVisitor visitor = new JSElementVisitor() {
      boolean myInVariable;
      boolean myInNewExpression;
      boolean myInField;

      @Override
      public void visitJSReferenceExpression(final JSReferenceExpression node) {
        if (!myInVariable && !myInNewExpression) {
          return;
        }

        if (myInNewExpression && node.getParent() instanceof JSGenericSignature) {
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
            relType = DiagramRelationships.CREATE;
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
          add(result, ((JSClass)resolved), relType);
        }

        super.visitJSReferenceExpression(node);
      }

      @Override
      public void visitJSVariable(final JSVariable node) {
        if (node instanceof JSParameter) {
          return;
        }
        myInVariable = true;
        myInField = JSResolveUtil.findParent(node) instanceof JSClass;
        try {
          super.visitJSVariable(node);
        }
        finally {
          myInVariable = false;
          myInField = false;
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
              add(result, (JSClass)declaration, DiagramRelationships.TO_ONE);
            }
          }
          super.visitXmlTag(tag);
        }

        @Override
        public void visitXmlAttribute(final XmlAttribute attribute) {
          XmlAttributeDescriptor descriptor = attribute.getDescriptor();
          if (descriptor instanceof AnnotationBackedDescriptor) {
            if (FlexReferenceContributor.isObjectType(((AnnotationBackedDescriptor)descriptor).getType())) {
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
            PsiReference[] references = value.getReferences();
            if (references.length > 0) {
              PsiElement element = references[references.length - 1].resolve();
              if (element instanceof JSClass) {
                add(result, (JSClass)element, DiagramRelationships.TO_ONE);
              }
            }
          }
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
    return ContainerUtil
      .map(result.entrySet(), new Function<Map.Entry<JSClass, DiagramRelationshipInfo>, Pair<JSClass, DiagramRelationshipInfo>>() {
        @Override
        public Pair<JSClass, DiagramRelationshipInfo> fun(final Map.Entry<JSClass, DiagramRelationshipInfo> e) {
          return Pair.create(e.getKey(), e.getValue());
        }
      });
  }

  public Collection<Pair<JSClass, DiagramRelationshipInfo>> computeUsingClasses() {
    final Collection<Pair<JSClass, DiagramRelationshipInfo>> result = new ArrayList<Pair<JSClass, DiagramRelationshipInfo>>();
    return result;
  }

  private static void add(final Map<JSClass, DiagramRelationshipInfo> result, final JSClass jsClass, final DiagramRelationshipInfo type) {
    DiagramRelationshipInfo existingRelType = result.get(jsClass);
    if (existingRelType == null || getWeight(existingRelType) < getWeight(type)) {
      result.put(jsClass, type);
    }
  }

  private static int getWeight(DiagramRelationshipInfo relType) {
    if (relType == DiagramRelationships.DEPENDENCY) return 0;
    if (relType == DiagramRelationships.TO_ONE) return 1;
    if (relType == DiagramRelationships.TO_MANY) return 2;
    if (relType == DiagramRelationships.CREATE) return 3;
    assert false : relType;
    return 0;
  }
}
