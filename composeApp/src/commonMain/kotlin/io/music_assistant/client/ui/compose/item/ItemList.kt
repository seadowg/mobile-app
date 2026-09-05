package io.music_assistant.client.ui.compose.item

import io.music_assistant.client.api.Request
import io.music_assistant.client.data.model.client.MediaType
import io.music_assistant.client.data.model.server.ServerMediaItem
import kotlinx.serialization.Serializable

@Serializable
sealed interface ItemList {
    val mediaType: MediaType
    val providerDomain: String?

    @Serializable
    data class ArtistAlbums(
        val providerInstance: String,
        val artistId: String,
        override val providerDomain: String,
    ) : ItemList {
        override val mediaType: MediaType = MediaType.ALBUM
    }

    @Serializable
    data class ArtistTopTracks(
        val providerInstance: String,
        val artistId: String,
        override val providerDomain: String,
    ) : ItemList {
        override val mediaType: MediaType = MediaType.TRACK
    }

    @Serializable
    data class ArtistLibrary(val artistId: String) : ItemList {
        override val mediaType: MediaType = MediaType.ALBUM
        override val providerDomain: String? = null
    }
}

fun ItemList.toRequest(): Request {
    return when (this) {
        is ItemList.ArtistAlbums -> Request.Artist.getAlbums(
            this.artistId,
            this.providerInstance,
        )

        is ItemList.ArtistTopTracks -> Request.Artist.getTopTracks(
            this.artistId,
            this.providerInstance,
        )

        is ItemList.ArtistLibrary -> Request.Artist.getAlbums(
            this.artistId,
            ServerMediaItem.LIBRARY_PROVIDER,
        )
    }
}
