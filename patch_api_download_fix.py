import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "r") as f:
    text = f.read()

text = text.replace("response.io.ktor.client.call.body<ByteArray>()", "response.body<ByteArray>()")

if "import io.ktor.client.call.body" not in text:
    text = text.replace("import io.ktor.client.request.parameter", "import io.ktor.client.request.parameter\nimport io.ktor.client.call.body")

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "w") as f:
    f.write(text)

