package com.dev.nextchapter.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.dev.nextchapter.data.User
import com.dev.nextchapter.data.UserDatabase
import com.dev.nextchapter.utils.HashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = UserDatabase.getDatabase(application).userDao()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    // Function to set the current logged-in user
    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }

    private fun updateUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            userDao.update(user)
            _currentUser.postValue(user)
            println("User updated successfully: $user")
        }
    }


    // Function to handle user login
    fun login(username: String, password: String) = liveData(Dispatchers.IO) {
        val user = userDao.getUserByUsername(username)
        if (user != null) {
            val hashedPassword = HashUtils.hashPassword(password, user.salt)
            // Verify the user if the hashed passwords are equal
            if (hashedPassword == user.password) {
                emit(user)
            } else {
                emit(null)
            }
        } else {
            emit(null)
        }
    }

    fun signUp(username: String, password: String) = liveData(Dispatchers.IO) {
        val existingUser = userDao.getUserByUsername(username)
        if (existingUser == null) {
            val salt = HashUtils.generateSalt() // Generate the salt
            val hashedPassword =
                HashUtils.hashPassword(password, salt) // Hash the password with salt
            val newUser = User(username = username, password = hashedPassword, salt = salt)
            userDao.insert(newUser)
            emit(true)
        } else {
            emit(false)
        }
    }

    fun updatePassword(password: String): Boolean {
        val user = _currentUser.value
        if (user != null) {
            val salt = HashUtils.generateSalt() // Generate the salt
            val hashedPassword =
                HashUtils.hashPassword(password, salt) // Hash the password with salt
            // Verify the user if the hashed passwords are equal
            val updatedUser = user.copy(password = hashedPassword, salt = salt)
            updateUser(updatedUser)

            return true
        }
        return false
    }

    // Function to logout the user
    fun logoutUser() {
        _currentUser.value = null
    }


    // Add to Have Read or Want to Read List
    fun addBookToRead(bookId: String) {
        _currentUser.value?.let { user ->
            val updatedReadBooks = user.readBooks.toMutableList()

            if (!updatedReadBooks.contains(bookId)) {
                println("$bookId is not in the list")
                updatedReadBooks += bookId
            }
            println(updatedReadBooks.toString())
            val updatedUser = user.copy(readBooks = updatedReadBooks)
            updateUser(updatedUser)
        }
    }

    fun addBookToWantToReadList(bookId: String) {
        _currentUser.value?.let { user ->
            val updatedWantToReadList = user.wantToReadList.toMutableList()
            if (!updatedWantToReadList.contains(bookId)) {
                updatedWantToReadList += bookId
            }


            println(updatedWantToReadList.toString())
            val updatedUser = user.copy(wantToReadList = updatedWantToReadList)
            updateUser(updatedUser)
        }
    }

    fun removeBookToRead(bookId: String) {
        _currentUser.value?.let { user ->
            val updatedReadBooks = user.readBooks.toMutableList()

            if (updatedReadBooks.contains(bookId)) {
                updatedReadBooks -= bookId
            }
            val updatedUser = user.copy(readBooks = updatedReadBooks)
            updateUser(updatedUser)
        }
    }

    fun removeBookToWantToReadList(bookId: String) {
        _currentUser.value?.let { user ->
            val updatedWantToReadList = user.wantToReadList.toMutableList()
            if (updatedWantToReadList.contains(bookId)) {
                updatedWantToReadList -= bookId
            }

            val updatedUser = user.copy(wantToReadList = updatedWantToReadList)
            updateUser(updatedUser)
        }
    }

}