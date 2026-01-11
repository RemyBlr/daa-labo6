package ch.heigvd.iict.and.rest

import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.first
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.ktor.http.ContentType
import io.ktor.http.contentType


class ContactsRepository(
    private val contactsDao: ContactsDao,
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val UUID = stringPreferencesKey("uuid")
    }
    private val client = HttpClient(CIO)

    val allContacts = contactsDao.getAllContactsLiveData()

    // Create
    suspend fun insertContact(contact: Contact) {
        val uuid = getUuid()

        if(uuid == null)
            throw IllegalStateException("UUID unknown")

        client.post("https://daa.iict.ch/contacts") {
            header("X-UUID", uuid)
            contentType(ContentType.Application.Json)
            setBody(contact)
        } // .body()

        contactsDao.insert(contact)
    }

    // Read
    suspend fun getContactById(id: Long): Contact? {
        return contactsDao.getContactById(id)
    }

    suspend fun getAllContacts(): List<Contact> {
        return contactsDao.getAllContacts()
    }

    // Update
    suspend fun updateContact(contact: Contact) {
        contactsDao.update(contact)
    }

    // Delete
    suspend fun deleteContact(contact: Contact) {
        contactsDao.delete(contact)
    }

    suspend fun clearAllContacts() {
        contactsDao.clearAllContacts()
    }

    suspend fun getUuid(): String? {
        val storedUuid = dataStore.data.first()[Keys.UUID]
        if (storedUuid != null)
            return storedUuid
        return null
    }

    suspend fun createUuid(): String {
        val newUuid: String = client.get("https://daa.iict.ch/enroll").body()
        dataStore.edit { prefs -> prefs[Keys.UUID] = newUuid }
        return newUuid;
    }

    companion object {
        private val TAG = "ContactsRepository"
    }
}