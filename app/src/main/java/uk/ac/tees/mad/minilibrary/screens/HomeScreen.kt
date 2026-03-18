package uk.ac.tees.mad.minilibrary.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.launch
import uk.ac.tees.mad.minilibrary.models.Book
import uk.ac.tees.mad.minilibrary.supabase.SupabaseClient

class HomeViewModel : ViewModel() {

    var books by mutableStateOf<List<Book>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var showUploadDialog by mutableStateOf(false)
        private set

    var userName by mutableStateOf("")
        private set

    init {
        loadBooks()
        loadUserInfo()
    }

    private fun loadUserInfo() {
        viewModelScope.launch {
            try {
                val user = SupabaseClient.auth.currentUserOrNull()
                userName = user?.email?.substringBefore('@') ?: "User"
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadBooks() {
        viewModelScope.launch {
            try {
                isLoading = true
                val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch

                val result = SupabaseClient.database
                    .from("books")
                    .select()
                    .decodeList<Book>()

                books = result.sortedByDescending { it.uploadedAt }
                isLoading = false

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error loading books: ${e.message}")
                isLoading = false
            }
        }
    }

    fun toggleUploadDialog() {
        showUploadDialog = !showUploadDialog
    }

    fun deleteBook(book: Book) {
        viewModelScope.launch {
            try {
                // Delete from database
                SupabaseClient.database
                    .from("books")
                    .delete {
                        filter { eq("id", book.id) }
                    }

                // Delete from storage
                val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@launch
                SupabaseClient.storage
                    .from("books")
                    .delete("$userId/${book.fileName}")

                // Reload books
                loadBooks()

            } catch (e: Exception) {
                e.printStackTrace()
                println("Error deleting book: ${e.message}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToReader: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "MiniLibrary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Welcome, ${viewModel.userName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6366F1),
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleUploadDialog() },
                containerColor = Color(0xFF6366F1),
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Book"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewModel.isLoading) {
                // Loading State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF6366F1)
                    )
                }
            } else if (viewModel.books.isEmpty()) {
                // Empty State
                EmptyLibraryView(
                    onAddBook = { viewModel.toggleUploadDialog() }
                )
            } else {
                // Books Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.books) { book ->
                        BookCard(
                            book = book,
                            onClick = { onNavigateToReader(book.id) },
                            onDelete = { viewModel.deleteBook(book) }
                        )
                    }
                }
            }
        }

        // Upload Dialog
        if (viewModel.showUploadDialog) {
            UploadBookDialog(
                onDismiss = { viewModel.toggleUploadDialog() },
                onBookUploaded = {
                    viewModel.toggleUploadDialog()
                    viewModel.loadBooks()
                }
            )
        }
    }
}

@Composable
fun EmptyLibraryView(
    onAddBook: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.MenuBook,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color.Gray.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your library is empty",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start building your collection\nby adding your first book",
            fontSize = 14.sp,
            color = Color.Gray.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddBook,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6366F1)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Book")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Book Cover (Gradient with Icon)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF6366F1),
                                Color(0xFF8B5CF6)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color.White.copy(alpha = 0.8f)
                )

                // Delete button
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Book Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = book.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Label,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF6366F1)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = book.subject,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Book?") },
            text = { Text("Are you sure you want to delete \"${book.title}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UploadBookDialog(
    onDismiss: () -> Unit,
    onBookUploaded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
    }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("Add New Book") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Book Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isUploading
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject/Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isUploading,
                    placeholder = { Text("e.g., Science, History, Novel") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { filePickerLauncher.launch("application/pdf") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6366F1)
                    )
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedFileUri != null) "PDF Selected ✓" else "Select PDF File"
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || subject.isBlank() || selectedFileUri == null) {
                        errorMessage = "Please fill all fields and select a PDF"
                        return@Button
                    }

                    isUploading = true
                    errorMessage = null

                    // This will be implemented in the next part with actual upload
                    // For now, just show a message
                    errorMessage = "Upload feature coming soon!"
                    isUploading = false
                },
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White
                    )
                } else {
                    Text("Upload")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("Cancel")
            }
        }
    )
}