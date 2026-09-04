import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/App.kt", "r") as f:
    text = f.read()

# Add QUEUE to enum
text = text.replace('    SETTINGS("纸谱")', '    SETTINGS("纸谱"),\n    QUEUE("传输")')

# Add to AppScreenContent
text = text.replace('        Screen.SETTINGS -> SettingsView(isCompact = isCompact)\n    }', '        Screen.SETTINGS -> SettingsView(isCompact = isCompact)\n        Screen.QUEUE -> QueueView(isCompact = isCompact)\n    }')

# Navigation Icons for both NavigationBar and NavigationRail
nav_icon_patch = """
                                            Screen.HISTORY -> if (currentScreen == screen) Icons.Filled.History else Icons.Outlined.History
                                            Screen.SETTINGS -> if (currentScreen == screen) Icons.Filled.Tune else Icons.Outlined.Tune
                                            Screen.QUEUE -> if (currentScreen == screen) Icons.Filled.Download else Icons.Outlined.Download
"""

text = re.sub(r'(\s+)Screen\.HISTORY -> if \(currentScreen == screen\) Icons\.Filled\.History else Icons\.Outlined\.History\s+Screen\.SETTINGS -> if \(currentScreen == screen\) Icons\.Filled\.Tune else Icons\.Outlined\.Tune', nav_icon_patch, text)

# Ensure Icons.Filled.Download / Icons.Outlined.Download are imported
if "import androidx.compose.material.icons.filled.Download" not in text:
    text = text.replace("import androidx.compose.material.icons.filled.Tune", "import androidx.compose.material.icons.filled.Tune\nimport androidx.compose.material.icons.filled.Download")
if "import androidx.compose.material.icons.outlined.Download" not in text:
    text = text.replace("import androidx.compose.material.icons.outlined.Tune", "import androidx.compose.material.icons.outlined.Tune\nimport androidx.compose.material.icons.outlined.Download")


with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/App.kt", "w") as f:
    f.write(text)

