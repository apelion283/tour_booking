package com.project17.tourbooking.activities.user.search.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.project17.tourbooking.helper.firestore_helper.FirestoreHelper
import com.project17.tourbooking.models.Tour
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {
    var historyItems = mutableStateListOf<String>()
        private set

    var inputValue = mutableStateOf("")
    var isSearched = mutableStateOf(false)
    var categorySelectedIndex = mutableIntStateOf(-1)
    var tourList = mutableStateListOf<Tour>()

    var categories by mutableStateOf<List<com.project17.tourbooking.models.Category>>(emptyList())
    var moneyPriceRange by mutableStateOf<Pair<Int, Int>?>(null)
    var starRating by mutableIntStateOf(1)

    init {
        fetchCategories()
    }
    var isLoading = mutableStateOf(false)
    private fun fetchCategories() {
        FirestoreHelper.getAllCategories { fetchedCategories ->
            categories = fetchedCategories
        }
    }

    fun filterTourByNameCategoryStarAndPrice(
        categoryId: String = "",
        rate: Int = starRating,
        moneyRange: Pair<Int, Int>? = moneyPriceRange,
    ) {
        viewModelScope.launch {
            isLoading.value = true
            val filteredTours = FirestoreHelper.getToursByCategoryIdNameStarRatingAndPriceRange(categoryId, inputValue.value, rate, moneyRange)
            tourList.clear()
            tourList.addAll(filteredTours)
            isLoading.value = false
        }
    }

    fun addHistoryItem(historyContent: String) {
        if (!historyItems.contains(historyContent)) {
            historyItems.add(0, historyContent)
        }
    }

    fun deleteHistoryItem(index: Int) {
        if (index in historyItems.indices) {
            historyItems.removeAt(index)
        }
    }

    fun onBackButtonPress(navController: NavController) {
        if (!isSearched.value) {
            moneyPriceRange = null
            starRating = 1
            navController.popBackStack()
        } else {
            isSearched.value = false
            inputValue.value = ""
        }
    }
}
