import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/SettingsView.kt", "r") as f:
    text = f.read()

proxy_ui = """        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("全局代理设定 (HTTP/SOCKS)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = prefs.proxyUrl,
                    onValueChange = { SettingsManager.setProxyUrl(it) },
                    placeholder = { Text("例如：http://127.0.0.1:10809") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "为无法直连海外图源 (如 Fanbox、Pixiv、Danbooru) 的网络环境配置代理。更改后系统会热重载底层网络引擎。留空则表示不使用代理直连。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }
        }
        Spacer(Modifier.height(12.dp))

"""

target = """        // 5. 账号与认证"""

text = text.replace(target, proxy_ui + target)

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/SettingsView.kt", "w") as f:
    f.write(text)

