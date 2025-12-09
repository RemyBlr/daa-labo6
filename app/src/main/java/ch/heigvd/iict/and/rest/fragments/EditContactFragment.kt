package ch.heigvd.iict.and.rest.fragments

import androidx.fragment.app.Fragment
import androidx.room.Transaction

class EditContactFragment: Fragment() {

    companion object {
        @JvmStatic
        fun newInstance() = EditContactFragment()
        private val TAG = EditContactFragment::class.java.simpleName
    }
}