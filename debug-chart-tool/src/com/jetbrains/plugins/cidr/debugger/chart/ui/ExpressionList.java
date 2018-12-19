package com.jetbrains.plugins.cidr.debugger.chart.ui;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.AddEditRemovePanel;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.jetbrains.plugins.cidr.debugger.chart.ChartToolPersistence;
import com.jetbrains.plugins.cidr.debugger.chart.state.ChartExpression;
import com.jetbrains.plugins.cidr.debugger.chart.state.ExpressionState;
import org.jdesktop.swingx.JXRadioGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ExpressionList extends AddEditRemovePanel<ChartExpression> {

  private final ChartToolPersistence persistence;
  private final Runnable updateRunner;

  public ExpressionList(ChartToolPersistence persistence, Runnable updateRunner) {
    super(createModel(), persistence.getExpressions());
    this.persistence = persistence;
    this.updateRunner = updateRunner;
    getTable().getColumnModel().getColumn(1).setPreferredWidth(80);
  }

  private static TableModel<ChartExpression> createModel() {
    return new TableModel<ChartExpression>() {
      @Override
      public int getColumnCount() {
        return 2;
      }

      @Override
      public String getColumnName(int columnIndex) {
        return new String[]{"Name", "State", "Expression"}[columnIndex];
      }

      @Override
      public boolean isEditable(int column) {
        return column == 1;
      }

      @Override
      public Class getColumnClass(int columnIndex) {
        return columnIndex == 1 ? ExpressionState.class : String.class;
      }

      @Override
      public Object getField(ChartExpression chartExpression, int columnIndex) {
        if (columnIndex == 1) {
          return chartExpression.getState();
        }
        String name = chartExpression.getName();
        String expressionTrim = chartExpression.getExpressionTrim();
        return (expressionTrim.equals(name)) ? name : (name + " (" + expressionTrim + ")");
      }
    };
  }

  @Override
  protected ChartExpression addItem() {
    persistence.registerChange();
    return doEdit(new ChartExpression());
  }

  @Override
  protected boolean removeItem(ChartExpression chartExpression) {
    persistence.registerChange();
    return true;
  }

  @Nullable
  @Override
  protected ChartExpression editItem(ChartExpression chartExpression) {
    return doEdit(chartExpression);
  }

  @Nullable
  private ChartExpression doEdit(ChartExpression chartExpression) {
    JBTextField expressionField = new JBTextField();
    JBTextField nameField = new JBTextField();
    JBTextField baseXField = new JBTextField();
    JBTextField baseYField = new JBTextField();

    JBTextField scaleXField = new JBTextField();
    JBTextField scaleYField = new JBTextField();

    JXRadioGroup<ExpressionState> stateGroup = new JXRadioGroup<>(ExpressionState.values());
    Stream.of(ExpressionState.values()).forEach(v -> stateGroup.getChildButton(v).setToolTipText(v.myHint));
    stateGroup.setSelectedValue(chartExpression.getState());
    JBPanel<JBPanel> dataPanel = new JBPanel<>(new GridLayout(7, 2));
    dataPanel.add(new JBLabel("Name (optional): "));
    dataPanel.add(nameField);
    dataPanel.add(new JBLabel("Expression: "));
    dataPanel.add(expressionField);
    dataPanel.add(new JBLabel("Action: "));
    dataPanel.add(stateGroup);

    dataPanel.add(new JBLabel("Base X: "));
    dataPanel.add(baseXField);

    dataPanel.add(new JBLabel("Scale X: "));
    dataPanel.add(scaleXField);

    dataPanel.add(new JBLabel("Base Y: "));
    dataPanel.add(baseYField);

    dataPanel.add(new JBLabel("Scale Y: "));
    dataPanel.add(scaleYField);


    DialogBuilder dialogBuilder = new DialogBuilder(this)
      .centerPanel(dataPanel)
      .title("Edit expressions");
    dialogBuilder.addOkAction();
    expressionField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        dialogBuilder.setOkActionEnabled(!expressionField.getText().trim().isEmpty());
      }
    });

    dialogBuilder.addCancelAction();
    dialogBuilder.addAction(new AbstractAction("Reset") {
      @Override
      public void actionPerformed(ActionEvent e) {
        baseXField.setText("" + 0.0);
        baseYField.setText("" + 0.0);
        scaleXField.setText("" + 1.0);
        scaleYField.setText("" + 1.0);
      }
    });

    expressionField.setText(chartExpression.getExpression());
    nameField.setText(chartExpression.getName());
    stateGroup.setSelectedValue(chartExpression.getState());

    baseXField.setText(String.valueOf(chartExpression.getXBase()));
    baseYField.setText(String.valueOf(chartExpression.getYBase()));
    scaleXField.setText(String.valueOf(chartExpression.getXScale()));
    scaleYField.setText(String.valueOf(chartExpression.getYScale()));

    if (dialogBuilder.showAndGet()) {
      chartExpression.setExpression(expressionField.getText().trim());
      chartExpression.setName(nameField.getText().trim());
      chartExpression.setState(stateGroup.getSelectedValue());

      setDouble(baseXField, chartExpression::setXBase);
      setDouble(baseYField, chartExpression::setYBase);

      setDouble(scaleXField, chartExpression::setXScale);
      setDouble(scaleYField, chartExpression::setYScale);

      persistence.registerChange();
      updateRunner.run();
      return chartExpression;
    }
    return null;
  }

  private static void setDouble(JTextComponent field, Consumer<Double> target) {
    try {
      target.accept(Double.parseDouble(field.getText()));
    }
    catch (NumberFormatException ignore) {
    }
  }
}
