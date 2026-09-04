import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "r") as f:
    text = f.read()

# Fix the squashed text
text = text.replace("@Serializable@Serializabledata class MoebooruPost(", "@Serializable\ndata class MoebooruPost(\n")
text = text.replace(")data class DanbooruPost(", ")\n\n@Serializable\ndata class DanbooruPost(\n")

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "w") as f:
    f.write(text)

