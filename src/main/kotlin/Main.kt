import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import javax.swing.JFileChooser
import javax.swing.SwingWorker
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.imageio.ImageIO

@Composable
fun App() {
    var projectName by remember { mutableStateOf(TextFieldValue()) }
    var folderPath by remember { mutableStateOf("No Folder Chosen") }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showProgressDialog by remember { mutableStateOf(false) }
    var showProjectExistsDialog by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var downloadedSize by remember { mutableStateOf(0L) }
    var operationMessage by remember { mutableStateOf("") }

    MaterialTheme {
        Surface(color = Color(0xFFE3F2FD), modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource("celeris-with-name.png"),
                        contentDescription = "Logo",
                        modifier = Modifier.size(100.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Celeris Project Creator",
                        style = MaterialTheme.typography.h5,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = projectName,
                    onValueChange = {
                        projectName = it
                        errorMessage = ""
                    },
                    label = { Text("Enter Project Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp) // Rounded corners
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        val selectedFolder = chooseDirectory()
                        if (selectedFolder != null) {
                            folderPath = selectedFolder.absolutePath
                            errorMessage = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp) // Rounded corners
                ) {
                    Text("Choose Folder")
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(text = folderPath, style = MaterialTheme.typography.body1)

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (folderPath != "No Folder Chosen" && projectName.text.isNotEmpty()) {
                            val projectDir = File(folderPath, projectName.text)
                            if (projectDir.exists()) {
                                showProjectExistsDialog = true
                            } else {
                                // Check for internet connection
                                if (!isInternetAvailable()) {
                                    showNoInternetDialog = true // Show no internet dialog
                                } else {
                                    showProgressDialog = true
                                    operationMessage = "Downloading..."
                                    downloadedSize = 0

                                    object : SwingWorker<Void, Long>() {
                                        override fun doInBackground(): Void? {
                                            runBlocking {
                                                downloadAndExtractProject(folderPath, projectName.text) { downloadedBytes ->
                                                    publish(downloadedBytes)
                                                }
                                            }
                                            return null
                                        }

                                        override fun process(chunks: List<Long>) {
                                            downloadedSize = chunks.last()
                                        }

                                        override fun done() {
                                            showSuccessDialog = true
                                            showProgressDialog = false
                                        }
                                    }.execute()
                                }
                            }
                        } else {
                            errorMessage = "Please provide a valid project name and folder."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp) // Rounded corners
                ) {
                    Text("Create Project")
                }

                if (errorMessage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = errorMessage, color = Color.Red)
                }

                if (showProgressDialog) {
                    AlertDialog(
                        onDismissRequest = { showProgressDialog = false },
                        title = { Text("Progress") },
                        text = {
                            Column {
                                Text(operationMessage)
                                val progress = if (downloadedSize > 0) downloadedSize / (26.63 * 1024 * 1024).toFloat() else 0f
                                LinearProgressIndicator(progress = progress.coerceIn(0f, 1f))
                                Text("Downloaded: ${downloadedSize / (1024 * 1024)} MB")
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showProgressDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                if (showSuccessDialog) {
                    AlertDialog(
                        onDismissRequest = { showSuccessDialog = false },
                        title = { Text("Success") },
                        text = { Text("Project created successfully at $folderPath/${projectName.text}") },
                        confirmButton = {
                            Button(onClick = { showSuccessDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }

                if (showProjectExistsDialog) {
                    AlertDialog(
                        onDismissRequest = { showProjectExistsDialog = false },
                        title = { Text("Error") },
                        text = { Text("A project with the name '${projectName.text}' already exists in the chosen folder.") },
                        confirmButton = {
                            Button(onClick = { showProjectExistsDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }

                // No Internet Dialog
                if (showNoInternetDialog) {
                    AlertDialog(
                        onDismissRequest = { showNoInternetDialog = false },
                        title = { Text("Error") },
                        text = { Text("Internet connection is required to create a project.") },
                        confirmButton = {
                            Button(onClick = { showNoInternetDialog = false }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}

// Function to check internet availability
fun isInternetAvailable(): Boolean {
    return try {
        val url = URL("http://www.google.com")
        val connection = url.openConnection() as HttpURLConnection
        connection.apply {
            connectTimeout = 3000 // Timeout in milliseconds
            readTimeout = 3000
            requestMethod = "HEAD"
        }
        connection.responseCode == 200
    } catch (e: Exception) {
        false
    }
}

fun chooseDirectory(): File? {
    val chooser = JFileChooser()
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    val result = chooser.showOpenDialog(null)
    return if (result == JFileChooser.APPROVE_OPTION) {
        chooser.selectedFile
    } else {
        null
    }
}

suspend fun downloadFile(url: String, destination: File, onProgress: (Long) -> Unit) {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.connect()

    val inputStream = BufferedInputStream(connection.inputStream)
    val outputStream = FileOutputStream(destination)

    val data = ByteArray(1024)
    var total: Long = 0
    var count: Int

    while (inputStream.read(data).also { count = it } != -1) {
        total += count
        outputStream.write(data, 0, count)
        onProgress(total) // Update progress with total bytes downloaded
    }

    outputStream.flush()
    outputStream.close()
    inputStream.close()
}

suspend fun extractZip(zipFile: File, destDir: File, onProgress: (Float) -> Unit) {
    val zipInputStream = ZipInputStream(FileInputStream(zipFile))
    var totalBytes = 0L

    var entry: ZipEntry?
    while (zipInputStream.nextEntry.also { entry = it } != null) {
        val newFile = File(destDir, entry!!.name)
        if (entry!!.isDirectory) {
            newFile.mkdirs()
        } else {
            File(newFile.parent).mkdirs()
            FileOutputStream(newFile).use { outputStream ->
                val buffer = ByteArray(1024)
                var bytesRead: Int
                while (zipInputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead.toLong()
                    // Since we don't know the total bytes for the zip, we won't report progress here
                }
            }
        }
    }
    zipInputStream.close()
}

fun renameCMakeListsFile(folderPath: String, oldName: String, newName: String) {
    val cmakeFile = File(folderPath, "CMakeLists.txt")
    if (cmakeFile.exists()) {
        val content = cmakeFile.readText().replace(oldName, newName)
        cmakeFile.writeText(content)
    }
}

fun copyProjectFiles(sourceDir: File, targetDir: File) {
    if (!targetDir.exists()) {
        targetDir.mkdirs()
    }
    sourceDir.listFiles()?.forEach { file ->
        if (file.isDirectory) {
            copyProjectFiles(file, File(targetDir, file.name))
        } else {
            file.copyTo(File(targetDir, file.name), overwrite = true)
        }
    }
}

suspend fun downloadAndExtractProject(folderPath: String, projectName: String, onProgress: (Long) -> Unit) {
    val zipFile = File("$folderPath/CelerisStarterProject.zip")
    val url = "https://github.com/Alazar42/CelerisStarterProject/archive/refs/heads/main.zip"

    downloadFile(url, zipFile) { downloadedBytes ->
        onProgress(downloadedBytes) // Update the downloaded bytes
    }

    // Create a temporary directory to extract the zip file
    val tempExtractedDir = File(folderPath, "temp_extracted")
    extractZip(zipFile, tempExtractedDir) { progress ->
        // No need to update progress here since we don't know total zip bytes
    }

    // Navigate into the 'CelerisStarterProject-main' folder
    val projectFolder = File(tempExtractedDir, "CelerisStarterProject-main")
    if (projectFolder.exists() && projectFolder.isDirectory) {
        // Copy the files from the project folder to the chosen folder
        copyProjectFiles(projectFolder, File(folderPath, projectName))
    }

    // Clean up: delete the zip file and the temporary extracted folder
    zipFile.delete()
    tempExtractedDir.deleteRecursively()
    renameCMakeListsFile(File(folderPath, projectName).absolutePath, "Celeris", projectName)
}

@Preview
@Composable
fun PreviewApp() {
    App()
}

fun main() = application {
    // Load the window icon image using ImageIO for the application icon
    val iconImage = painterResource("celeris-with-name.png")

    Window(
        onCloseRequest = ::exitApplication,
        title = "Celeris Project Creator",
        icon = iconImage // Set the window icon
    ) {
        App()
    }
}
