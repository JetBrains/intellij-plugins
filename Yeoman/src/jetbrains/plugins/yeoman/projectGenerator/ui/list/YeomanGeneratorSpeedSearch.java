package jetbrains.plugins.yeoman.projectGenerator.ui.list;

import com.intellij.ui.SpeedSearchBase;
import com.intellij.ui.TableUtil;
import jetbrains.plugins.yeoman.generators.YeomanGeneratorInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class YeomanGeneratorSpeedSearch extends SpeedSearchBase<YeomanGeneratorTable> {
  private YeomanGeneratorSpeedSearch(YeomanGeneratorTable component) {
    super(component, null);
  }

  public static @NotNull YeomanGeneratorSpeedSearch installOn(YeomanGeneratorTable component) {
    YeomanGeneratorSpeedSearch search = new YeomanGeneratorSpeedSearch(component);
    search.setupListeners();
    return search;
  }

  @Override
  protected int getSelectedIndex() {
    return getComponent().getSelectedRow();
  }

  @Override
  protected int getElementCount() {
    return ((YeomanGeneratorTableModel)getComponent().getModel()).getView().size();
  }

  @Override
  protected Object getElementAt(int viewIndex) {
    return ((YeomanGeneratorTableModel)getComponent().getModel()).getView().get(getComponent().convertRowIndexToModel(viewIndex));
  }

  @Nullable
  @Override
  protected String getElementText(Object element) {
    if (element instanceof YeomanGeneratorInfo) {
      return ((YeomanGeneratorInfo)element).getYoName();
    }

    return null;
  }

  @Override
  protected void selectElement(Object element, String selectedText) {
    for (int i = 0; i < myComponent.getRowCount(); i++) {
      if (myComponent.getObjectAt(i).getYoName().equals(((YeomanGeneratorInfo)element).getYoName())) {
        myComponent.setRowSelectionInterval(i, i);
        TableUtil.scrollSelectionToVisible(myComponent);
        break;
      }
    }
  }
}
