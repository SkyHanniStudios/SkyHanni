package at.hannibal2.skyhanni.utils.collection

import kotlin.time.Duration

// TODO Make WeakIdentityCache be the only cache with weak keys, and remove the useWeakKeys parameter from all other caches
class WeakIdentityCache<K : Any, V : Any> private constructor(
    private val cache: TimeLimitedCache<K, V>,
    private val mappingFunction: (K) -> V,
) : MutableMap<K, V> by cache {

    constructor(expireAfterWrite: Duration, mappingFunction: (K) -> V) : this(
        TimeLimitedCache(expireAfterWrite, useWeakKeys = true),
        mappingFunction,
    )

    override fun get(key: K): V = cache.computeIfAbsent(key, mappingFunction)
}
