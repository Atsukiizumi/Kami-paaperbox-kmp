import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "r") as f:
    text = f.read()

text = text.replace("val title: String,", "val title: String,\n    val type: String? = null,")

text = text.replace("isAi = isAi", "isAi = isAi,\n                isUgoira = illust.type == \"ugoira\"")

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "w") as f:
    f.write(text)

