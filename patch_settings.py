import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/SettingsView.kt", "r") as f:
    text = f.read()

# Add Dialog imports
if "import androidx.compose.ui.window.Dialog" not in text:
    text = text.replace("import androidx.compose.ui.unit.sp", "import androidx.compose.ui.unit.sp\nimport androidx.compose.ui.window.Dialog\nimport kotlinx.coroutines.launch")

# Insert states
state_insert = """
    val prefs by SettingsManager.prefs.collectAsState()
    var showPixivLogin by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
"""
text = re.sub(r'val prefs by SettingsManager\.prefs\.collectAsState\(\)', state_insert, text)

# Insert WebView Dialog at the bottom of Column
dialog_insert = """
        Spacer(Modifier.height(28.dp))
        // 7. 架构与致谢说明

        if (showPixivLogin) {
            Dialog(onDismissRequest = { showPixivLogin = false }) {
                Card(modifier = Modifier.fillMaxWidth().height(500.dp)) {
                    PixivLoginWebView(
                        onAuthCodeReceived = { code, verifier ->
                            coroutineScope.launch {
                                val success = PixivAuthManager.loginWithCode(code, verifier)
                                if (success) {
                                    showPixivLogin = false
                                }
                            }
                        },
                        onCookieReceived = { cookie ->
                            SettingsManager.setPixivCookie(cookie)
                        }
                    )
                }
            }
        }
"""
text = text.replace("        Spacer(Modifier.height(28.dp))\n        // 7. 架构与致谢说明", dialog_insert)

# Modify Pixiv Account UI
auth_ui = """                Text("Pixiv 授权与登录", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Button(onClick = { showPixivLogin = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("网页登录 Pixiv")
                }
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = prefs.pixivRefreshToken,
                    onValueChange = { SettingsManager.setPixivTokens(prefs.pixivAccessToken, it) },
                    placeholder = { Text("或手动输入 Refresh Token...") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                if (prefs.pixivCookie.isNotBlank()) {
                    Text("已成功抓取登录 Cookie！", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "通过内嵌网页安全登录 Pixiv，系统将自动抓取 Cookie 和 Access Token 以访问官方接口。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
"""

text = re.sub(r'Text\("Pixiv Refresh Token", fontWeight = FontWeight.SemiBold, fontSize = 14\.sp\).*?lineHeight = 18\.sp\s+\)', auth_ui, text, flags=re.DOTALL)

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/SettingsView.kt", "w") as f:
    f.write(text)

