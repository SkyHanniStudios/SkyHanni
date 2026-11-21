package at.hannibal2.skyhanni.test.framework.base

import at.hannibal2.skyhanni.test.framework.NeedsStorage
import at.hannibal2.skyhanni.test.framework.fixtures.StorageFixture
import org.junit.jupiter.api.BeforeEach

@NeedsStorage
abstract class StorageTest : ConfigTest() {
    protected lateinit var storage: StorageFixture
        private set

    @BeforeEach
    fun bindStorageFixture(storage: StorageFixture) {
        this.storage = storage
    }
}
