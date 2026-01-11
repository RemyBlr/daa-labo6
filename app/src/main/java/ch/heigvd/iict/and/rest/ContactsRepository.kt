/**
 * ContactsRepository.kt
 *
 * Repository gérant les opérations CRUD sur les contacts avec synchronisation
 * vers un serveur REST. Implémente une politique "best-effort" : les opérations
 * sont d'abord appliquées localement puis synchronisées avec le serveur.
 * En cas d'échec réseau, les contacts restent marqués "dirty" pour une
 * synchronisation ultérieure.
 *
 * @authors Bleuer Rémy, Changanaqui Yoann, Rajadurai Thirusan
 * @date 11.01.2026
 */
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
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


class ContactsRepository(
    private val contactsDao: ContactsDao,
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val UUID = stringPreferencesKey("uuid")
    }
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }
    }

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
        return newUuid
    }

    suspend fun getAllContactsFromServer(): List<Contact> {
        val uuid = getUuid() ?: throw IllegalStateException("UUID unknown")

        val allContacts = client.get("https://daa.iict.ch/contacts") {
            header("X-UUID", uuid)
            contentType(ContentType.Application.Json)
        }.body<List<Contact>>()

        for(contact in allContacts) {
            // L'ID du serveur devient remoteId, et on laisse Room générer un id local
            contactsDao.insert(contact.copy(
                id = null,
                remoteId = contact.id.toString(),
                dirty = false
            ))
        }

        return contactsDao.getAllContacts()
    }

    suspend fun getContactById(id: Long): Contact? {
        return contactsDao.getContactById(id)
    }

    suspend fun insertContact(contact: Contact) {
        // Insérer localement et récupérer l'ID local généré
        val localId = contactsDao.insert(contact)

        val uuid = getUuid() ?: throw IllegalStateException("UUID unknown")

        try {
            val serverContact: Contact = client.post("https://daa.iict.ch/contacts") {
                header("X-UUID", uuid)
                contentType(ContentType.Application.Json)
                setBody(contact.copy(id = null, remoteId = null))
            }.body<Contact>()

            // Mettre à jour avec le remoteId du serveur, en gardant l'ID local
            contactsDao.update(contact.copy(
                id = localId,
                remoteId = serverContact.id.toString(),
                dirty = false
            ))
        } catch (e: Exception) {
            // reste dirty
        }
    }

    suspend fun updateContact(contact: Contact) {
        // Marquer comme dirty et sauvegarder localement
        contactsDao.update(contact.copy(dirty = true))

        val uuid = getUuid() ?: throw IllegalStateException("UUID unknown")

        // Ne synchroniser que si le contact a un remoteId (existe sur le serveur)
        if (contact.remoteId == null) return

        try {
            val serverContact = client.put("https://daa.iict.ch/contacts/${contact.remoteId}") {
                header("X-UUID", uuid)
                contentType(ContentType.Application.Json)
                // Envoyer avec l'id serveur
                setBody(contact.copy(id = contact.remoteId?.toLongOrNull()))
            }.body<Contact>()

            // Mettre à jour localement avec dirty = false
            contactsDao.update(contact.copy(dirty = false))
        } catch (e: Exception) {
            // reste dirty
        }
    }

    suspend fun deleteContact(contact: Contact) {
        val uuid = getUuid() ?: throw IllegalStateException("UUID unknown")

        // Marquer comme supprimé localement (best-effort)
        contactsDao.update(contact.copy(isDeletedLocally = true, dirty = true))

        // Si le contact n'existe pas sur le serveur, supprimer directement
        if (contact.remoteId == null) {
            contactsDao.delete(contact)
            return
        }

        try {
            client.delete("https://daa.iict.ch/contacts/${contact.remoteId}") {
                header("X-UUID", uuid)
            }
            // Succès: supprimer de la DB locale
            contactsDao.delete(contact)
        } catch (e: Exception) {
            // reste dirty avec isDeletedLocally = true, sera supprimé lors de la prochaine sync
        }
    }

    suspend fun clearAllContactsLocally() {
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