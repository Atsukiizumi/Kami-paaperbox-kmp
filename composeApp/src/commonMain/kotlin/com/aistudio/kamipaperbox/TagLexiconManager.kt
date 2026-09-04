package com.aistudio.kamipaperbox

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object TagLexiconManager {
    private val _lexicon = MutableStateFlow<Map<String, String>>(emptyMap())
    val lexicon = _lexicon.asStateFlow()

    fun loadLexiconFromJson(jsonString: String) {
        try {
            val jsonElement = Json.parseToJsonElement(jsonString)
            val map = jsonElement.jsonObject.mapValues { it.value.jsonPrimitive.content }
            _lexicon.value = _lexicon.value + map
        } catch (e: Exception) {
            e.printStackTrace()
            // Ignore format errors
        }
    }

    fun getTranslation(tag: String): String? {
        // Tag format from booru often includes namespaces like artist:name or copyright:series
        val pureTag = tag.substringAfterLast(":") 
        return _lexicon.value[pureTag] ?: _lexicon.value[tag]
    }
}
