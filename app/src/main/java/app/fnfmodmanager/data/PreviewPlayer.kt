package app.fnfmodmanager.data

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Plays the instrumental of a mod straight from its folder (the "music" row on the home screen). */
class PreviewPlayer(private val context: Context) {

    data class State(
        val title: String = "",
        val loaded: Boolean = false,
        val playing: Boolean = false,
        val position: Int = 0,
        val duration: Int = 0,
        val muted: Boolean = false,
    ) {
        val progress: Float get() = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f
    }

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    private var mp: MediaPlayer? = null
    private var ticker: Job? = null

    suspend fun load(scope: CoroutineScope, uri: Uri, title: String): Boolean {
        release()
        val player = try {
            withContext(Dispatchers.IO) {
                MediaPlayer().apply {
                    setDataSource(context, uri)
                    prepare()
                }
            }
        } catch (e: Exception) {
            return false
        }
        mp = player
        player.setOnCompletionListener {
            _state.value = _state.value.copy(playing = false, position = 0)
            runCatching { it.seekTo(0) }
        }
        _state.value = State(title = title, loaded = true, playing = false, position = 0, duration = player.duration)
        play(scope)
        return true
    }

    private fun play(scope: CoroutineScope) {
        val p = mp ?: return
        p.start()
        _state.value = _state.value.copy(playing = true)
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                val cur = mp ?: break
                _state.value = _state.value.copy(position = runCatching { cur.currentPosition }.getOrDefault(0), playing = cur.isPlaying)
                delay(250)
            }
        }
    }

    fun toggle(scope: CoroutineScope) {
        val p = mp ?: return
        if (p.isPlaying) {
            p.pause()
            _state.value = _state.value.copy(playing = false)
        } else {
            play(scope)
        }
    }

    fun seek(fraction: Float) {
        val p = mp ?: return
        val pos = (p.duration * fraction.coerceIn(0f, 1f)).toInt()
        p.seekTo(pos)
        _state.value = _state.value.copy(position = pos)
    }

    fun toggleMute() {
        val p = mp ?: return
        val muted = !_state.value.muted
        p.setVolume(if (muted) 0f else 1f, if (muted) 0f else 1f)
        _state.value = _state.value.copy(muted = muted)
    }

    fun release() {
        ticker?.cancel()
        ticker = null
        mp?.let { runCatching { it.stop() }; it.release() }
        mp = null
        _state.value = State()
    }
}
