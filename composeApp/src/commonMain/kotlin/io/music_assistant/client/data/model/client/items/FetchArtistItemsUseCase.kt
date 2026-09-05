package io.music_assistant.client.data.model.client.items

import io.music_assistant.client.data.model.server.ProviderMapping
import io.music_assistant.client.data.repository.MediaItemRepository
import io.music_assistant.client.ui.compose.item.ItemList
import io.music_assistant.client.ui.compose.item.toRequest

class FetchArtistItemsUseCase(private val mediaItemRepository: MediaItemRepository) {
    suspend fun run(
        artist: Artist,
        itemListBuilder: (ProviderMapping) -> ItemList,
    ): ArtistItems? {
        if (artist.providerMappings.isNullOrEmpty()) {
            return null
        }

        val itemLists = artist.providerMappings.map { itemListBuilder(it) }
        for (itemList in itemLists) {
            val result = mediaItemRepository.fetchMediaItems(itemList.toRequest())
            val items = result.getOrNull() ?: emptyList()
            if (items.isNotEmpty()) {
                return ArtistItems(items, itemList, itemLists)
            }
        }

        return ArtistItems(emptyList(), itemLists.first(), itemLists)
    }

    data class ArtistItems(
        val items: List<AppMediaItem>,
        val itemList: ItemList,
        val options: List<ItemList>,
    )
}
