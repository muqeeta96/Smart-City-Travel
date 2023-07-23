package com.example.smartcitytravel.Util;

public class Validation {

    public String validateFullName(String fullName) {
        String nameRegex = "[a-zA-Z\\s]+";
        if (fullName.isEmpty()) {
            return "Error! Enter your name";
        } else if (!fullName.matches(nameRegex)) {
            return "Error! Only alphabets and spaces are acceptable";
        } else {
            return "";
        }
    }

    public String validateEmail(String email) {
        String emailRegex = "^[A-Za-z0-9.]+@[A-Za-z.]+.[A-Za-z]+$";
        if (email.isEmpty()) {
            return "Error! Empty Email";
        } else if (!email.matches(emailRegex)) {
            return "Error! Invalid Email";
        } else {
            return "";
        }
    }

    public String validatePassword(String password) {
        if (password.isEmpty()) {
            return "Error! Enter password";
        } else if (password.contains(" ")) {
            return "Error! Password should not contain spaces";
        } else if (password.matches("[^0-9]+")) {
            return "Error! Password should contain at least one digit";
        } else if (password.matches("[^A-Z]+")) {
            return "Error! Password should contain at least one uppercase letter";
        } else if (password.matches("[^a-z]+")) {
            return "Error! Password should contain at least one lowercase letter";
        } else if (password.length() < 8) {
            return "Error! Password should contain 8 or more characters";
        } else {
            return "";
        }
    }

    public int matchPasswordAndConfirmPassword(String password, String confirmPassword) {
        if (confirmPassword.isEmpty()) {
            return 2;
        } else if (!password.equals(confirmPassword)) {
            return 1;
        } else if (password.equals(confirmPassword)) {
            return 0;
        } else {
            return 3;
        }
    }
}
