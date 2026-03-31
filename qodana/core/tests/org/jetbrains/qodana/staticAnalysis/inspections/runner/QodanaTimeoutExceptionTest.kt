package org.jetbrains.qodana.staticAnalysis.inspections.runner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class QodanaTimeoutExceptionTest {
  @Test
  fun `is subclass of QodanaException and RuntimeException`() {
    val ex = QodanaTimeoutException("timeout")
    assertThat(ex).isInstanceOf(QodanaException::class.java)
    assertThat(ex).isInstanceOf(RuntimeException::class.java)
  }

  @Test
  fun `message constructor preserves message`() {
    val ex = QodanaTimeoutException("test timeout reached")
    assertThat(ex.message).isEqualTo("test timeout reached")
  }

  @Test
  fun `message and cause constructor preserves both`() {
    val cause = IllegalStateException("root cause")
    val ex = QodanaTimeoutException("test timeout reached", cause)
    assertThat(ex.message).isEqualTo("test timeout reached")
    assertThat(ex.cause).isSameAs(cause)
  }

  @Test
  fun `can be caught as QodanaException`() {
    val caught = runCatching { throw QodanaTimeoutException("timeout") }
    assertThat(caught.exceptionOrNull()).isInstanceOf(QodanaException::class.java)
  }
}
