package com.intellij.lang.javascript.linter.tslint;

import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.lang.javascript.linter.JSLinterError;
import com.intellij.lang.javascript.linter.tslint.execution.TsLintOutputParser;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Irina.Chernushina on 6/4/2015.
 */
public class TsLintOutputParsingTest {
  @Test
  public void testParseExample() throws Exception {
    final String text = "[{\"name\":\"C:/Users/Irina.Chernushina/AppData/Local/Temp/intellij-js-closure-linter/first0.ts\",\"failure\":\"if statements must be braced\",\"startPosition\":{\"position\":105,\"line\":4,\"character\":12},\"endPosition\":{\"position\":175,\"line\":4,\"character\":82},\"ruleName\":\"curly\"},{\"name\":\"C:/Users/Irina.Chernushina/AppData/Local/Temp/intellij-js-closure-linter/first0.ts\",\"failure\":\"use of debugger statements is disallowed\",\"startPosition\":{\"position\":83,\"line\":3,\"character\":8},\"endPosition\":{\"position\":91,\"line\":3,\"character\":16},\"ruleName\":\"no-debugger\"},{\"name\":\"C:/Users/Irina.Chernushina/AppData/Local/Temp/intellij-js-closure-linter/first0.ts\",\"failure\":\"' should be \\\"\",\"startPosition\":{\"position\":260,\"line\":8,\"character\":26},\"endPosition\":{\"position\":275,\"line\":8,\"character\":41},\"ruleName\":\"quotemark\"}]";
    final TsLintOutputParser parser = new TsLintOutputParser("test", false);
    final DefaultDebugProcessHandler processHandler = new DefaultDebugProcessHandler();
    parser.onTextAvailable(new ProcessEvent(processHandler, text), ProcessOutputTypes.STDOUT);
    parser.process();
    Assert.assertNull(parser.getGlobalError());
    Assert.assertEquals(3, parser.getErrors().size());
    final List<JSLinterError> errors = parser.getErrors();
    final Set<String> expected = new HashSet<>();
    expected.add("curly");
    expected.add("no-debugger");
    expected.add("quotemark");
    for (JSLinterError error : errors) {
      expected.remove(error.getCode());
    }
    Assert.assertTrue(expected.isEmpty());
  }
}
