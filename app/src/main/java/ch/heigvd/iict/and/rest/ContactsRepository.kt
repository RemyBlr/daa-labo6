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

    suspend fun getUuid(): String? {
        val storedUuid = dataStore.data.first()[Keys.UUID]
        if (storedUuid != null)
            return storedUuid
        return null
    }

    // Enroll
    suspend fun createUuid(): String {
        val newUuid: String = client.get("https://daa.iict.ch/enroll").body()
        dataStore.edit { prefs -> prefs[Keys.UUID] = newUuid }
        return newUuid;
    }

    suspend fun getAllContactsFromServer(): List<Contact> {
        val uuid = getUuid() ?: throw IllegalStateException("UUID unknown")

        val allContacts = client.get("https://daa.iict.ch/contacts") {
            header("X-UUID", uuid)
            contentType(ContentType.Application.Json)
        }.body<List<Contact>>()

        for(contact in allContacts)
            contactsDao.insert(contact.copy(dirty = false))

        return contactsDao.getAllContacts()
    }

    suspend fun getContactById(id: Long): Contact? {
        return contactsDao.getContactById(id)
    }

    suspend fun insertContact(contact: Contact) {
        contactsDao.insert(contact)

        val uuid = getUuid() ?: throw IllegalStateException("UUID unknown")

        try {
            val serverContact: Contact = client.post("https://daa.iict.ch/contacts") {
                header("X-UUID", uuid)
                contentType(ContentType.Application.Json)
                setBody(contact.copy(id = null))
            }.body<Contact>()

            contactsDao.update(serverContact.copy(dirty = false))
        } catch (e: Exception) {
            // reste dirty
        }
    }

    suspend fun updateContact(contact: Contact) {
        contactsDao.update(contact)

        val uuid = getUuid() ?: throw IllegalStateException("UUID unknown")

        try {
            val serverContact = client.put("https://daa.iict.ch/contacts/${contact.id}") {
                header("X-UUID", uuid)
                contentType(ContentType.Application.Json)
                setBody(contact)
            }.body<Contact>()

            contactsDao.update(serverContact.copy(dirty = false))
        } catch (e: Exception) {
            // reste dirty
        }
    }

    suspend fun deleteContact(contact: Contact) {
        val uuid = getUuid() ?: throw IllegalStateException("UUID unknown")

        client.delete("https://daa.iict.ch/contacts/${contact.id}") {
            header("X-UUID", uuid)
            contentType(ContentType.Application.Json)
        }

        contactsDao.delete(contact)
    }

    suspend fun clearAllContacts() {
        contactsDao.clearAllContacts()
    }

    suspend fun syncDirtyRead() {
        val dirtyContacts = contactsDao.getDirtyContacts()

        for (contact in dirtyContacts) {
            try {
                when {
                    contact.id == null -> { insertContact(contact) }
                    contact.isDeletedLocally -> { deleteContact(contact) }
                    else -> { updateContact(contact) }
                }
            } catch (e: Exception) {
                // best-effort : on laisse dirty
            }
        }
    }

    companion object {
        private val TAG = "ContactsRepository"
    }
}