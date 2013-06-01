package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.file.impl.FileManager;
import com.intellij.psi.impl.source.tree.FileElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.testFramework.LightVirtualFile;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.DartParser;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.DartExpressionCodeFragment;
import com.jetbrains.lang.dart.psi.DartFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.lang.parser.GeneratedParserUtilBase.*;

/**
 * @author: Fedor.Korotkov
 */
public class DartExpressionCodeFragmentImpl extends DartFile implements DartExpressionCodeFragment {
  private PsiElement myContext;
  private boolean myPhysical;
  private FileViewProvider myViewProvider;
  private GlobalSearchScope myScope = null;

  public DartExpressionCodeFragmentImpl(Project project,
                                        @NonNls String name,
                                        CharSequence text,
                                        boolean isPhysical) {
    super(new SingleRootFileViewProvider(PsiManager.getInstance(project),
                                         new LightVirtualFile(name, FileTypeManager.getInstance().getFileTypeByFileName(name), text),
                                         isPhysical) {
      @Override
      public boolean supportsIncrementalReparse(@NotNull Language rootLanguage) {
        return false;
      }
    });

    myPhysical = isPhysical;
    ((SingleRootFileViewProvider)getViewProvider()).forceCachedPsi(this);
    final MyDartFileElementType type = new MyDartFileElementType();
    init(type, type);
  }


  public PsiElement getContext() {
    return myContext;
  }

  @NotNull
  public FileViewProvider getViewProvider() {
    if (myViewProvider != null) return myViewProvider;
    return super.getViewProvider();
  }

  public boolean isValid() {
    if (!super.isValid()) return false;
    if (myContext != null && !myContext.isValid()) return false;
    return true;
  }

  protected DartExpressionCodeFragmentImpl clone() {
    final DartExpressionCodeFragmentImpl clone = (DartExpressionCodeFragmentImpl)cloneImpl((FileElement)calcTreeElement().clone());
    clone.myPhysical = myPhysical;
    clone.myOriginalFile = this;
    final FileManager fileManager = ((PsiManagerEx)getManager()).getFileManager();
    final SingleRootFileViewProvider cloneViewProvider =
      (SingleRootFileViewProvider)fileManager.createFileViewProvider(new LightVirtualFile(getName(), getLanguage(), getText()), myPhysical);
    clone.myViewProvider = cloneViewProvider;
    cloneViewProvider.forceCachedPsi(clone);
    clone.init(getContentElementType(), getContentElementType());
    return clone;
  }

  public boolean isPhysical() {
    return myPhysical;
  }

  public void setContext(PsiElement context) {
    myContext = context;
  }

  @Override
  public void forceResolveScope(GlobalSearchScope scope) {
    myScope = scope;
  }

  @Override
  public GlobalSearchScope getForcedResolveScope() {
    return myScope;
  }

  private class MyDartFileElementType extends IFileElementType {
    public MyDartFileElementType() {
      super(DartLanguage.INSTANCE);
    }

    @Nullable
    @Override
    public ASTNode parseContents(final ASTNode chameleon) {
      final PsiElement psi = new DartPsiCompositeElementImpl(chameleon);
      return doParseContents(chameleon, psi);
    }

    @Override
    protected ASTNode doParseContents(@NotNull ASTNode chameleon, @NotNull PsiElement psi) {
      final PsiBuilderFactory factory = PsiBuilderFactory.getInstance();
      final PsiBuilder psiBuilder = factory.createBuilder(getProject(), chameleon);
      final PsiBuilder builder = adapt_builder_(DartTokenTypes.STATEMENTS, psiBuilder, new DartParser());

      final PsiBuilder.Marker marker = builder.mark();
      enterErrorRecordingSection(builder, 0, _SECTION_GENERAL_, "<code fragment>");
      DartParser.expression(builder, 1);
      while (builder.getTokenType() != null) {
        builder.advanceLexer();
      }
      marker.done(DartTokenTypes.STATEMENTS);
      return builder.getTreeBuilt();
    }
  }
}
