package org.angularjs.index;

import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlRecursiveElementWalkingVisitor;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.Processor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.xml.util.HtmlUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dennis.Ushakov
 */
public final class AngularTemplateCacheIndex extends ScalarIndexExtension<String> {
  public static final ID<String, Void> TEMPLATE_CACHE_INDEX = ID.create("angularjs.template.cache");
  private final DataIndexer<String, Void, FileContent> myDataIndexer = new MyDataIndexer();

  @Override
  public @NotNull ID<String, Void> getName() {
    return TEMPLATE_CACHE_INDEX;
  }

  @Override
  public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
    return myDataIndexer;
  }

  @Override
  public @NotNull KeyDescriptor<String> getKeyDescriptor() {
    return EnumeratorStringDescriptor.INSTANCE;
  }

  @Override
  public @NotNull FileBasedIndex.InputFilter getInputFilter() {
    return AngularTemplateIndexInputFilter.INSTANCE;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return AngularIndexUtil.BASE_VERSION;
  }

  private static class MyDataIndexer implements DataIndexer<String, Void, FileContent> {
    @Override
    public @NotNull Map<String, Void> map(@NotNull FileContent inputData) {
      final Map<String, Void> result = new HashMap<>();
      PsiFile psiFile = inputData.getPsiFile();
      processTemplates(psiFile, attribute -> {
        result.put(attribute.getValue(), null);
        return true;
      });

      return result;
    }
  }

  public static void processTemplates(final PsiFile psiFile, final Processor<? super XmlAttribute> processor) {
    if (psiFile instanceof XmlFile) {
      psiFile.accept(new XmlRecursiveElementWalkingVisitor() {
        @Override
        public void visitXmlTag(@NotNull XmlTag tag) {
          if (HtmlUtil.isScriptTag(tag) && "text/ng-template".equals(tag.getAttributeValue("type"))) {
            final XmlAttribute id = tag.getAttribute("id");
            if (id != null) {
              processor.process(id);
              return;
            }
          }
          super.visitXmlTag(tag);
        }
      });
    }
  }
}
