package com.example.testprojectmusicplayer.utils

object Validator {

    // Function to validate email format and provide feedback
    fun emailValidator(email: String): Pair<Boolean, String> {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"

        return if (email.isEmpty()) {
            Pair(false, "Email cannot be empty.")
        } else if (!email.matches(emailRegex.toRegex())) {
            Pair(false, "Invalid email format. Please enter a valid email address.")
        } else {
            Pair(true, "Valid email.")
        }
    }

    // Function to validate password criteria and provide feedback
    fun passwordValidator(password: String): Pair<Boolean, String> {
        val passwordRegex = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$"

        return if (password.isEmpty()) {
            Pair(false, "Password cannot be empty.")
        } else if (password.length < 6) {
            Pair(false, "Password should be at least 6 characters long.")
        } else if (!password.matches(passwordRegex.toRegex())) {
            Pair(false, "Password must contain at least one letter and one number.")
        } else {
            Pair(true, "Valid password.")
        }
    }
}
