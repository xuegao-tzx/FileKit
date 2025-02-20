package io.github.vinceglb.filekit.dialogs.platform.swing

import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.platform.PlatformFilePicker
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter
import kotlin.coroutines.resume

internal class SwingFilePicker : PlatformFilePicker {

    init {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (ex: Throwable) {
            println("Failed to set native UI for JFileChooser")
        }
    }

    override suspend fun openFilePicker(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        platformSettings: FileKitDialogSettings,
    ): File? = callSwingFilePicker(
        title = title,
        mode = JFileChooser.FILES_ONLY,
        isMultiSelectionEnabled = false,
        initialDirectory = initialDirectory,
        fileExtensions = fileExtensions,
        platformSettings = platformSettings,
    )?.firstOrNull()

    override suspend fun openFilesPicker(
        initialDirectory: String?,
        fileExtensions: List<String>?,
        title: String?,
        platformSettings: FileKitDialogSettings,
    ): List<File>? = callSwingFilePicker(
        title = title,
        mode = JFileChooser.FILES_ONLY,
        isMultiSelectionEnabled = true,
        initialDirectory = initialDirectory,
        fileExtensions = fileExtensions,
        platformSettings = platformSettings,
    )

    override suspend fun openDirectoryPicker(
        initialDirectory: String?,
        title: String?,
        platformSettings: FileKitDialogSettings,
    ): File? =
        callSwingFilePicker(
            title = title,
            mode = JFileChooser.DIRECTORIES_ONLY,
            isMultiSelectionEnabled = false,
            initialDirectory = initialDirectory,
            fileExtensions = null,
            platformSettings = platformSettings,
        )?.firstOrNull()

    private suspend fun callSwingFilePicker(
        title: String?,
        mode: Int,
        isMultiSelectionEnabled: Boolean,
        initialDirectory: String?,
        fileExtensions: List<String>?,
        platformSettings: FileKitDialogSettings,
    ): List<File>? = suspendCancellableCoroutine { continuation ->
        val jFileChooser = JFileChooser(initialDirectory)
        jFileChooser.fileSelectionMode = mode
        jFileChooser.isMultiSelectionEnabled = isMultiSelectionEnabled

        if(fileExtensions != null) {
            val filter = FileNameExtensionFilter(null, *fileExtensions.toTypedArray())
            jFileChooser.addChoosableFileFilter(filter)
        }

        if (title != null) {
            jFileChooser.dialogTitle = title
        }

        val returnValue = jFileChooser.showOpenDialog(platformSettings.parentWindow)
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            continuation.resume(jFileChooser.selectedFiles.toList())
        }

        continuation.invokeOnCancellation { jFileChooser.cancelSelection() }
    }
}