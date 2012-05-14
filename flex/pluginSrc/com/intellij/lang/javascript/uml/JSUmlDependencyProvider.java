package com.intellij.lang.javascript.uml;

import com.intellij.diagram.DiagramRelationshipInfo;
import com.intellij.diagram.DiagramRelationships;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSGenericSignature;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.scope.BaseScopeProcessor;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xml.XmlElementDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

        if (resolved instanceof JSClass && !JSPsiImplUtils.isTheSameClass(resolved, myClazz)) {
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
        @Override
        public void visitXmlTag(final XmlTag tag) {
          XmlElementDescriptor descriptor = tag.getDescriptor();
          if (descriptor != null) {
            PsiElement declaration = descriptor.getDeclaration();
            if (declaration instanceof JSClass && !JSPsiImplUtils.isTheSameClass(declaration, myClazz)) {
              add(result, (JSClass)declaration, DiagramRelationships.TO_ONE);
            }
          }
          super.visitXmlTag(tag);
        }

        @Override
        public void visitElement(final PsiElement element) {
          super.visitElement(element);
          element.acceptChildren(this);
        }
      });
    }
    else {
      //myClazz.accept(visitor);
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

  private static void add(final Map<JSClass, DiagramRelationshipInfo> result,
                          final JSClass jsClass,
                          final DiagramRelationshipInfo newRelType) {

    // CREATE wins over TO_MANY which wins over TO_ONE
    DiagramRelationshipInfo existingRelType = result.get(jsClass);
    if (existingRelType == DiagramRelationships.CREATE) {
      return;
    }

    if (existingRelType == DiagramRelationships.TO_MANY && newRelType != DiagramRelationships.CREATE) {
      return;
    }

    result.put(jsClass, newRelType);
  }

  public Collection<Pair<JSClass, DiagramRelationshipInfo>> computeUsingClasses() {
    final Collection<Pair<JSClass, DiagramRelationshipInfo>> result = new ArrayList<Pair<JSClass, DiagramRelationshipInfo>>();
    return result;
  }
}
