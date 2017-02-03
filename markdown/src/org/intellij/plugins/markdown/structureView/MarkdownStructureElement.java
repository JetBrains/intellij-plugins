package org.intellij.plugins.markdown.structureView;

import com.intellij.ide.IdeBundle;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.ide.util.treeView.smartTree.SortableTreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.LocationPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import org.intellij.plugins.markdown.lang.MarkdownElementTypes;
import org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets;
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.intellij.plugins.markdown.lang.MarkdownTokenTypeSets.*;

public class MarkdownStructureElement extends PsiTreeElementBase<PsiElement> implements  SortableTreeElement, LocationPresentation {

  private static final ItemPresentation DUMMY_PRESENTATION = new MarkdownBasePresentation() {

    @Nullable
    @Override
    public String getPresentableText() {
      return null;
    }

    @Nullable
    @Override
    public String getLocationString() {
      return null;
    }
  };

  static final TokenSet PRESENTABLE_TYPES = TokenSet.orSet(MarkdownTokenTypeSets.HEADERS);

  MarkdownStructureElement(@NotNull PsiElement element) {
    super(element);
  }


  @Override
  public boolean canNavigate() {
    return getElement() instanceof NavigationItem && ((NavigationItem) getElement()).canNavigate();
  }

  @Override
  public boolean canNavigateToSource() {
    return getElement() instanceof NavigationItem && ((NavigationItem) getElement()).canNavigateToSource();
  }


  @Override
  public void navigate(boolean requestFocus) {
    if (getElement() instanceof NavigationItem) {
      ((NavigationItem) getElement()).navigate(requestFocus);
    }
  }


  @NotNull
  @Override
  public String getAlphaSortKey() {
    return StringUtil.notNullize(getElement() instanceof NavigationItem ?
            ((NavigationItem) getElement()).getName() : null);
  }

  @Override
  public boolean isSearchInLocationString() {
    return true;
  }

  @Nullable
  @Override
  public String getPresentableText() {
    final PsiElement tag = getElement();
    if (tag == null) {
      return IdeBundle.message("node.structureview.invalid");
    }
    return getPresentation().getPresentableText();
  }


  @Override
  public String getLocationString() {
    return getPresentation().getLocationString();
  }

  @NotNull
  @Override
  public ItemPresentation getPresentation() {
    if (getElement() instanceof PsiFileImpl) {
      ItemPresentation filePresent = ((PsiFileImpl) getElement()).getPresentation();
      return filePresent!=null ? filePresent : DUMMY_PRESENTATION;
    }

    if (getElement() instanceof NavigationItem) {
      final ItemPresentation itemPresent = ((NavigationItem) getElement()).getPresentation();
      if (itemPresent != null) {
        return itemPresent;
      }
    }

    return DUMMY_PRESENTATION;
  }

  @NotNull
  @Override
  public Collection<StructureViewTreeElement> getChildrenBase() {
    final List<StructureViewTreeElement> childrenElements = new ArrayList<StructureViewTreeElement>();

    final PsiElement myElement = getElement();
    if(myElement==null) return childrenElements;

    PsiElement nextSibling = myElement instanceof MarkdownFile ?
            myElement.getFirstChild().getFirstChild() :
            myElement.getNextSibling();

    PsiElement maxContentLevel = null;

    while (nextSibling != null) {
      if (isSameLevelOrHigher(nextSibling, myElement)) {
        break;
      }

      if (maxContentLevel == null || isSameLevelOrHigher(nextSibling, maxContentLevel)) {
        maxContentLevel = nextSibling;

        final IElementType type = nextSibling.getNode().getElementType();
        if (PRESENTABLE_TYPES.contains(type)) {
          childrenElements.add(new MarkdownStructureElement(nextSibling));
        }
      }

      nextSibling = nextSibling.getNextSibling();
    }

    return childrenElements;
  }


  private boolean isSameLevelOrHigher(@NotNull PsiElement psiA, @NotNull PsiElement psiB) {
    IElementType typeA = psiA.getNode().getElementType();
    IElementType typeB = psiB.getNode().getElementType();

    return headerLevel(typeA) <= headerLevel(typeB);
  }


  private int headerLevel(@NotNull IElementType curLevelType) {
    for (int i = 0; i < HEADER_ORDER.size(); i++) {
      if (HEADER_ORDER.get(i).contains(curLevelType)) {
        return i;
      }
    }

    // not a header so return lowest level
    return Integer.MAX_VALUE;
  }


  private static final List<TokenSet> HEADER_ORDER = Arrays.asList(
          TokenSet.create(MarkdownElementTypes.MARKDOWN_FILE_ELEMENT_TYPE),
          HEADER_LEVEL_1_SET,
          HEADER_LEVEL_2_SET,
          HEADER_LEVEL_3_SET,
          HEADER_LEVEL_4_SET,
          HEADER_LEVEL_5_SET,
          HEADER_LEVEL_6_SET);

  @Override
  public String getLocationPrefix() {
    return " ";
  }

  @Override
  public String getLocationSuffix() {
    return "";
  }
}
