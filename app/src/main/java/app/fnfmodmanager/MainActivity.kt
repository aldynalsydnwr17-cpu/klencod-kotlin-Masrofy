package app.fnfmodmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.fnfmodmanager.ui.FnfApp
import app.fnfmodmanager.ui.Pickers
import app.fnfmodmanager.ui.theme.FnfTheme

class MainActivity : ComponentActivity() {
    private val vm: MainViewModel by viewModels()

    private val zipPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.pickZip(uri)
    }
    private val sourceFolderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.pickSourceFolder(uri)
    }
    private val coverPicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.setCover(uri)
    }
    private val modsFolderPicker = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) vm.setModsTree(uri)
    }

    override fun onResume() {
        super.onResume()
        vm.refreshGame()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val pickers = Pickers(
            zip = { zipPicker.launch(arrayOf("*/*")) },
            sourceFolder = { sourceFolderPicker.launch(null) },
            modsFolder = { modsFolderPicker.launch(null) },
            cover = { coverPicker.launch(arrayOf("image/*")) },
        )
        setContent {
            val s by vm.ui.collectAsState()
            FnfTheme(amoled = s.amoled) { FnfApp(vm, pickers) }
        }
    }
}
