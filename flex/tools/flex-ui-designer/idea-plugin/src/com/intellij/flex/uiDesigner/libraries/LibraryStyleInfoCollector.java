package com.intellij.flex.uiDesigner.libraries;

import com.intellij.flex.uiDesigner.CssWriter;
import com.intellij.flex.uiDesigner.ProblemsHolder;
import com.intellij.flex.uiDesigner.io.ByteArrayOutputStreamEx;
import com.intellij.flex.uiDesigner.io.PrimitiveAmfOutputStream;
import com.intellij.flex.uiDesigner.io.StringRegistry;
import com.intellij.javascript.flex.css.FlexStyleIndex;
import com.intellij.javascript.flex.css.FlexStyleIndexInfo;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Consumer;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import gnu.trove.THashSet;

import java.util.Set;

public class LibraryStyleInfoCollector implements Consumer<Library> {
  private final Project project;
  private final Module module;

  private final PrimitiveAmfOutputStream bytes = new PrimitiveAmfOutputStream(new ByteArrayOutputStreamEx(128));
  private final CssWriter cssWriter;
  private final StringRegistry.StringWriter stringWriter;
  private final ProblemsHolder problemsHolder;

  public LibraryStyleInfoCollector(Project project, Module module, StringRegistry.StringWriter stringWriter, ProblemsHolder problemsHolder) {
    this.project = project;
    this.module = module;
    this.stringWriter = stringWriter;
    this.problemsHolder = problemsHolder;
    cssWriter = new CssWriter(this.stringWriter);
  }

  private byte[] collectInherited(final VirtualFile jarFile) {
    bytes.getByteArrayOut().allocate(2);

    final VirtualFile libraryFile = jarFile.findChild("library.swf");
    assert libraryFile != null;

    final FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
    final GlobalSearchScope searchScope = GlobalSearchScope.fileScope(project, libraryFile);
    final THashSet<String> uniqueGuard = new THashSet<String>();
    fileBasedIndex.processAllKeys(FlexStyleIndex.INDEX_ID, new Processor<String>() {
        @Override
        public boolean process(String dataKey) {
          fileBasedIndex
            .processValues(FlexStyleIndex.INDEX_ID, dataKey, libraryFile, new FileBasedIndex.ValueProcessor<Set<FlexStyleIndexInfo>>() {
                @Override
                public boolean process(VirtualFile file, Set<FlexStyleIndexInfo> value) {
                  final FlexStyleIndexInfo firstInfo = value.iterator().next();
                  if (firstInfo.getInherit().charAt(0) == 'y' && uniqueGuard.add(firstInfo.getAttributeName())) {
                    bytes.writeUInt29(stringWriter.getReference(firstInfo.getAttributeName()) - 1);
                  }

                  // If the property is defined in the library — we it consider that unique for all library — we make an assumption that 
                  // may not be in a class stylePName be inherited, and another class of the same library not inherited
                  return false;
                }
              }, searchScope);

          return true;
        }
      }, project);

    if (uniqueGuard.size() == 0) {
      return null;
    }
    else {
      bytes.putShort(uniqueGuard.size(), 0);
      return bytes.getByteArrayOut().toByteArray();
    }
  }

  @Override
  public void consume(Library library) {
    try {
      library.inheritingStyles = collectInherited(library.getFile());
    }
    finally {
      bytes.reset();
    }

    VirtualFile defaultsCssVirtualFile = library.getDefaultsCssFile();
    if (defaultsCssVirtualFile != null) {
      library.defaultsStyle = cssWriter.write(defaultsCssVirtualFile, module, problemsHolder);
      library.requiredAssetsInfo = cssWriter.getRequiredAssetsInfo();
    }
  }
}