package uk.ac.tees.mad.minilibrary.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.barteksc.pdfviewer.PDFView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.ac.tees.mad.minilibrary.database.AppDatabase
import uk.ac.tees.mad.minilibrary.models.Book
import uk.ac.tees.mad.minilibrary.supabase.SupabaseClient
import java.io.File
import java.net.URL

class BookReaderViewModel(
    private val context: Context,
    private val bookId: String
) : ViewModel() {

    private val database = AppDatabase.getInstance(context)
    private val bookDao = database.bookDao()

    var book by mutableStateOf<Book?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var pdfFile by mutableStateOf<File?>(null)
        private set

    init {
        loadBook()
    }

    private fun loadBook() {
        viewModelScope.launch {
            try {
                isLoading = true
                errorMessage = null

                val entity = bookDao.getBookById(bookId)
                if (entity != null) {
                    book = Book(
                        id = entity.id,
                        userId = entity.userId,
                        title = entity.title,
                        subject = entity.subject,
                        fileName = entity.fileName,
                        fileUrl = entity.fileUrl,
                        uploadedAt = entity.uploadedAt
                    )

                    downloadPdf(entity.fileUrl)
                } else {
                    errorMessage = "Book not found"
                    isLoading = false
                }

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message ?: "Failed to load book"
                isLoading = false
            }
        }
    }

    private fun downloadPdf(fileUrl: String) {
        viewModelScope.launch {
            try {
                val cacheDir = context.cacheDir
                val pdfCacheFile = File(cacheDir, "temp_${bookId}.pdf")

                if (pdfCacheFile.exists()) {
                    pdfFile = pdfCacheFile
                    isLoading = false
                    return@launch
                }

                withContext(Dispatchers.IO) {
                    val userId = SupabaseClient.auth.currentUserOrNull()?.id ?: return@withContext
                    val fileName = book?.fileName ?: return@withContext
                    val filePath = "$userId/$fileName"

                    val bytes = SupabaseClient.storage
                        .from("books")
                        .downloadAuthenticated(filePath)

                    pdfCacheFile.writeBytes(bytes)
                    pdfFile = pdfCacheFile
                }

                isLoading = false

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = e.message ?: "Failed to download PDF"
                isLoading = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReaderScreen(
    bookId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: BookReaderViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BookReaderViewModel(context, bookId) as T
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = viewModel.book?.title ?: "Loading...",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                viewModel.isLoading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFF6366F1),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading PDF...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                viewModel.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Red
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Error",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = viewModel.errorMessage!!,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            )
                        ) {
                            Text("Go Back")
                        }
                    }
                }

                viewModel.pdfFile != null -> {
                    AndroidView(
                        factory = { context ->
                            PDFView(context, null).apply {
                                fromFile(viewModel.pdfFile)
                                    .enableSwipe(true)
                                    .swipeHorizontal(false)
                                    .enableDoubletap(true)
                                    .defaultPage(0)
                                    .enableAnnotationRendering(true)
                                    .password(null)
                                    .scrollHandle(null)
                                    .enableAntialiasing(true)
                                    .spacing(10)
                                    .load()
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "BookReaderScreenPreview")
@Composable
fun BookReaderScreenPreview() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Atomic Habits",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = Color(0xFF6366F1).copy(alpha = 0.4f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Atomic Habits",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C3E50)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Chapter 1 – The Surprising Power of Atomic Habits",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "PDF Viewer Area\n(Full-screen scrollable content would appear here)",
                    fontSize = 14.sp,
                    color = Color.Gray.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}