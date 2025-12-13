package com.dwlhm.browser.ui

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dwlhm.domain.browser.LastVisitedRepository
import com.dwlhm.webview.Tab
import com.dwlhm.webview.TabManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrowserUiState(
    val currentUrl: String = "",
    val inputUrl: String = "",
    val isLoading: Boolean = false,
    val progress: Float = 0.2f,
    val pageTitle: String = "",
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val isUrlBarFocused: Boolean = false,
    val themeColor: Color? = null,
    // Tab related state
    val showTabList: Boolean = false,
    val tabCount: Int = 1
)

@HiltViewModel
class BrowserViewModel @Inject constructor(
    private val lastVisitedRepository: LastVisitedRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BrowserUiState())
    val uiState: StateFlow<BrowserUiState> = _uiState.asStateFlow()
    
    // TabManager instance
    val tabManager = TabManager()
    
    fun updateInputUrl(url: String) {
        _uiState.update { it.copy(inputUrl = url) }
    }
    
    fun updateCurrentUrl(url: String) {
        _uiState.update { it.copy(currentUrl = url, inputUrl = url) }
    }
    
    fun updateLoadingState(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }
    
    fun updateProgress(progress: Float) {
        _uiState.update { it.copy(progress = progress) }
    }
    
    fun updatePageTitle(title: String) {
        _uiState.update { it.copy(pageTitle = title) }
    }
    
    fun updateNavigationState(canGoBack: Boolean, canGoForward: Boolean) {
        _uiState.update { 
            it.copy(canGoBack = canGoBack, canGoForward = canGoForward) 
        }
    }
    
    fun setUrlBarFocused(focused: Boolean) {
        _uiState.update { it.copy(isUrlBarFocused = focused) }
    }
    
    fun updateThemeColor(color: Color?) {
        _uiState.update { it.copy(themeColor = color) }
    }
    
    fun submitUrl(url: String): String {
        val trimmedUrl = url.trim()
        val formattedUrl = when {
            trimmedUrl.startsWith("http://") || trimmedUrl.startsWith("https://") -> trimmedUrl
            trimmedUrl.contains(".") && !trimmedUrl.contains(" ") -> "https://$trimmedUrl"
            else -> "https://www.google.com/search?q=${trimmedUrl.replace(" ", "+")}"
        }
        _uiState.update { it.copy(inputUrl = formattedUrl, isUrlBarFocused = false) }
        return formattedUrl
    }
    
    /**
     * Called when page finishes loading to save last visited URL and title
     */
    fun onPageFinished(url: String, title: String) {
        _uiState.update { 
            it.copy(
                currentUrl = url, 
                inputUrl = url, 
                pageTitle = title,
                isLoading = false
            ) 
        }
        saveLastVisited(url, title)
    }
    
    /**
     * Save URL and title to preferences in background
     */
    private fun saveLastVisited(url: String, title: String) {
        viewModelScope.launch {
            lastVisitedRepository.saveLastVisited(url, title)
        }
    }
    
    // ==================== Tab Management ====================
    
    /**
     * Initialize tabs with initial URL
     */
    fun initializeTabs(initialUrl: String) {
        if (tabManager.tabCount == 0) {
            tabManager.createTab(initialUrl)
            updateTabCount()
        }
    }
    
    /**
     * Show/hide tab list overlay
     */
    fun setShowTabList(show: Boolean) {
        _uiState.update { it.copy(showTabList = show) }
    }
    
    /**
     * Toggle tab list visibility
     */
    fun toggleTabList() {
        _uiState.update { it.copy(showTabList = !it.showTabList) }
    }
    
    /**
     * Create a new tab
     */
    fun createNewTab(url: String = ""): Tab {
        val tab = tabManager.createTab(url)
        updateTabCount()
        _uiState.update { it.copy(showTabList = false) }
        return tab
    }
    
    /**
     * Switch to a specific tab
     */
    fun switchToTab(tabId: String) {
        tabManager.switchToTab(tabId)
        _uiState.update { it.copy(showTabList = false) }
        
        // Update UI state with the new active tab's info
        tabManager.activeTab?.let { tab ->
            _uiState.update { 
                it.copy(
                    currentUrl = tab.url,
                    inputUrl = tab.url,
                    pageTitle = tab.title,
                    themeColor = tab.themeColor
                )
            }
        }
    }
    
    /**
     * Close a specific tab
     */
    fun closeTab(tabId: String) {
        tabManager.closeTab(tabId)
        updateTabCount()
        
        // Update UI state with the new active tab's info
        tabManager.activeTab?.let { tab ->
            _uiState.update { 
                it.copy(
                    currentUrl = tab.url,
                    inputUrl = tab.url,
                    pageTitle = tab.title,
                    themeColor = tab.themeColor
                )
            }
        }
    }
    
    /**
     * Update the active tab's information
     */
    fun updateActiveTabInfo(url: String, title: String, themeColor: Color?) {
        tabManager.updateActiveTab { tab ->
            tab.copy(
                url = url,
                title = title.ifEmpty { tab.title },
                themeColor = themeColor
            )
        }
    }
    
    /**
     * Update tab count in UI state
     */
    private fun updateTabCount() {
        _uiState.update { it.copy(tabCount = tabManager.tabCount) }
    }
}
