package at.hannibal2.skyhanni.test.framework.fixtures

import at.hannibal2.skyhanni.config.storage.PlayerSpecificStorage
import at.hannibal2.skyhanni.config.storage.ProfileSpecificStorage
import at.hannibal2.skyhanni.data.ProfileStorageData
import at.hannibal2.skyhanni.test.framework.TestFixture
import org.junit.jupiter.api.extension.ExtensionContext

class StorageFixture : TestFixture {
    lateinit var player: PlayerSpecificStorage
        private set
    lateinit var profile: ProfileSpecificStorage
        private set

    fun switchProfile(name: String): ProfileSpecificStorage {
        profile = ProfileSpecificStorage(name)
        ProfileStorageData.profileSpecific = profile
        return profile
    }

    override fun setUp(context: ExtensionContext) {
        player = PlayerSpecificStorage()
        ProfileStorageData.playerSpecific = player
        switchProfile(DEFAULT_PROFILE)
        ProfileStorageData.loaded = true
    }

    override fun tearDown(context: ExtensionContext) {
        ProfileStorageData.playerSpecific = null
        ProfileStorageData.profileSpecific = null
        ProfileStorageData.loaded = false
    }

    private companion object {
        const val DEFAULT_PROFILE = "test"
    }
}
