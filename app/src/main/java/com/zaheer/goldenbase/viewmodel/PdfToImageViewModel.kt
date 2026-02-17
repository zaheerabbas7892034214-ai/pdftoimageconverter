package com.zaheer.goldenbase.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.goldenbase.data.repository.ConversionRepository
import com.zaheer.goldenbase.data.repository.SettingsRepository
import com.zaheer.goldenbase.model.ConversionEntity
import com.zaheer.goldenbase.model.ConversionType
import com.zaheer.goldenbase.util.PdfConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PdfToImageViewModel(context: Context) : ViewModel() {
    private val pdfConverter = PdfConverter(context)
    private val conversionRepository = ConversionRepository()
    private val settingsRepository = SettingsRepository(context)

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _isConverting = MutableStateFlow(false)
    val isConverting: StateFlow<Boolean> = _isConverting.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    fun convertPdfToImages(uri: Uri, fileName: String) {
        viewModelScope.launch {
            _isConverting.value = true
            _error.value = null
            try {
                val pageCount = pdfConverter.getPdfPageCount(uri)
                _totalPages.value = pageCount
                pdfConverter.convertPdfToImages(uri) { currentPage ->
                    _progress.value = ((currentPage + 1) * 100) / pageCount
                }
                val conversion = ConversionEntity(
                    type = ConversionType.PDF_TO_IMAGE.name,
                    inputFileName = fileName,
                    outputFileName = "images_$fileName",
                    pageCount = pageCount,
                    isPro = false
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

    fun cancelConversion() {
        pdfConverter.cancel()
        _isConverting.value = false
    }
}