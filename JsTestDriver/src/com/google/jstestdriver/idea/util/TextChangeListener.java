package com.google.jstestdriver.idea.util;

import org.jetbrains.annotations.NotNull;

public interface TextChangeListener {

  void textChanged(String oldText, @NotNull String newText);

}
