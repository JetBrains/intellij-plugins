package org.angularjs.codeInsight.attributes;

import com.intellij.lang.javascript.index.AngularJSIndex;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import com.intellij.util.indexing.FileBasedIndex;
import gnu.trove.TObjectIntHashMap;
import org.angularjs.index.AngularJSIndexingHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dennis.Ushakov
 */
public class ControllerAttributeDescriptor extends AngularAttributeDescriptor {
  public ControllerAttributeDescriptor(Project project, VirtualFile file, int offset) {
    super(project, "ng-controller", file, offset);
  }

  @Override
  public String[] getEnumeratedValues() {
    if (myProject == null) return ArrayUtil.EMPTY_STRING_ARRAY;

    final Set<String> result = new HashSet<String>();
    FileBasedIndex.getInstance().processValues(AngularJSIndex.INDEX_ID, AngularJSIndexingHandler.CONTROLLER_KEY, null,
                                               new FileBasedIndex.ValueProcessor<TObjectIntHashMap<String>>() {
                                                 @Override
                                                 public boolean process(VirtualFile file, TObjectIntHashMap<String> descriptorNames) {
                                                   for (Object o : descriptorNames.keys()) {
                                                     result.add((String)o);
                                                   }
                                                   return true;
                                                 }
                                               }, GlobalSearchScope.allScope(myProject)
    );
    return ArrayUtil.toStringArray(result);
  }

  @Override
  public PsiReference[] getReferences(PsiElement element) {
    return new PsiReference[] {
      new ControllerReference((XmlAttributeValue)element)
    };
  }

  private class ControllerReference extends PsiReferenceBase<XmlAttributeValue> {
    public ControllerReference(XmlAttributeValue value) {
      super(value, value.getValueTextRange().shiftRight(1- value.getTextOffset()));
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      final Ref<PsiElement> result = Ref.create();
      final XmlAttributeValue element = getElement();
      FileBasedIndex.getInstance().processValues(AngularJSIndex.INDEX_ID, AngularJSIndexingHandler.CONTROLLER_KEY, null,
                                                 new FileBasedIndex.ValueProcessor<TObjectIntHashMap<String>>() {
                                                   @Override
                                                   public boolean process(VirtualFile file, TObjectIntHashMap<String> descriptorNames) {
                                                     for (Object o : descriptorNames.keys()) {
                                                       if (element.getValue().equals(o)) {
                                                         PsiFile psiFile = element.getManager().findFile(file);
                                                         if (psiFile != null) {
                                                           result.set(psiFile.findElementAt(descriptorNames.get((String)o)));
                                                           break;
                                                         }
                                                       }
                                                     }
                                                     return result.get() == null;
                                                   }
                                                 }, GlobalSearchScope.allScope(element.getProject())
      );

      return result.get();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      ControllerReference reference = (ControllerReference)o;

      if (!getRangeInElement().equals(reference.getRangeInElement())) return false;
      if (!getElement().equals(reference.getElement())) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = getRangeInElement().hashCode();
      result = 31 * result + getElement().hashCode();
      return result;
    }
  }
}
