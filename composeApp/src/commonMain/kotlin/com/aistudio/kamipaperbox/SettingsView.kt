package com.aistudio.kamipaperbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsView(
    isCompact: Boolean
) {
    val prefs by SettingsManager.prefs.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(if (isCompact) 16.dp else 24.dp)
    ) {
        Text(
            "纸谱与首选项",
            style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "定制您的和风画卷体验与全局过滤规则",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(20.dp))

        // 1. 和风纸色主题
        Text("和风纸色主题", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ThemePreset.entries.forEach { preset ->
                val isSelected = prefs.themePreset == preset
                ElevatedCard(
                    onClick = { SettingsManager.setThemePreset(preset) },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(if (isCompact) 10.dp else 14.dp)) {
                        Text(
                            text = when (preset) {
                                ThemePreset.WASHI -> "和纸"
                                ThemePreset.AOSUMI -> "青墨"
                                ThemePreset.SHUSHA -> "朱砂"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isCompact) 13.sp else 15.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = when (preset) {
                                ThemePreset.WASHI -> "Washi"
                                ThemePreset.AOSUMI -> "Aosumi"
                                ThemePreset.SHUSHA -> "Shusha"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (isSelected) "当前生效" else "点击切换",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 10.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(20.dp))

        // 2. 内容分级与 R18 控制
        Text("内容分级与安全", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // R18 全局开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("包含 R18 / 限制级内容", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            "Pixiv / Fanbox 将通过远端 API 请求完整限制级内容；Booru 站点放开分级限制。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )
                    }
                    Switch(
                        checked = prefs.includeR18,
                        onCheckedChange = { SettingsManager.setIncludeR18(it) }
                    )
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(Modifier.height(14.dp))

                // 高斯模糊开关
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("瀑布流敏感图像高斯模糊", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(
                            "在画卷中对敏感作品应用高斯模糊遮罩；点击大图灯箱时正常显示原图。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 18.sp
                        )
                    }
                    Switch(
                        checked = prefs.blurNsfw,
                        onCheckedChange = { SettingsManager.setBlurNsfw(it) }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(20.dp))

        // 3. AI 生成作品过滤模式
        Text("AI 生成作品管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "针对画卷中标记为 AI 生成的作品的处理规则",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AiFilterMode.entries.forEach { mode ->
                val isSelected = prefs.aiFilterMode == mode
                OutlinedCard(
                    onClick = { SettingsManager.setAiFilterMode(mode) },
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            mode.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(20.dp))

        // 4. 网络加速与镜像
        Text("网络与图源加速", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("启用 i.pixiv.re 反盗链高速镜像", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Text(
                        "自动将 Pixiv 官方图片服务器重定向至反盗链镜像节点，彻底解决 403 错误与加载超时。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        lineHeight = 18.sp
                    )
                }
                Switch(
                    checked = prefs.usePixivMirror,
                    onCheckedChange = { SettingsManager.setUsePixivMirror(it) }
                )
            }
        }

        // 5. 账号与认证
        Text("账号与认证", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Pixiv Refresh Token", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = prefs.pixivRefreshToken,
                    onValueChange = { SettingsManager.setPixivTokens(prefs.pixivAccessToken, it) },
                    placeholder = { Text("输入您的 Refresh Token...") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "为访问官方接口，需输入您的 Pixiv 刷新令牌。目前已支持自动利用令牌换取 Access Token。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
                
                Spacer(Modifier.height(14.dp))
                Text("SauceNAO API Key (以图搜图)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = prefs.sauceNaoApiKey,
                    onValueChange = { SettingsManager.setSauceNaoApiKey(it) },
                    placeholder = { Text("输入您的 SauceNAO API Key (可选)") },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "用于突破查询频率限制，可以在 saucenao.com 获取免费 Key。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        Spacer(Modifier.height(20.dp))

        // 6. 存储仓储
        Text("画匣与存储仓储", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("原图存储位置 (Vault Path)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = prefs.vaultPath.ifBlank { getDefaultStorageDirectory() },
                    onValueChange = { SettingsManager.setVaultPath(it) },
                    placeholder = { Text(getDefaultStorageDirectory()) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodySmall,
                    singleLine = true
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "指定保存原始大图与动图的本地路径。KMP 端暂不支持自动文件夹选择，请手动输入合法绝对路径。画匣的校验功能将对齐此目录。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    lineHeight = 18.sp
                )
            }
        }

        // 6. 标签词典导入
        Text("高级映射：双向标签词典", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        var jsonInput by remember { mutableStateOf("") }
        var importSuccess by remember { mutableStateOf<Boolean?>(null) }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("导入自定义 JSON 映射", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    placeholder = { Text("""{"1girl": "单人女孩", "landscape": "风景"}""") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    textStyle = MaterialTheme.typography.bodySmall,
                    maxLines = 5
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "粘贴如上格式的 JSON 对象覆盖或追加本地词库。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    )
                    Button(onClick = { 
                        TagLexiconManager.loadLexiconFromJson(jsonInput)
                        importSuccess = true
                        jsonInput = ""
                    }) {
                        Text("应用映射")
                    }
                }
                if (importSuccess == true) {
                    Spacer(Modifier.height(4.dp))
                    Text("导入成功！", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        // 7. 架构与致谢说明
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Kami Paperbox (纸匣)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "• 原项目: Atsukiizumi/Kami-paperbox\n• 架构: Compose Multiplatform (Desktop / Android 双轨优化)\n• 离线引擎: Room Database + SQLite 全平台持久化",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
