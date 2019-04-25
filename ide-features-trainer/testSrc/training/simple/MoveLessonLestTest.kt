package training.simple

import com.intellij.testFramework.UsefulTestCase
import training.learn.lesson.general.checkSwapMoreThan2Lines
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MoveLessonLestTest : UsefulTestCase() {
  fun testSeveralLinesMoved() {
    assertTrue {
      checkSwapMoreThan2Lines("""
        --------
        def hello
           puts "hello"
        end
        def world
           puts "world"
        end
        --------
      """.trimIndent(),"""
        --------
        def world
           puts "world"
        end
        def hello
           puts "hello"
        end
        --------
      """.trimIndent())
    }
  }
  fun testOnlyOneMoved() {
    assertFalse {
      checkSwapMoreThan2Lines("""
        --------
        def hello
           puts "hello"
        end
        def world
           puts "world"
        end
        --------
      """.trimIndent(),"""
        --------
        def hello
           puts "hello"
        end
        puts "world"
        def world
        end
        --------
      """.trimIndent())
    }
  }

}
