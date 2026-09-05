package io.music_assistant.client.data.model.client.items

import io.music_assistant.client.api.Request
import io.music_assistant.client.data.model.client.AppMediaItemFixtures
import io.music_assistant.client.data.model.client.items.support.StubMediaItemRepository
import io.music_assistant.client.data.model.server.ProviderMapping
import io.music_assistant.client.ui.compose.item.ItemList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FetchArtistItemsUseCaseTest {
    private val mediaItemRepository = StubMediaItemRepository()

    @Test
    fun `returns first provider's items when it has items`() = runTest {
        val provider1 = ProviderMapping("1", "niflheim", "niflheim-1")
        val provider2 = ProviderMapping("1", "muspelheim", "muspelheim-1")
        val artist = AppMediaItemFixtures.artist(providerMappings = listOf(provider1, provider2))
        val provider1Albums = listOf(AppMediaItemFixtures.album(artist = artist))
        val provider2Albums = listOf(AppMediaItemFixtures.album(artist = artist))

        mediaItemRepository.setItemsResult(
            request = Request.Artist.getAlbums(provider1.itemId, provider1.providerInstance),
            result = Result.success(provider1Albums),
        )

        mediaItemRepository.setItemsResult(
            request = Request.Artist.getAlbums(provider2.itemId, provider2.providerInstance),
            result = Result.success(provider2Albums),
        )

        val useCase = FetchArtistItemsUseCase(mediaItemRepository)
        val artistItems = useCase.run(artist) { ItemList.ArtistAlbums(it) }

        assertEquals(
            FetchArtistItemsUseCase.ArtistItems(
                items = provider1Albums,
                itemList = ItemList.ArtistAlbums(provider1),
                options = listOf(ItemList.ArtistAlbums(provider1), ItemList.ArtistAlbums(provider2)),
            ),
            artistItems,
        )
    }

    @Test
    fun `returns first non-empty provider's items`() = runTest {
        val provider1 = ProviderMapping("1", "niflheim", "niflheim-1")
        val provider2 = ProviderMapping("1", "muspelheim", "muspelheim-1")
        val artist = AppMediaItemFixtures.artist(providerMappings = listOf(provider1, provider2))
        val provider2Albums = listOf(AppMediaItemFixtures.album(artist = artist))

        mediaItemRepository.setItemsResult(
            request = Request.Artist.getAlbums(provider1.itemId, provider1.providerInstance),
            result = Result.success(emptyList()),
        )

        mediaItemRepository.setItemsResult(
            request = Request.Artist.getAlbums(provider2.itemId, provider2.providerInstance),
            result = Result.success(provider2Albums),
        )

        val useCase = FetchArtistItemsUseCase(mediaItemRepository)
        val artistItems = useCase.run(artist) { ItemList.ArtistAlbums(it) }

        assertEquals(
            FetchArtistItemsUseCase.ArtistItems(
                items = provider2Albums,
                itemList = ItemList.ArtistAlbums(provider2),
                options = listOf(ItemList.ArtistAlbums(provider1), ItemList.ArtistAlbums(provider2)),
            ),
            artistItems,
        )
    }

    @Test
    fun `returns with first provider if all providers are empty`() = runTest {
        val provider1 = ProviderMapping("1", "niflheim", "niflheim-1")
        val provider2 = ProviderMapping("1", "muspelheim", "muspelheim-1")
        val artist = AppMediaItemFixtures.artist(providerMappings = listOf(provider1, provider2))

        mediaItemRepository.setItemsResult(
            request = Request.Artist.getAlbums(provider1.itemId, provider1.providerInstance),
            result = Result.success(emptyList()),
        )

        mediaItemRepository.setItemsResult(
            request = Request.Artist.getAlbums(provider2.itemId, provider2.providerInstance),
            result = Result.success(emptyList()),
        )

        val useCase = FetchArtistItemsUseCase(mediaItemRepository)
        val artistItems = useCase.run(artist) { ItemList.ArtistAlbums(it) }

        assertEquals(
            FetchArtistItemsUseCase.ArtistItems(
                items = emptyList(),
                itemList = ItemList.ArtistAlbums(provider1),
                options = listOf(ItemList.ArtistAlbums(provider1), ItemList.ArtistAlbums(provider2)),
            ),
            artistItems,
        )
    }

    @Test
    fun `returns null when artist has no provider mappings`() = runTest {
        val artist = AppMediaItemFixtures.artist(providerMappings = emptyList())

        val useCase = FetchArtistItemsUseCase(mediaItemRepository)
        val artistItems = useCase.run(artist) { ItemList.ArtistAlbums(it) }
        assertEquals(null, artistItems)
    }
}
