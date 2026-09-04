import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "r") as f:
    text = f.read()

text = text.replace("tags = tagList,", "tags = tagList,\n                translatedTags = tagList.associateWith { TagLexiconManager.getTranslation(it) ?: \"\" }.filterValues { it.isNotBlank() },")

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "w") as f:
    f.write(text)

