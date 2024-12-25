package com.jetbrains.plugins.meteor.spacebars.lang;

import com.dmarcotte.handlebars.parsing.HbParseDefinition;
import com.dmarcotte.handlebars.parsing.HbParser;
import com.dmarcotte.handlebars.parsing.HbParsing;
import com.dmarcotte.handlebars.psi.HbPsiFile;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.IStubFileElementType;
import com.jetbrains.plugins.meteor.spacebars.lang.parsing.SpacebarsParsing;
import org.jetbrains.annotations.NotNull;

public final class SpacebarsParseDefinition extends HbParseDefinition {
  private static final IFileElementType FILE = new IStubFileElementType<>("spacebars", SpacebarsLanguageDialect.INSTANCE);

  @Override
  public @NotNull PsiParser createParser(Project project) {
    return new HbParser() {
      @Override
      protected HbParsing getParsing(PsiBuilder builder) {
        return new SpacebarsParsing(builder);
      }
    };
  }

  @Override
  public @NotNull IFileElementType getFileNodeType() {
    return FILE;
  }

  @Override
  public @NotNull PsiFile createFile(@NotNull FileViewProvider viewProvider) {
    return new HbPsiFile(viewProvider, SpacebarsLanguageDialect.INSTANCE) {
      @Override
      public @NotNull FileType getFileType() {
        return SpacebarsFileType.SPACEBARS_INSTANCE;
      }


      @Override
      public String toString() {
        return "SpacebarsFile:" + getName();
      }
    };
  }
}
