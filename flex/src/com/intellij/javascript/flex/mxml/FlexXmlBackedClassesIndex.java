package com.intellij.javascript.flex.mxml;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.ecmal4.JSReferenceList;
import com.intellij.lang.javascript.types.JSFileElementType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.containers.HashMap;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public abstract class FlexXmlBackedClassesIndex extends ScalarIndexExtension<String> {
  private static final int INDEX_VERSION = 4;
  private final EnumeratorStringDescriptor myKeyDescriptor = new EnumeratorStringDescriptor();
  private final FileBasedIndex.InputFilter myInputFilter = new FileBasedIndex.InputFilter() {
    @Override
    public boolean acceptInput(VirtualFile file) {
      return JavaScriptSupportLoader.isMxmlOrFxgFile(file);
    }
  };

  @Override
  @NotNull
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return new DataIndexer<String, Void, FileContent>() {

      @Override
      @NotNull
      public Map<String, Void> map(FileContent inputData) {
        final XmlFile file = (XmlFile)inputData.getPsiFile();

        final Map<String, Void> result = new HashMap<String, Void>();
        for (JSClass clazz : XmlBackedJSClassImpl.getClasses(file)) {
          JSReferenceList supers = getSupers(clazz);
          if (supers != null) {
            final JSReferenceExpression[] expressions = supers.getExpressions();
            if (expressions != null) {
              for (JSReferenceExpression expr : expressions) {
                String s = expr.getReferencedName();
                if (s != null) result.put(s, null);
              }
            }
          }
        }
        return result;
      }
    };
  }

  @Nullable
  protected abstract JSReferenceList getSupers(JSClass clazz);

  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return myKeyDescriptor;
  }

  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return myInputFilter;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return JSFileElementType.VERSION + INDEX_VERSION;
  }

  public static Collection<JSClass> searchClassInheritors(ID<String, Void> indexId, String name, Project project, final GlobalSearchScope scope) {
    final Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(indexId, name, scope);
    final PsiManager psiManager = PsiManager.getInstance(project);
    Collection<JSClass> classes = new ArrayList<JSClass>(files.size());
    for (VirtualFile file : files) {
      PsiFile psifile = psiManager.findFile(file);
      if (!(psifile instanceof XmlFile)) continue;
      XmlFile xmlFile = (XmlFile)psifile;
      if (xmlFile != null) classes.addAll(XmlBackedJSClassImpl.getClasses(xmlFile));
    }

    if (FlexNameAlias.CONTAINER_TYPE_NAME.equals(name)) {
      classes.addAll(searchClassInheritors(indexId, FlexNameAlias.COMPONENT_TYPE_NAME, project, scope));
    }
    return classes;
  }
}
