package com.example.fooedtra

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.example.fooedtra.Model.tokenModel
import com.example.fooedtra.Model.tokenPreferences
import com.example.fooedtra.SubmitImageFood.Companion.token
import com.example.fooedtra.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mainModel: MainModel

    private companion object{
        private const val RC_SIGN_IN = 100
        private const val TAG = "GOOGLE_SIGNIN_IN_TAG"
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //untuk google sign in
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        //untuk init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //untuk button google
        binding.googleBtn.setOnClickListener{
            Log.d(TAG, "onClick: google button clicked")
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
        setupViewModel()
    }

    private fun checkUser() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            startActivity(Intent(this, Dashboard::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == RC_SIGN_IN){
            Log.d(TAG, "onActivityResult: google sign in request code")
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val googleAccount = account.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(googleAccount!!)
            }
            catch (e: Exception){
                Log.d(TAG, "onActivityResult: ${e.message}")
            }
        }
    }
    private fun setupViewModel() {
        mainModel = ViewModelProvider(
            this,
            ViewModelFactory(tokenPreferences.getInstance(dataStore))
        )[MainModel::class.java]
    }


    private fun firebaseAuthWithGoogle(googleAccount: GoogleSignInAccount?) {
        Log.d(TAG, "firebaseAuthWithGoogle: begin firebase auth with google")
        val credential = GoogleAuthProvider.getCredential(googleAccount!!.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnSuccessListener {authResult ->
            Log.d(TAG, "firebaseAuthWithGoogle: Login success")
            val firebaseUser = firebaseAuth.currentUser
            val uid = firebaseUser!!.uid
            val email = firebaseUser!!.email
            val name = firebaseUser!!.displayName
//            val idtoken = firebaseUser!!.getIdToken(true)

            firebaseUser!!.getIdToken(true).addOnCompleteListener {task ->
                if(task.isSuccessful){
                    val idtoken = task.result!!.token
                    token = idtoken.toString()
                    Log.d(TAG, "firebaseAuthWithGoogle: idtoken: $token")
                }
                else{
                    task.exception
                }
            }

            Log.d(TAG, "firebaseAuthWithGoogle: Uid: $uid")
            Log.d(TAG, "firebaseAuthWiknthGoogle: Email: $email")
            Log.d(TAG, "firebaseAuthWithGoogle: Name: $name")
//            Log.d(TAG, "firebaseAuthWithGoogle: token: $idtoken")

            mainModel.saveUser(tokenModel(token))

            if (authResult.additionalUserInfo!!.isNewUser){
                Log.d(TAG, "firebaseAuthWithGoogle: Account created... \n$email, name: $name")
                Toast.makeText(this@MainActivity, "Account create", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "firebaseAuthWithGoogle: Account already exist... \n$email, name: $name")
                Toast.makeText(this@MainActivity, "Account already exist", Toast.LENGTH_SHORT).show()
            }

        //pindah page ke dashboard
            intent.putExtra(EXTRA_TOKEN, token)
            startActivity(Intent(this@MainActivity, Dashboard::class.java))
            finish()
        }
        .addOnFailureListener { e ->
            Log.d(TAG, "firebaseAuthWithGoogle: Login failed \n${e.message}")
            Toast.makeText(this@MainActivity, "Login failed \n" + "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}