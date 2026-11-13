package at.hannibal2.skyhanni.utils.collection

import kotlin.time.Duration

class WeakIdentityCache<K : Any, V : Any> private constructor(
    private val cache: TimeLimitedCache<K, V>,
    private val compute: (K) -> V,
) : MutableMap<K, V> by cache {

    constructor(expireAfterWrite: Duration, compute: (K) -> V) : this(
        TimeLimitedCache(expireAfterWrite, useWeakKeys = true),
        compute,
    )

    override fun get(key: K): V = cache.getOrPut(key) { compute(key) }
}
