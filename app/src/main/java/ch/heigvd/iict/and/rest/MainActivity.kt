package ch.heigvd.iict.and.rest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ch.heigvd.iict.and.rest.databinding.ActivityMainBinding
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModel
import ch.heigvd.iict.and.rest.viewmodels.ContactsViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private val contactsViewModel: ContactsViewModel by viewModels {
        ContactsViewModelFactory((application as ContactsApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // depuis android 15 (sdk 35), le mode edge2edge doit être activé
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // comme edge2edge est activé, l'application doit garder un espace suffisant pour la barre système
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // la barre d'action doit être définie dans le layout, on la lie à l'activité
        setSupportActionBar(binding.toolbar)

        // contenu
        binding.mainFabNew.setOnClickListener {
            // FIXME - create a new contact
            Toast.makeText(this, "TODO - Création d'un nouveau contact", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.menu_main_synchronize -> {
                contactsViewModel.refresh()
                true
            }
            R.id.menu_main_populate -> {
                contactsViewModel.enroll()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

}