package com.imaniapp.uganda.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.imaniapp.uganda.R
import com.imaniapp.uganda.domain.model.Ayah
import com.imaniapp.uganda.domain.model.Surah
import com.imaniapp.uganda.presentation.theme.*
import com.imaniapp.uganda.presentation.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahReadingScreen(
    surahNumber: Int,
    onNavigateBack: () -> Unit,
    viewModel: QuranViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var selectedAyah by remember { mutableStateOf<Ayah?>(null) }
    
    LaunchedEffect(surahNumber) {
        viewModel.loadSurah(surahNumber)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Top App Bar
        SurahTopBar(
            surah = uiState.currentSurah,
            showTranslation = uiState.showTranslation,
            onNavigateBack = onNavigateBack,
            onToggleTranslation = { viewModel.toggleTranslation() },
            onFontSizeChange = { viewModel.updateFontSize(it) },
            fontSize = uiState.fontSize
        )
        
        when {
            uiState.isLoading -> {
                LoadingContent()
            }
            uiState.error != null -> {
                ErrorContent(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadSurah(surahNumber) }
                )
            }
            uiState.currentSurah != null -> {
                SurahContent(
                    surah = uiState.currentSurah!!,
                    showTranslation = uiState.showTranslation,
                    fontSize = uiState.fontSize,
                    bookmarks = uiState.bookmarks,
                    listState = listState,
                    onAyahLongPress = { ayah ->
                        selectedAyah = ayah
                        showBottomSheet = true
                    },
                    onAyahClick = { ayah ->
                        viewModel.selectAyah(ayah)
                    }
                )
            }
        }
    }
    
    // Bottom Sheet for Ayah actions
    if (showBottomSheet && selectedAyah != null) {
        AyahActionsBottomSheet(
            ayah = selectedAyah!!,
            surah = uiState.currentSurah!!,
            isBookmarked = viewModel.isBookmarked(selectedAyah!!.surahNumber, selectedAyah!!.numberInSurah),
            onDismiss = { 
                showBottomSheet = false
                selectedAyah = null
            },
            onBookmark = { ayah, note ->
                viewModel.addBookmark(uiState.currentSurah!!, ayah, note)
                showBottomSheet = false
                selectedAyah = null
            },
            onRemoveBookmark = { ayah ->
                viewModel.removeBookmark(ayah.surahNumber, ayah.numberInSurah)
                showBottomSheet = false
                selectedAyah = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahTopBar(
    surah: Surah?,
    showTranslation: Boolean,
    onNavigateBack: () -> Unit,
    onToggleTranslation: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    fontSize: Float
) {
    var showFontMenu by remember { mutableStateOf(false) }
    
    TopAppBar(
        title = {
            Column {
                Text(
                    text = surah?.englishName ?: "Loading...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (surah != null) {
                    Text(
                        text = surah.name,
                        style = ArabicTextStyle.copy(fontSize = 14.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            // Translation toggle
            IconButton(onClick = onToggleTranslation) {
                Icon(
                    if (showTranslation) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = "Toggle Translation",
                    tint = if (showTranslation) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Font size menu
            Box {
                IconButton(onClick = { showFontMenu = true }) {
                    Icon(Icons.Default.FormatSize, contentDescription = "Font Size")
                }
                
                DropdownMenu(
                    expanded = showFontMenu,
                    onDismissRequest = { showFontMenu = false }
                ) {
                    listOf(14f, 16f, 18f, 20f, 22f, 24f).forEach { size ->
                        DropdownMenuItem(
                            text = { Text("${size.toInt()}sp") },
                            onClick = {
                                onFontSizeChange(size)
                                showFontMenu = false
                            },
                            leadingIcon = {
                                if (fontSize == size) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun SurahContent(
    surah: Surah,
    showTranslation: Boolean,
    fontSize: Float,
    bookmarks: List<com.imaniapp.uganda.domain.model.QuranBookmark>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onAyahLongPress: (Ayah) -> Unit,
    onAyahClick: (Ayah) -> Unit
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Surah header
        item {
            SurahHeader(surah = surah)
        }
        
        // Bismillah (except for Surah At-Tawbah)
        if (surah.number != 9) {
            item {
                BismillahCard()
            }
        }
        
        // Ayahs
        itemsIndexed(surah.ayahs) { index, ayah ->
            val isBookmarked = bookmarks.any { 
                it.surahNumber == ayah.surahNumber && it.ayahNumber == ayah.numberInSurah 
            }
            
            AyahCard(
                ayah = ayah,
                showTranslation = showTranslation,
                fontSize = fontSize,
                isBookmarked = isBookmarked,
                onLongPress = { onAyahLongPress(ayah) },
                onClick = { onAyahClick(ayah) }
            )
        }
    }
}

@Composable
private fun SurahHeader(surah: Surah) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = surah.name,
                style = ArabicLargeTextStyle,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = surah.englishName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = surah.englishNameTranslation,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(
                    label = "${surah.numberOfAyahs} Verses",
                    icon = Icons.AutoMirrored.Filled.MenuBook
                )
                InfoChip(
                    label = surah.revelationType,
                    icon = Icons.Default.Place
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun BismillahCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = stringResource(R.string.bismillah),
            style = ArabicLargeTextStyle,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AyahCard(
    ayah: Ayah,
    showTranslation: Boolean,
    fontSize: Float,
    isBookmarked: Boolean,
    onLongPress: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isBookmarked) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Ayah number and bookmark indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = ayah.numberInSurah.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (isBookmarked) {
                    Icon(
                        Icons.Default.Bookmark,
                        contentDescription = "Bookmarked",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Arabic text
            Text(
                text = ayah.text,
                style = ArabicTextStyle.copy(fontSize = fontSize.sp),
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLongPress() },
                lineHeight = (fontSize * 1.5).sp
            )
            
            // Translation
            if (showTranslation && ayah.translation.text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    thickness = 1.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = ayah.translation.text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = (fontSize - 2).sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = ((fontSize - 2) * 1.4).sp
                )
                Text(
                    text = "— ${ayah.translation.translator}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AyahActionsBottomSheet(
    ayah: Ayah,
    surah: Surah,
    isBookmarked: Boolean,
    onDismiss: () -> Unit,
    onBookmark: (Ayah, String?) -> Unit,
    onRemoveBookmark: (Ayah) -> Unit
) {
    var noteText by remember { mutableStateOf("") }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ayah ${ayah.numberInSurah} Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Ayah preview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = ayah.text,
                        style = ArabicTextStyle.copy(fontSize = 16.sp),
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (ayah.translation.text.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ayah.translation.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bookmark section
            if (!isBookmarked) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Add a note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { onBookmark(ayah, noteText.ifBlank { null }) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Bookmark")
                }
            } else {
                Button(
                    onClick = { onRemoveBookmark(ayah) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.BookmarkRemove, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove Bookmark")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading Surah...",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error Loading Surah",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
} 