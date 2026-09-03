package org.jetbrains.qodana.extensions

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class GitRemoteUrlTest(private val first: String, private val second: String, private val expected: Boolean) {
  @Test
  fun comparesRemoteUrls() {
    assertEquals(expected, areSameRemoteUrls(first, second))
  }

  companion object {
    @Parameterized.Parameters(name = "{index}: {0} and {1}")
    @JvmStatic
    fun data(): Collection<Array<Any>> = listOf(
      arrayOf("git@github.com:JetBrains/qodana.git", "https://github.com/jetbrains/qodana.git", true),
      arrayOf("git://github.com/JetBrains/qodana.git/", "http://github.com/jetbrains/qodana", true),
      arrayOf("ssh://build@github.com/JetBrains/qodana.git", "https://github.com/jetbrains/qodana.git", true),
      arrayOf("file:///Users/JetBrains/qodana.git", "file:///users/jetbrains/qodana", true),
      arrayOf("https://gitlab.com/JetBrains/qodana.git", "https://github.com/jetbrains/qodana.git", false),
      arrayOf("https://github.com/JetBrains/other.git", "https://github.com/jetbrains/qodana.git", false),
      arrayOf("https://github.com:2222/JetBrains/qodana.git", "https://github.com/jetbrains/qodana.git", false),
      arrayOf("not a valid remote URL", "https://github.com/jetbrains/qodana.git", false),
    )
  }
}
