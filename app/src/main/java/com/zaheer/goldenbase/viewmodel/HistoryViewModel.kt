package com.zaheer.goldenbase.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zaheer.goldenbase.data.repository.ConversionRepository
import com.zaheer.goldenbase.model.ConversionEntity
import com.zaheer.goldenbase.model.ConversionType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val conversionRepository = ConversionRepository()

    val allConversions: StateFlow<List<ConversionEntity>> = conversionRepository.getAllConversions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val conversionCount: StateFlow<Int> = conversionRepository.getConversionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun deleteConversion(id: Long) {
        viewModelScope.launch {
            conversionRepository.delete(id)
        }
    }

    fun addConversion(conversion: ConversionEntity) {
        viewModelScope.launch {
            conversionRepository.insert(conversion)
        }
    }
}