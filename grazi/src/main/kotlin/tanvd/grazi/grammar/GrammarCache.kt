package tanvd.grazi.grammar

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import tanvd.grazi.model.Typo

class GrammarCache {
    private val cache: LoadingCache<Int, Set<Typo>> = Caffeine.newBuilder()
            .maximumSize(30_000)
            .build { null }

    fun contains(str: String) = cache.getIfPresent(str.hashCode()) != null

    fun get(str: String) = cache.getIfPresent(str.hashCode()) ?: emptySet()

    fun put(str: String, typos: Set<Typo>) {
        cache.put(str.hashCode(), typos)
    }

    fun reset() {
        cache.invalidateAll()
    }
}
