package com.omar.ai

interface AIProvider {
    suspend fun ask(prompt: String): String
}
