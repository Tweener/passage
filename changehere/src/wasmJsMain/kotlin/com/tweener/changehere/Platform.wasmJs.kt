package com.tweener.changehere

class WasmJsPlatform : Platform {
    override val name: String = "WasmJS"
}

actual fun getPlatform(): Platform = WasmJsPlatform()
