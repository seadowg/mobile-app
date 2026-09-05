package io.music_assistant.client.ui.compose.item

import io.music_assistant.client.data.model.client.items.AppMediaItem
import io.music_assistant.client.data.model.client.items.Artist
import io.music_assistant.client.data.model.server.ProviderMapping
import io.music_assistant.client.data.repository.MediaItemRepository

class FetchArtistItemsUseCase(private val mediaItemRepository: MediaItemRepository) {
    suspend fun run(
        artist: Artist,
        itemListBuilder: (ProviderMapping) -> ItemList,
    ): Pair<List<AppMediaItem>, ItemList>? {
        if (artist.providerMappings.isNullOrEmpty()) {
            return null
        }

        for (mapping in artist.providerMappings) {
            val itemList = itemListBuilder(mapping)
            val result = mediaItemRepository.fetchMediaItems(itemList.toRequest())
            val items = result.getOrNull() ?: emptyList()
            if (items.isNotEmpty()) {
                return Pair(items, itemList)
            }
        }

        val mapping = artist.providerMappings.first()
        return Pair(emptyList(), itemListBuilder(mapping))
    }
}
