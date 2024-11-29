package com.tweener.changehere

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform