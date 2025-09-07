package com.bilalazzam.mycontacts

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform