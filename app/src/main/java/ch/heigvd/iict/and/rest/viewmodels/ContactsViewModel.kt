package ch.heigvd.iict.and.rest.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.heigvd.iict.and.rest.ContactsRepository
import ch.heigvd.iict.and.rest.models.Contact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ContactsViewModel(private val repository: ContactsRepository) : ViewModel() {

    val allContacts = repository.allContacts

    // LiveDAta to track the contact being edited or created
    private val _editedContact = MutableLiveData<Contact?>()
    val editedContact: MutableLiveData<Contact?> get() = _editedContact

    // Actions to start editing or creating contact
    fun startEditContact(contact: Contact) {
        _editedContact.postValue(contact)
    }

    fun startCreateContact() {
        _editedContact.postValue(
            Contact(
                id = null,
                name = "",
                firstname = null,
                birthday = null,
                email = null,
                address = null,
                zip = null,
                city = null,
                type = null,
                phoneNumber = null
            )
        )
    }

    fun cancelEdit() {
        _editedContact.postValue(null)
    }

    fun createContact(contact: Contact) {
        // Dispatcher.IO for database operations
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertContact(contact)
            // Back to list
            _editedContact.postValue(null)
        }
    }

    // actions
    fun enroll() {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            // TODO
        }
    }

}

class ContactsViewModelFactory(private val repository: ContactsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}