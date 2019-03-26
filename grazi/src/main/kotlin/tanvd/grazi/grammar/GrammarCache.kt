package tanvd.grazi.grammar

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache

object GrammarCache {
    private val cache: LoadingCache<Int, Int> = Caffeine.newBuilder()
            .maximumSize(10000)
            .build { key -> key }

    fun isValid(str: String): Boolean {
        return cache.getIfPresent(str.hashCode()) != null
    }

    fun setValid(str: String) {
        cache.put(str.hashCode(), str.hashCode())
    }
}
