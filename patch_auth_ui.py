import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/SettingsView.kt", "r") as f:
    text = f.read()

# Make sure the UI button is visible enough
auth_ui_patch = """                Text("Pixiv 授权与登录", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Button(onClick = { showPixivLogin = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("网页登录 Pixiv 自动抓取 Token")
                }
                Spacer(Modifier.height(4.dp))
"""

text = text.replace('                Text("Pixiv 授权与登录", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)\n                Spacer(Modifier.height(4.dp))\n                Button(onClick = { showPixivLogin = true }, modifier = Modifier.fillMaxWidth()) {\n                    Text("网页登录 Pixiv")\n                }\n                Spacer(Modifier.height(4.dp))\n', auth_ui_patch)

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/SettingsView.kt", "w") as f:
    f.write(text)

