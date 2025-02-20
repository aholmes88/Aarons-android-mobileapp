package com.dev.nextchapter.ui.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import coil.transform.RoundedCornersTransformation
import com.dev.nextchapter.data.Book
import com.dev.nextchapter.viewmodel.UserViewModel
import com.dev.nextchapter.data.googleService

@Composable
fun UserProfileScreen(
    userViewModel: UserViewModel = viewModel(),
    navController: NavController
) {

    val currentUser by userViewModel.currentUser.observeAsState()
    var showDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loadingBooks by remember { mutableStateOf(true) }
    val readList = remember { mutableStateListOf<Book>() }

    val context = LocalContext.current

    LaunchedEffect(key1 = currentUser?.readBooks, key2 = currentUser?.wantToReadList) {
        currentUser?.readBooks?.forEach { id ->
            val book = googleService.getBookDetails(id)
            readList.add(book)
        }
        loadingBooks = false
    }

    Column(
        modifier = Modifier
            .background(Color(0xFFE1DCC5))
            .padding(top = 36.dp, start = 16.dp, end = 16.dp)
            .fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween

        ) {
            Button(onClick = {
                navController.navigate("home")
            }) {
                Text("Go Back")
            }

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .clickable { showMenu = true },
                verticalAlignment = Alignment.Bottom
            ) {
                currentUser.let { user ->
                    Icon(
                        modifier = Modifier.size(30.dp),
                        imageVector = Icons.Default.Person,
                        tint = Color.DarkGray,
                        contentDescription = "user-icon"
                    )
                    Text(
                        text = user?.username ?: "",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Log out") },
                        onClick = {
                            showMenu = false
                            userViewModel.logoutUser()
                            navController.navigate("login")
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("My Account") },
                        onClick = {
                            showMenu = false
                            navController.navigate("profile")
                        }
                    )
                }
            }

        }

        Text("Welcome, ${userViewModel.currentUser.value?.username}")

        Text(
            modifier = Modifier.clickable { showDialog = true },
            text = "Change your password?",
            textDecoration = TextDecoration.Underline
        )

        if (showDialog) {
            AlertDialog(onDismissRequest = { showDialog = false }, confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = {

                        if (password == confirmPassword) {
                            showDialog = false
                            if (userViewModel.updatePassword(password)) {
                                Toast.makeText(
                                    context,
                                    "User password updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            password = ""
                            confirmPassword = ""
                        }
                    }) { Text("Update Password") }
                    Button(onClick = {
                        showDialog = false
                        password = ""
                        confirmPassword = ""
                    }) { Text("Cancel") }
                }
            }, title = {
                Text("Enter new password")
            }, text = {
                Column {
                    OutlinedTextField(
                        value = password,
                        placeholder = { Text("Enter Password") },
                        onValueChange = { password = it },
                        visualTransformation = PasswordVisualTransformation()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        placeholder = { Text("Confirm Password") },
                        onValueChange = { confirmPassword = it },
                        visualTransformation = PasswordVisualTransformation()
                    )
                }
            })
        }

        Spacer(modifier = Modifier.height(64.dp))

        Text(
            "Read List",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.fillMaxWidth()
        )

        when {
            loadingBooks -> {
                CircularProgressIndicator()
            }

            !loadingBooks ->
                BookList(readList)
        }

    }
}

@Composable
fun BookList(books: List<Book>) {
    var showCurrentPlaylist by remember { mutableStateOf(false) }

    Column(modifier = Modifier.clickable {
        showCurrentPlaylist = true
    }, horizontalAlignment = Alignment.CenterHorizontally) {
        if(showCurrentPlaylist){
            AlertDialog(onDismissRequest = { showCurrentPlaylist = false }, confirmButton = {

            }, title = {
                Text("BookList Title")
            }, text = {
                LazyColumn {
                    items(books) {
                        Text(it.volumeInfo.title)
                    }
                }
            })
        }

        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(books[0].volumeInfo.imageLinks?.thumbnail)
                    .size(Size.ORIGINAL) // Fetch the original size
                    .crossfade(true)
                    .transformations(RoundedCornersTransformation(8f))
                    .build(),
            ),
            contentDescription = books[0].volumeInfo.title,
            modifier = Modifier
                .size(150.dp)
                .fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Text("Liked Books ${books.size}")
    }

}