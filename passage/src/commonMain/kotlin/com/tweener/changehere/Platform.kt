package com.tweener.passage

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform