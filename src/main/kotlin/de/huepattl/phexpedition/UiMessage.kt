package de.huepattl.phexpedition

enum class MessageType {
    Information, Error
}

data class UiMessage(
        val type: MessageType = MessageType.Information,
        val title: String,
        val text: String) {

    /**
     * Semantic CSS class.
     */
    fun cssHint1(): String {
        if (type == MessageType.Error) {
            return "negative icon"
        } else {
            return "positive icon"
        }
    }

    /**
     * Semantic CSS class.
     */
    fun cssHint2(): String {
        if (type == MessageType.Error) {
            return "exclamation triangle"
        } else {
            return "check circle"
        }
    }

}
