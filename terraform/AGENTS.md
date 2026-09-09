# Terraform plugin tests

Use these conventions when writing or changing tests in this plugin.

## User interactions

- Exercise user interactions, such as opening a file, invoking an action, or formatting code.
- Invoke registered actions through `myFixture.testAction`. Look the action up with `com.intellij.openapi.actionSystem.ex.ActionUtil` and fail with a clear message when it is absent:

  ```kotlin
  val action = ActionUtil.getAction(actionId) ?: throw AssertionError("$actionId is not registered")
  myFixture.testAction(action)
  ```

- Get services through the project or application container. Let extension points create their implementations.
- Avoid constructing services, executors, or internal requests to bypass the interaction under test.
- Reuse `TfCommandLineServiceMock.instance` to simulate external commands and inspect execution. Do not create custom request mocks.

## Await action jobs

Run asynchronous tests inside `timeoutRunBlocking`.
Invoke editor actions in a narrow `withContext(Dispatchers.EDT)` block with the required lock context.
Await background work outside that block.

`yield()`, sleeps, and arbitrary delays do not prove completion.
Observing a command only proves that execution started.
If another operation needs a wait helper, expose a narrow suspendable method on its owner.
Await the operation jobs and their children. Do not join the service's lifetime job.

## Files in light tests

The default light fixture uses an in-memory filesystem that cannot supply a NIO path.
`TfFmtFileAction` needs a real path for its EEL conversion.
Keep the light project and select a disk-backed temporary fixture:

```kotlin
import com.intellij.testFramework.fixtures.TempDirTestFixture
import com.intellij.testFramework.fixtures.impl.TempDirTestFixtureImpl

override fun createTempDirTestFixture(): TempDirTestFixture = TempDirTestFixtureImpl()
```

The fixture provides VFS access and deletes its files during teardown.
See [TfBaseRunConfigurationTest](test/org/intellij/terraform/runtime/TfBaseRunConfigurationTest.kt) for the same fixture choice.
Do not change `TfFmtFileAction` or its path conversion solely to accommodate test files.

## Setup and cleanup

- Use `setUp()` and `tearDown()` for resources that the fixture does not own.
- Create each temporary executable in setup and delete it during teardown. Do not create unmanaged files in companion objects or static initializers.
- Handle partial setup and assertion failures. Run each cleanup step with `com.intellij.testFramework.common.runAll`, and put `super.tearDown()` last. `runAll` keeps the later steps and the cleanup exceptions:

  ```kotlin
  override fun tearDown() {
    runAll(
      { releaseMyResource() },
      { super.tearDown() },
    )
  }
  ```

- Await relevant jobs before restoring project trust or settings, or deleting files that those jobs can use.
- Clear `TfCommandLineServiceMock.instance` in setup and call `throwErrorsIfAny()` during teardown.

## Verification

Run the affected tests from the repository root. Name the test module and use a fully qualified class name:

```bash
./tests.cmd --module intellij.terraform.tests --test org.intellij.terraform.TfSafeModeTest
```

Replace the class name with the affected test. Use `lint_files` for changed Kotlin or Java files when the IDE tool is available.
