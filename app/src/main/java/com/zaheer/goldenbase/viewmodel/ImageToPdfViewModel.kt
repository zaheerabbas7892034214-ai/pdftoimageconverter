package com.zaheer.goldenbase.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.goldenbase.data.repository.ConversionRepository
import com.zaheer.goldenbase.model.ConversionEntity
import com.zaheer.goldenbase.model.ConversionType
import com.zaheer.goldenbase.util.ImageConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ImageToPdfViewModel(context: Context) : ViewModel() {
    private val imageConverter = ImageConverter(context)
    private val conversionRepository = ConversionRepository()
    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages.asStateFlow()
    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()
    private val _isConverting = MutableStateFlow(false)
    val isConverting: StateFlow<Boolean> = _isConverting.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun selectImages(uris: List<Uri>) {
        _selectedImages.value = uris
    }

    fun reorderImages(fromIndex: Int, toIndex: Int) {
        val list = _selectedImages.value.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        _selectedImages.value = list
    }

    fun convertImagesToPdf(outputFileName: String) {
        viewModelScope.launch {
            _isConverting.value = true
            _error.value = null
            try {
                val totalImages = _selectedImages.value.size
                imageConverter.createPdfFromImages(
                    _selectedImages.value, outputFileName
                ) { processed ->
                    _progress.value = ((processed + 1) * 100) / totalImages
                }
                val conversion = ConversionEntity(
                    type = ConversionType.IMAGE_TO_PDF.name,
                    inputFileName = "${totalImages}_images",
                    outputFileName = outputFileName,
                    pageCount = totalImages
                )
                conversionRepository.insert(conversion)
                _progress.value = 100
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isConverting.value = false
            }
        }
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value.filter { it != uri }
    }
}