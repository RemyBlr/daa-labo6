package ch.heigvd.iict.and.rest

import ch.heigvd.iict.and.rest.database.ContactsDao
import ch.heigvd.iict.and.rest.models.Contact

class ContactsRepository(private val contactsDao: ContactsDao) {

    val allContacts = contactsDao.getAllContactsLiveData()

    // Create
    suspend fun insertContact(contact: Contact) {
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

    companion object {
        private val TAG = "ContactsRepository"
    }

}