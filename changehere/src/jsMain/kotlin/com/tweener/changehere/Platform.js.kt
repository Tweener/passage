package com.tweener.changehere

class JsPlatform : Platform {
    override val name: String = "Js"
}

actual fun getPlatform(): Platform = JsPlatform()
