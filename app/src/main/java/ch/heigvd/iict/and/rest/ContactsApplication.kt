package ch.heigvd.iict.and.rest

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import ch.heigvd.iict.and.rest.database.ContactsDatabase
import android.content.Context

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "uuid")

class ContactsApplication : Application() {

    private val database by lazy { ContactsDatabase.getDatabase(this) }
    val repository by lazy { ContactsRepository(database.contactsDao(), applicationContext.dataStore) }
}