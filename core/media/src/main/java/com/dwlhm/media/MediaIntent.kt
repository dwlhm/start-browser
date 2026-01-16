package com.dwlhm.media

object MediaIntent {
    object Action {
        const val INITIALIZE = "com.dwlhm.startbrowser.media.INITIALIZE"
        const val UPDATE_STATE = "com.dwlhm.startbrowser.media.UPDATE_STATE"
        const val UPDATE_METADATA = "com.dwlhm.startbrowser.media.UPDATE_METADATA"
    }

    object Extra {
        const val TAB_ID = "extra_tab_id"
        const val STATE = "extra_state"
        const val SESSION = "extra_session"
        const val TITLE = "extra_title"
        const val ARTIST = "extra_artist"
        const val ALBUM = "extra_album"
        const val ARTWORK = "extra_artwork"
    }
}