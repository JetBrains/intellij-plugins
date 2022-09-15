package com.intellij.lang.javascript.flex.artifacts;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.packaging.elements.PackagingElement;
import com.intellij.packaging.elements.PackagingElementFactory;
import com.intellij.packaging.impl.elements.DirectoryCopyPackagingElement;
import com.intellij.packaging.impl.elements.FileCopyPackagingElement;
import com.intellij.packaging.ui.ArtifactEditorContext;
import com.intellij.packaging.ui.PackagingSourceItem;
import com.intellij.packaging.ui.SourceItemPresentation;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FlashBCOutputSourceItem extends PackagingSourceItem {

  enum Type {OutputFile, OutputFileAndHtmlWrapper, OutputFolderContents}

  private final FlexBuildConfiguration myBc;
  private final Type myType;
  private final int myOrderNumber;

  public FlashBCOutputSourceItem(final FlexBuildConfiguration bc, final Type type, final int number) {
    myBc = bc;
    myType = type;
    myOrderNumber = number;
  }

  public boolean equals(final Object o) {
    return o instanceof FlashBCOutputSourceItem &&
           myBc.equals(((FlashBCOutputSourceItem)o).myBc) &&
           myType.equals(((FlashBCOutputSourceItem)o).myType);
  }

  public int hashCode() {
    return myBc.hashCode() + myType.hashCode() * 239;
  }

  @Override
  @NotNull
  public SourceItemPresentation createPresentation(final @NotNull ArtifactEditorContext context) {
    return new SourceItemPresentation() {
      @Override
      public String getPresentableName() {
        return switch (myType) {
          case OutputFile ->
            FlexBundle.message("bc.output.file.source.item", myBc.getName(), PathUtil.getFileName(myBc.getActualOutputFilePath()));
          case OutputFileAndHtmlWrapper ->
            FlexBundle.message("bc.output.file.and.wrapper.source.item", myBc.getName());
          case OutputFolderContents ->
            FlexBundle.message("bc.output.folder.source.item", myBc.getName());
        };
      }

      @Override
      public void render(final @NotNull PresentationData presentationData,
                         final SimpleTextAttributes mainAttributes,
                         final SimpleTextAttributes commentAttributes) {
        presentationData.setIcon(myBc.getIcon());
        presentationData.addText(getPresentableName(), mainAttributes);
      }

      @Override
      public int getWeight() {
        return -myOrderNumber;
      }
    };
  }

  @Override
  @NotNull
  public List<? extends PackagingElement<?>> createElements(@NotNull final ArtifactEditorContext context) {
    final String outputFilePath = myBc.getActualOutputFilePath();
    final String outputFolderPath = PathUtil.getParentPath(outputFilePath);

    return switch (myType) {
      case OutputFile -> Collections.singletonList(new FileCopyPackagingElement(outputFilePath));
      case OutputFileAndHtmlWrapper -> {
        final List<PackagingElement<?>> result = new ArrayList<>();

        result.add(new FileCopyPackagingElement(outputFilePath));
        result.add(new FileCopyPackagingElement(outputFolderPath + "/" + BCUtils.getWrapperFileName(myBc)));

        final VirtualFile wrapperDir = LocalFileSystem.getInstance().findFileByPath(myBc.getWrapperTemplatePath());
        if (wrapperDir != null && wrapperDir.isDirectory()) {
          for (VirtualFile file : wrapperDir.getChildren()) {
            if (!FlexCommonUtils.HTML_WRAPPER_TEMPLATE_FILE_NAME.equals(file.getName())) {
              if (file.isDirectory()) {
                final DirectoryCopyPackagingElement packagingElement =
                  new DirectoryCopyPackagingElement(outputFolderPath + "/" + file.getName());
                result.add(PackagingElementFactory.getInstance().createParentDirectories(file.getName(), packagingElement));
              }
              else {
                result.add(new FileCopyPackagingElement(outputFolderPath + "/" + file.getName()));
              }
            }
          }
        }
        yield result;
      }
      case OutputFolderContents -> Collections.singletonList(new DirectoryCopyPackagingElement(outputFolderPath));
    };
  }
}
