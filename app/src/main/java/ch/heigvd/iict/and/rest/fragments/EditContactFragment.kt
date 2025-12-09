package ch.heigvd.iict.and.rest.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import ch.heigvd.iict.and.rest.ContactsApplication
import ch.heigvd.iict.and.rest.R
import ch.heigvd.iict.and.rest.databinding.FragmentEditContactBinding
import ch.heigvd.iict.and.rest.models.Contact
import ch.heigvd.iict.and.rest.models.PhoneType
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

/**
 * A fragment for editing or creating a contact
 */
class EditContactFragment: Fragment() {

    private lateinit var binding: FragmentEditContactBinding

    private val contactsViewModel: ContactsViewModel by activityViewModels {
        ContactsViewModelFactory(((requireActivity().application as ContactsApplication).repository))
    }

    private var currentContact: Contact? = null
    private var isEditMode = false

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditContactBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    /**
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obersve editedContact to populate fields
        contactsViewModel.editedContact.observe(viewLifecycleOwner) { contact ->
            currentContact = contact
            isEditMode = contact != null && contact.id != null

            // Update title
            binding.editContactTitle.text = if (isEditMode) {
                getString(R.string.fragment_detail_title_edit)
            } else {
                getString(R.string.fragment_detail_title_new)
            }

            // Fill fields if editing
            if (contact != null) {
                populateFields(contact)
            } else {
                clearFields()
            }

            // Show or hide Delete btn
            binding.editContactDeleteBtn.visibility = if (isEditMode) View.VISIBLE else View.GONE
        }

        // Cancel button
        binding.editContactCancelBtn.setOnClickListener { contactsViewModel.cancelEdit() }
        // Save button
        binding.editContactSaveBtn.setOnClickListener { saveContact() }
        // Delete button
        binding.editContactDeleteBtn.setOnClickListener { deleteContact() }
    }

    /**
     * Populate fields with contact data
     * @param contact Contact to populate fields with
     */
    private fun populateFields(contact: Contact) {
        binding.editContactName.setText(contact.name)
        binding.editContactFirstname.setText(contact.firstname ?: "")
        binding.editContactEmail.setText(contact.email ?: "")
        binding.editContactAddress.setText(contact.address ?: "")
        binding.editContactZip.setText(contact.zip ?: "")
        binding.editContactCity.setText(contact.city ?: "")
        binding.editContactPhoneNumber.setText(contact.phoneNumber ?: "")

        // Phone type radio buttons, home by default
        when (contact.type) {
            PhoneType.HOME -> binding.radioBtnHome.isChecked = true
            PhoneType.OFFICE -> binding.radioBtnOffice.isChecked = true
            PhoneType.MOBILE -> binding.radioBtnMobile.isChecked = true
            PhoneType.FAX -> binding.radioBtnFax.isChecked = true
            else -> binding.radioBtnHome.isChecked = true
        }
    }

    /**
     * Clear all input fields
     */
    private fun clearFields() {
        binding.editContactName.setText("")
        binding.editContactFirstname.setText("")
        binding.editContactEmail.setText("")
        binding.editContactAddress.setText("")
        binding.editContactZip.setText("")
        binding.editContactCity.setText("")
        binding.editContactPhoneNumber.setText("")
        binding.radioBtnHome.isChecked = true
    }

    /**
     * Delete the current contact
     */
    private fun deleteContact() {
        currentContact?.let { contact ->
            contactsViewModel.deleteContact(contact)
        }
    }

    /**
     * Save the contact (create or update)
     */
    private fun saveContact() {
        // Get input values
        val name = binding.editContactName.text.toString().trim()
        val firstname = binding.editContactFirstname.text.toString().trim()
        val email = binding.editContactEmail.text.toString().trim()
        val address = binding.editContactAddress.text.toString().trim()
        val zip = binding.editContactZip.text.toString().trim()
        val city = binding.editContactCity.text.toString().trim()
        val phoneNumber = binding.editContactPhoneNumber.text.toString().trim()

        // Name is required
        if (name.isEmpty()) {
            binding.editContactName.error = "Name is required"
            return
        }

        // Phone type
        val phoneType = when (binding.editContactPhoneType.checkedRadioButtonId) {
            R.id.radio_btn_home -> PhoneType.HOME
            R.id.radio_btn_office -> PhoneType.OFFICE
            R.id.radio_btn_mobile -> PhoneType.MOBILE
            R.id.radio_btn_fax -> PhoneType.FAX
            else -> PhoneType.HOME
        }

        // Update or create contact
        val contact = Contact(
            id = currentContact?.id,
            name = name,
            firstname = firstname.ifEmpty { null },
            birthday = currentContact?.birthday,
            email = email.ifEmpty { null },
            address = address.ifEmpty { null },
            zip = zip.ifEmpty { null },
            city = city.ifEmpty { null },
            type = phoneType,
            phoneNumber = phoneNumber.ifEmpty { null }
        )

        if (isEditMode) {
            contactsViewModel.updateContact(contact)
        } else {
            contactsViewModel.createContact(contact)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = EditContactFragment()
        private val TAG = EditContactFragment::class.java.simpleName
    }
}