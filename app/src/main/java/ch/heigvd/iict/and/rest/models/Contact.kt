package ch.heigvd.iict.and.rest.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
@Entity
data class Contact(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var name: String,
    var firstname: String?,
    var birthday : String?,
    var email: String?,
    var address: String?,
    var zip: String?,
    var city: String?,
    var type: PhoneType?,
    var phoneNumber: String?,

    // Sync fields

    //Contact id on remote server
    var remoteId: String? = null,

    // Indicates if the contact is modified locally and needs to be synced
//    var isModifiedLocally: Boolean = false,

    var dirty: Boolean = true,

    // Indicates if the contact is deleted locally and needs to be deleted on the server
    var isDeletedLocally: Boolean = false
)