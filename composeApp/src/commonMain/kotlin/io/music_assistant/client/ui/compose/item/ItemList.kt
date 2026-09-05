package io.music_assistant.client.ui.compose.item

import io.music_assistant.client.api.Request
import io.music_assistant.client.data.model.client.MediaType
import io.music_assistant.client.data.model.server.ProviderMapping
import io.music_assistant.client.data.model.server.ServerMediaItem
import kotlinx.serialization.Serializable

@Serializable
sealed interface ItemList {
    val mediaType: MediaType
    val providerDomain: String?

    @Serializable
    data class ArtistAlbums(
        val mappings: List<Pair<String, String>>,
        override val providerDomain: String,
    ) : ItemList {
        override val mediaType: MediaType = MediaType.ALBUM

        constructor(providerMapping: ProviderMapping) : this(
            mappings = listOf(Pair(providerMapping.providerInstance, providerMapping.itemId)),
            providerDomain = providerMapping.providerDomain,
        )

        constructor(providerMappings: List<ProviderMapping>) : this(
            mappings = providerMappings.map { Pair(it.providerInstance, it.itemId) },
            providerDomain = providerMappings.first().providerDomain,
        )
    }

    @Serializable
    data class ArtistTopTracks(
        val mappings: List<Pair<String, String>>,
        override val providerDomain: String,
    ) : ItemList {
        override val mediaType: MediaType = MediaType.TRACK

        constructor(providerMappings: List<ProviderMapping>) : this(
            mappings = providerMappings.map { Pair(it.providerInstance, it.itemId) },
            providerDomain = providerMappings.first().providerDomain,
        )
    }

    @Serializable
    data class ArtistLibrary(val artistId: String) : ItemList {
        override val mediaType: MediaType = MediaType.ALBUM
        override val providerDomain: String? = null
    }
}

fun ItemList.toRequests(): List<Request> {
    return when (this) {
        is ItemList.ArtistAlbums -> {
            this.mappings.map {
                val (providerInstance, itemId) = it
                Request.Artist.getAlbums(itemId, providerInstance)
            }
        }

        is ItemList.ArtistTopTracks -> {
            this.mappings.map {
                val (providerInstance, itemId) = it
                Request.Artist.getTopTracks(itemId, providerInstance)
            }
        }

        is ItemList.ArtistLibrary -> listOf(
            Request.Artist.getAlbums(
                this.artistId,
                ServerMediaItem.LIBRARY_PROVIDER,
            ),
        )
    }
}
