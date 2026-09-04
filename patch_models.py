import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/Models.kt", "r") as f:
    text = f.read()

text = text.replace("val isRestricted: Boolean = false", "val isRestricted: Boolean = false,\n    val isUgoira: Boolean = false")

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/Models.kt", "w") as f:
    f.write(text)

