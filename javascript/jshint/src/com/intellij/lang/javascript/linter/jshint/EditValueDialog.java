package com.intellij.lang.javascript.linter.jshint;

import com.intellij.CommonBundle;
import com.intellij.lang.javascript.linter.jshint.JSHintBundle;
import com.intellij.lang.javascript.linter.option.OptionEnumType;
import com.intellij.lang.javascript.linter.option.OptionEnumVariant;
import com.intellij.lang.javascript.linter.option.OptionType;
import com.intellij.lang.javascript.linter.option.OptionTypes;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.HtmlBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.SwingHelper;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public class EditValueDialog extends DialogWrapper {

  private final JPanel myComponent;
  private final TypePeer<Object> myTypePeer;

  public EditValueDialog(@NotNull Component parent,
                         @NotNull @NlsContexts.Label String optionTitle,
                         @NotNull JSHintOption option,
                         @Nullable Object optionValue) {
    super(parent, false);
    //noinspection unchecked
    myTypePeer = (TypePeer<Object>)createTypePeer(option);
    myTypePeer.setValue(optionValue);
    myComponent = createComponent(optionTitle, myTypePeer);
    myComponent.setBorder(BorderFactory.createEmptyBorder(2, 1, 1, 1));

    setTitle(JSHintBundle.message("jshint.option.edit.dialog.title", option.getKey()));
    setOKButtonText(CommonBundle.message("button.set"));

    init();
  }

  private static @NotNull TypePeer<?> createTypePeer(@NotNull JSHintOption option) {
    if (OptionTypes.isIntegerOption(option)) {
      return new IntegerTypePeer();
    }
    if (OptionTypes.isStringOption(option)) {
      return new StringTypePeer();
    }
    if (OptionTypes.isEnumOption(option)) {
      OptionEnumType enumType = OptionTypes.getOptionEnumType(option);
      return new EnumTypePeer(enumType);
    }
    throw new RuntimeException("Unexpected option type: " + option.getType());
  }

  private static @NotNull <T> JPanel createComponent(@NotNull @NlsContexts.Label String optionTitle, @NotNull TypePeer<T> typePeer) {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    final Object labelLocation = typePeer.isHorizontallyStacked() ? BorderLayout.WEST : BorderLayout.NORTH;
    panel.add(new JLabel(optionTitle + ":"), labelLocation);
    panel.add(typePeer.getComponent(), BorderLayout.CENTER);
    if (typePeer instanceof IntegerTypePeer) {
      JLabel hintLabel = new JLabel(JSHintBundle.message("jshint.clear.field.to.disable"), SwingConstants.RIGHT);
      hintLabel.setFont(UIUtil.getTitledBorderFont());
      panel.add(SwingHelper.newVerticalPanel(
        Component.RIGHT_ALIGNMENT,
        Box.createVerticalStrut(5),
        hintLabel
      ), BorderLayout.SOUTH);
    }
    if (typePeer.isHorizontallyStacked()) {
      return SwingHelper.wrapWithHorizontalStretch(panel);
    }
    return panel;
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return myTypePeer.getComponent();
  }

  @Override
  protected @Nullable ValidationInfo doValidate() {
    return myTypePeer.doValidate();
  }

  @Override
  protected @Nullable JComponent createCenterPanel() {
    return myComponent;
  }

  public @Nullable Object getValue() {
    return myTypePeer.getValue();
  }

  private interface TypePeer<T> {
    boolean isHorizontallyStacked();

    @Nullable
    T getValue();

    void setValue(@Nullable T value);

    @Nullable
    ValidationInfo doValidate();

    @NotNull
    JComponent getComponent();
  }

  private static final class IntegerTypePeer implements TypePeer<Integer> {

    private final JTextField myTextField;

    private IntegerTypePeer() {
      myTextField = new JTextField();
      myTextField.setColumns(6);
    }

    @Override
    public boolean isHorizontallyStacked() {
      return true;
    }

    @Override
    public @Nullable Integer getValue() {
      return toInteger(getText());
    }

    @Override
    public void setValue(@Nullable Integer value) {
      String text = value == null ? "" : value.toString();
      myTextField.setText(text);
    }

    @Override
    public @Nullable ValidationInfo doValidate() {
      String text = getText();
      if (!text.isEmpty()) {
        OptionType<Integer> intType = OptionTypes.INTEGER;
        Integer value = intType.fromString(text);
        if (value == null || !intType.isValidValue(value)) {
          return new ValidationInfo(JSHintBundle.message("jshint.illegal.integer"), myTextField);
        }
      }
      return null;
    }

    @Override
    public @NotNull JComponent getComponent() {
      return myTextField;
    }

    private static @Nullable Integer toInteger(@NotNull String text) {
      try {
        return Integer.parseInt(text);
      } catch (Exception e) {
        return null;
      }
    }

    private @NotNull String getText() {
      return StringUtil.notNullize(myTextField.getText());
    }
  }

  private static final class StringTypePeer implements TypePeer<String> {

    private final JBScrollPane myScrollPane;
    private final JTextArea myTextArea;

    private StringTypePeer() {
      myTextArea = new JTextArea();
      myTextArea.setLineWrap(true);
      myTextArea.setWrapStyleWord(true);
      myScrollPane = new JBScrollPane(myTextArea);
      myScrollPane.setPreferredSize(JBUI.size(400, 200));
    }

    @Override
    public boolean isHorizontallyStacked() {
      return false;
    }

    @Override
    public String getValue() {
      return getText();
    }

    @Override
    public void setValue(@Nullable String value) {
      myTextArea.setText(StringUtil.notNullize(value));
    }

    @Override
    public @Nullable ValidationInfo doValidate() {
      return null;
    }

    @Override
    public @NotNull JComponent getComponent() {
      return myScrollPane;
    }

    private @NotNull String getText() {
      return StringUtil.notNullize(myTextArea.getText());
    }
  }

  private static final class EnumTypePeer implements TypePeer<Object> {

    private final OptionEnumType myEnumType;
    private final ButtonGroup myButtonGroup;
    private final JPanel myPanel;
    private final Map<ButtonModel, OptionEnumVariant> myVariantByModelMap = new IdentityHashMap<>();
    private final Map<OptionEnumVariant, ButtonModel> myModelByVariantMap = new IdentityHashMap<>();

    private EnumTypePeer(@NotNull OptionEnumType enumType) {
      myEnumType = enumType;
      myButtonGroup = new ButtonGroup();
      List<Component> components = new ArrayList<>();
      boolean first = true;
      for (OptionEnumVariant variant : enumType.getVariants()) {
        String html = new HtmlBuilder().append(variant.getValueAsJsonStr()).wrapWith("strong").wrapWith("body").wrapWith("html").toString();
        JRadioButton radioButton = new JRadioButton(html);
        myButtonGroup.add(radioButton);
        myModelByVariantMap.put(variant, radioButton.getModel());
        myVariantByModelMap.put(radioButton.getModel(), variant);

        JPanel radioButtonPanel = SwingHelper.wrapWithoutStretch(radioButton);
        if (!first) {
          radioButtonPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        }
        first = false;
        components.add(radioButtonPanel);

        JPanel descriptionPanel = createDescriptionPanel(radioButton, variant);

        components.add(descriptionPanel);
      }
      myPanel = SwingHelper.wrapWithHorizontalStretch(SwingHelper.newLeftAlignedVerticalPanel(components));
    }

    private static JPanel createDescriptionPanel(@NotNull JRadioButton button,
                                                 @NotNull OptionEnumVariant variant) {
      int leftMargin = button.getMargin().left + getRadioIconWidth() + button.getIconTextGap();
      JPanel result = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
      JLabel label = new JLabel(variant.getValueDescription());
      label.setBorder(BorderFactory.createEmptyBorder(0, leftMargin, 0, 0));
      result.add(label);
      return result;
    }

    private static int getRadioIconWidth() {
      Icon icon = UIManager.getIcon("RadioButton.icon");
      if (icon != null) {
        return icon.getIconWidth();
      }
      return 12;
    }

    @Override
    public boolean isHorizontallyStacked() {
      return false;
    }

    @Override
    public @Nullable Object getValue() {
      ButtonModel model = myButtonGroup.getSelection();
      if (model == null) {
        return null;
      }
      OptionEnumVariant variant = myVariantByModelMap.get(model);
      if (variant == null) {
        throw new RuntimeException("No variant found!");
      }
      return variant.getValue();
    }

    @Override
    public void setValue(@Nullable Object value) {
      if (value == null) {
        throw new RuntimeException("Unexpected null for enum!");
      }
      OptionEnumVariant variant = myEnumType.getVariantByValue(value);
      if (variant == null) {
        throw new RuntimeException("Invalid enum value!");
      }
      ButtonModel model = myModelByVariantMap.get(variant);
      if (model == null) {
        throw new RuntimeException("Unexpected null");
      }
      model.setSelected(true);
    }

    @Override
    public @Nullable ValidationInfo doValidate() {
      return null;
    }

    @Override
    public @NotNull JComponent getComponent() {
      return myPanel;
    }
  }

}
