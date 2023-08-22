package com.example.tabit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tabit.compose.AddScreen
import com.example.tabit.compose.DetailScreen
import com.example.tabit.compose.DrawerScreen
import com.example.tabit.compose.HomeScreen
import com.example.tabit.compose.LoginScreen
import com.example.tabit.compose.TrackerScreen
import com.example.tabit.compose.UsersScreen
import com.example.tabit.ui.theme.TabitTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    companion object {
        const val RC_SIGN_IN = 100
    }

    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // firebase auth instance
        val db = Firebase.firestore
        mAuth = FirebaseAuth.getInstance()

        // configure Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // default_web_client_id 에러 시 rebuild
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // UsersScreen 의 viewModel 매개변수 주기 위해

        setContent {
            TabitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val user: FirebaseUser? = mAuth.currentUser
                    val startDestination = remember {
                        if (user == null) {
                            "login"
                        } else {
                            "home"
                        }
                    }
                    val signInIntent = googleSignInClient.signInIntent
                    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.StartActivityForResult()) { result ->
                        val data = result.data
                        // result returned from launching the intent from GoogleSignInApi.getSignInIntent()
                        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                        val exception = task.exception
                        if (task.isSuccessful) {
                            try {
                                // Google SignIn was successful, authenticate with firebase
                                val account = task.getResult(ApiException::class.java)!!
                                firebaseAuthWithGoogle(account.idToken!!)
                                navController.navigate("home")
                            } catch (e: Exception) {
                                // Google SignIn failed
                                Log.d("SignIn", "로그인 실패")
                            }
                        } else {
                            Log.d("SignIn", exception.toString())
                        }
                    }
                    NavHost(navController = navController, startDestination = startDestination) {
                        composable(Screen.Login.route) {
                            LoginScreen() {
                                launcher.launch(signInIntent)
                            }
                        }
                        composable(Screen.Home.route) { HomeScreen(navController) }
                        composable(Screen.Add.route) { AddScreen(navController) }
                        composable(
                            Screen.Detail.route,
                            arguments = listOf(navArgument("imageUrl") {
                                type = NavType.StringType
                            })
                        )
                        { backStackEntry ->
                            DetailScreen(
                                navController = navController,
                                imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
                            )
                        }
                        composable(Screen.Tracker.route) { TrackerScreen(navController) }
                        composable(Screen.Drawer.route) {
                            DrawerScreen(
                                navController,
                                onSignOutClicked = { signOut(navController) })
                        }
                        composable(Screen.Users.route) { UsersScreen(navController) }
                    }
                    if (mAuth.currentUser == null) {
                        LoginScreen() {
                            signIn()
                        }
                    } else {
//                        val user: FirebaseUser = mAuth.currentUser!!
                        navController.navigate("home")
                    }
                }
            }
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // result returned from launching the intent from GoogleSignInApi.getSignInIntent()
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val exception = task.exception
            if (task.isSuccessful) {
                try {
                    // Google SignIn was successful, authenticate with firebase
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: Exception) {
                    // Google SignIn failed
                    Log.d("SignIn", "로그인 실패")
                }
            } else {
                Log.d("SignIn", exception.toString())
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // SignIn Successful
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()
                } else {
                    // SignIn Failed
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOut(navController: NavController) {
        // get the google account
        val googleSignInClient: GoogleSignInClient

        // configure Google SignIn
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Sign Out of all accounts
        mAuth.signOut()
        googleSignInClient.signOut().addOnSuccessListener {
            Toast.makeText(this, "로그아웃 성공", Toast.LENGTH_SHORT).show()
            navController.navigate("login")
        }.addOnFailureListener {
            Toast.makeText(this, "로그아웃 실패", Toast.LENGTH_SHORT).show()
        }
    }


    // main화면에서 뒤로가기 눌렀을 때 빈 login화면으로 옮겨가는 것 떄문에 찾아본 내용. 필요하면 사용해보기
//    override fun onBackPressed() {
//        val navController = findNavController(R.id.nav_host)
//        if (navController.currentBackStackEntry?.destination?.route == "main") {
//            // main 화면에서 뒤로가기를 눌렀을 때 처리
//            // 여기에 아무런 처리를 하지 않으면 뒤로가기 동작이 무시됨
//            // 뒤로가기 동작을 원하는 대로 커스텀하게 처리할 수 있음
//            // 예를 들어, 앱 종료 시에 확인 대화상자를 띄우거나 다른 동작을 수행하려면 여기에 코드를 추가
//        } else {
//            super.onBackPressed()
//        }
//    }
}
