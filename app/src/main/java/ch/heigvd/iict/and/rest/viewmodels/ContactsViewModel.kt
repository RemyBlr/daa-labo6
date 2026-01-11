/**
 * ContactsViewModel.kt
 *
 * ViewModel gérant la logique métier des contacts. Expose les données via LiveData
 * et fournit les actions d'enrollment, de synchronisation et d'édition des contacts.
 * Utilise les coroutines pour les opérations asynchrones.
 *
 * @authors Bleuer Rémy, Changanaqui Yoann, Rajadurai Thirusan
 * @date 11.01.2026
 */
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
    private val _editedContact = MutableLiveData<Contact?>(null)
    val editedContact: MutableLiveData<Contact?> get() = _editedContact

    init {
        viewModelScope.launch {
            repository.getUuid()
        }
    }

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

    fun updateContact(contact: Contact) {
        // Dispatcher.IO for database operations
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateContact(contact)
            // Back to list
            _editedContact.postValue(null)
        }
    }

    fun deleteContact(contact: Contact) {
        // Dispatcher.IO for database operations
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteContact(contact)
            // Back to list
            _editedContact.postValue(null)
        }
    }

    // actions
    fun enroll() {
        viewModelScope.launch(Dispatchers.IO) {
            // delete all local db as labo required
            repository.clearAllContactsLocally()

            repository.createUuid();

            // sync all server contacts in local db
            repository.getAllContactsFromServer()
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncDirtyRead()
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