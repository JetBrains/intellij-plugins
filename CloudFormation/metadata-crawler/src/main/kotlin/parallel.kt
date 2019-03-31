import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// Thanks https://github.com/holgerbrandl/kutils/blob/master/src/main/kotlin/de/mpicbg/scicomp/kutils/ParCollections.kt
// See also https://stackoverflow.com/a/35638609 / https://stackoverflow.com/questions/34697828/parallel-operations-on-kotlin-collections
fun <T, R> Iterable<T>.pmap(numThreads: Int = maxOf(Runtime.getRuntime().availableProcessors() - 2, 1), exec: ExecutorService = Executors.newFixedThreadPool(numThreads), transform: (T) -> R): List<R> {

  try {
    val result = exec.invokeAll(this.map { item -> Callable<R> { transform(item) } })
    return result.map { it.get() }
  } finally {
    exec.shutdownNow()
  }
}