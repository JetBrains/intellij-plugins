package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.project.Project;

import java.util.ArrayList;
import java.util.List;

import static com.intellij.lang.javascript.flex.build.FlexBuildConfiguration.ConditionalCompilationDefinition;

public class ConditionalCompilationDefinitionsDialog extends AddRemoveTableRowsDialog<ConditionalCompilationDefinition> {
  private final Project myProject;

  public ConditionalCompilationDefinitionsDialog(final Project project,
                                                 final List<ConditionalCompilationDefinition> conditionalCompilationDefinitionList) {
    super(project, FlexBundle.message("conditional.compilation.definitions.title"), cloneList(conditionalCompilationDefinitionList));
    myProject = project;

    init();
  }

  private static List<ConditionalCompilationDefinition> cloneList(final List<ConditionalCompilationDefinition> conditionalCompilationDefinitionList) {
    final List<ConditionalCompilationDefinition> clonedList = new ArrayList<ConditionalCompilationDefinition>();
    for (ConditionalCompilationDefinition conditionalCompilationDefinition : conditionalCompilationDefinitionList) {
      clonedList.add(conditionalCompilationDefinition.clone());
    }
    return clonedList;
  }

  protected AddObjectDialog<ConditionalCompilationDefinition> createAddObjectDialog() {
    return new AddConditionalCompilationDefinitionDialog(myProject);
  }

  protected TableModelBase getTableModel() {
    return new TableModelBase() {

      public int getColumnCount() {
        return Column.values().length;
      }

      public String getColumnName(int column) {
        return Column.values()[column].getColumnName();
      }

      public Class getColumnClass(int column) {
        return Column.values()[column].getColumnClass();
      }

      protected Object getValue(final ConditionalCompilationDefinition conditionalCompilationDefinition, final int column) {
        return Column.values()[column].getValue(conditionalCompilationDefinition);
      }

      protected void setValue(final ConditionalCompilationDefinition conditionalCompilationDefinition,
                              final int column,
                              final Object aValue) {
        Column.values()[column].setValue(conditionalCompilationDefinition, aValue);
      }
    };
  }

  private enum Column {
    Name("Constant Name", String.class) {
      Object getValue(final ConditionalCompilationDefinition conditionalCompilationDefinition) {
        return conditionalCompilationDefinition.NAME;
      }

      void setValue(final ConditionalCompilationDefinition conditionalCompilationDefinition, final Object value) {
        conditionalCompilationDefinition.NAME = (String)value;
      }
    },

    Value("Value", String.class) {
      Object getValue(final ConditionalCompilationDefinition conditionalCompilationDefinition) {
        return conditionalCompilationDefinition.VALUE;
      }

      void setValue(final ConditionalCompilationDefinition conditionalCompilationDefinition, final Object value) {
        conditionalCompilationDefinition.VALUE = (String)value;
      }
    };

    private String myColumnName;
    private Class myColumnClass;

    private Column(final String columnName, final Class columnClass) {
      myColumnName = columnName;
      myColumnClass = columnClass;
    }

    public String getColumnName() {
      return myColumnName;
    }

    private Class getColumnClass() {
      return myColumnClass;
    }

    abstract Object getValue(final ConditionalCompilationDefinition conditionalCompilationDefinition);

    abstract void setValue(final ConditionalCompilationDefinition conditionalCompilationDefinition, final Object value);
  }
}