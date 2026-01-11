/**
 * ContactsDatabase.kt
 *
 * Base de données Room stockant les contacts. Utilise le pattern Singleton
 * pour garantir une instance unique. Configurée avec fallbackToDestructiveMigration
 * pour simplifier les changements de schéma pendant le développement.
 *
 * @authors Bleuer Rémy, Changanaqui Yoann, Rajadurai Thirusan
 * @date 11.01.2026
 */
package ch.heigvd.iict.and.rest.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import ch.heigvd.iict.and.rest.database.converters.CalendarConverter
import ch.heigvd.iict.and.rest.models.Contact

@Database(entities = [Contact::class], version = 1, exportSchema = true)
@TypeConverters(CalendarConverter::class)
abstract class ContactsDatabase : RoomDatabase() {

    abstract fun contactsDao() : ContactsDao

    companion object {

        @Volatile
        private var INSTANCE : ContactsDatabase? = null

        fun getDatabase(context: Context) : ContactsDatabase {

            return INSTANCE ?: synchronized(this) {
                val _instance = Room.databaseBuilder(context.applicationContext,
                ContactsDatabase::class.java, "contacts.db")
                    .fallbackToDestructiveMigration(true)
                    .build()

                INSTANCE = _instance
                _instance
            }
        }

    }

}