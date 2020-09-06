package de.huepattl.phexpedition

enum class MessageType {
    Information, Error
}

data class UiMessage(val type: MessageType, val title: String, val text: String)

