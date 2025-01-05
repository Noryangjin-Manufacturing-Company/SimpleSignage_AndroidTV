package com.noryangjin.simplesignage

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Photo : Screen("photo")
    object Calendar : Screen("calendar")
}