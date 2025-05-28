package no.uio.ifi.in2000.simonng.simonng.team1.ui.vaer

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class FellesVaerViewModelFactory(
    private val applikasjon: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FellesVaerViewModel::class.java)) {
            return FellesVaerViewModel(applikasjon) as T
        }
        throw IllegalArgumentException("Ukjent ViewModel-klasse")
    }
}