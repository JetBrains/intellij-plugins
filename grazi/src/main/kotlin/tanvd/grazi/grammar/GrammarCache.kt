package tanvd.grazi.grammar

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.LoadingCache
import tanvd.grazi.model.Typo

class GrammarCache {
    private val cache: LoadingCache<Int, List<Typo>> = Caffeine.newBuilder()
            .maximumSize(10000)
            .build{ null }

    fun contains(str: String): Boolean {
        return cache.getIfPresent(str.hashCode()) != null
    }

    fun get(str: String): List<Typo> {
        return cache.getIfPresent(str.hashCode()) ?: return emptyList()
    }

    fun set(str: String, typos: List<Typo>) {
        cache.put(str.hashCode(), typos)
    }
}
