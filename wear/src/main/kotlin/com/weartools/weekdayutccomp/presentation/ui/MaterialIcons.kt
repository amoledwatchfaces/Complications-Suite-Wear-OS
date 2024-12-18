package com.weartools.weekdayutccomp.presentation.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.material.Icon
import com.weartools.weekdayutccomp.activity.Icon
import com.weartools.weekdayutccomp.theme.wearColorPalette
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun IconItem(
    icon: Icon,
    onClick: () -> Unit
) {
    var vector = Icons.Filled.Image

    if (icon.image != null) {
        vector = icon.image!!
    }

    Icon(
        modifier = Modifier.clickable(
            onClick = onClick
        ),
        imageVector = vector,
        contentDescription = "Refresh Icon",
        tint = wearColorPalette.secondary,
    )
}
object ImageUtil {
    fun createImageVector(name: String): ImageVector? {
        try {
            val className = "androidx.compose.material.icons.filled.${name}Kt"
            val cl = Class.forName(className)
            val method = cl.declaredMethods.first()
            return method.invoke(null, Icons.Filled) as ImageVector
        } catch (ex: Exception) {
            Log.e("ImageNotFound", name)
            return null
        }
    }

}
interface IconsViewModel {

    val state: StateFlow<IconsState>

    fun updateSearch(search: String)

    fun onClickIcon(icon: Icon)
}

data class IconsState(
    val icons: List<Icon> = emptyList(),
    val loading: Boolean = true
)

class IconsViewModelImp(
    private val applicationContext: Context
) : IconsViewModel, ViewModel() {

    private val _state = MutableStateFlow(IconsState())
    override val state = _state

    private var searchJob: Job? = null

    init {
        updateSearch("")
    }

    override fun updateSearch(search: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)

            _state.update { it.copy(loading = true) }

            val icons = getNamesIcons()
                .filter { it.contains(search, ignoreCase = true) }
                .map { parseIconItem(it) }

            updateSelection(icons)
        }
    }

    private fun updateSelection(icons: List<Icon>) {
        _state.update { it.copy(icons = icons, loading =  false) }
    }

    override fun onClickIcon(icon: Icon) {

    }


    private fun parseIconItem(line: String): Icon {
        val splitted = line.split(",")
        val id = splitted[0]
        val name = splitted[1]
        val image = ImageUtil.createImageVector(id)

        return Icon(id, name, image)
    }

    private fun getNamesIcons(): List<String> {
        val inputStream = applicationContext.assets.open("icons-names.txt")
        val reader = BufferedReader(InputStreamReader(inputStream))
        val lines = reader.readLines()
        reader.close()
        return lines
    }
}