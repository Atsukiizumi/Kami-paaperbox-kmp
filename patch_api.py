import re

with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "r") as f:
    text = f.read()

# Fix Danbooru tags
danbooru_replace = """            val original = post.large_file_url ?: post.file_url ?: preview
            val tagList = post.tag_string?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
            WorkCard(
                id = post.id?.toString() ?: "",
                thumb = preview,
                originalUrl = original,
                tags = tagList,
                title = "Danbooru #${post.id}",
"""
text = re.sub(r'            val original = post\.large_file_url \?: post\.file_url \?: preview\s+WorkCard\(\s+id = post\.id\?\.toString\(\) \?: "",\s+thumb = preview,\s+originalUrl = original,\s+title = "Danbooru #\$\{post\.id\}",', danbooru_replace, text)

# Fix Pixiv multiple images and tags
pixiv_replace = """            val finalOriginal = if (usePixivMirror && !original.contains("obfs.dev")) {
                original.replace("i.pximg.net", "i.pixiv.re")
            } else original
            
            val additional = illust.meta_pages?.mapNotNull { page ->
                val pageUrl = page.image_urls?.large ?: page.image_urls?.medium
                if (pageUrl != null && usePixivMirror && !pageUrl.contains("obfs.dev")) {
                    pageUrl.replace("i.pximg.net", "i.pixiv.re")
                } else pageUrl
            } ?: emptyList()
            
            val tagList = illust.tags?.map { it.name } ?: emptyList()

            WorkCard(
                id = illust.id.toString(),
                thumb = finalPreview,
                originalUrl = finalOriginal,
                additionalImages = additional,
                tags = tagList,
                title = illust.title,"""

text = re.sub(r'            val finalOriginal = if \(usePixivMirror && !original\.contains\("obfs\.dev"\)\) \{\s+original\.replace\("i\.pximg\.net", "i\.pixiv\.re"\)\s+\} else original\s+WorkCard\(\s+id = illust\.id\.toString\(\),\s+thumb = finalPreview,\s+originalUrl = finalOriginal,\s+title = illust\.title,', pixiv_replace, text)


# Fix Fanbox multiple images
fanbox_replace = """            if (preview != null && original != null) {
                val additional = post.body?.images?.map { it.originalUrl } ?: emptyList()
                cards.add(
                    WorkCard(
                        id = post.id,
                        thumb = preview,
                        originalUrl = original,
                        additionalImages = additional,
                        title = post.title,"""
text = re.sub(r'            if \(preview != null && original != null\) \{\s+cards\.add\(\s+WorkCard\(\s+id = post\.id,\s+thumb = preview,\s+originalUrl = original,\s+title = post\.title,', fanbox_replace, text)


# Fix Moebooru tags
moebooru_replace = """            val original = post.sample_url ?: post.file_url ?: preview
            val tagList = post.tags?.split(" ")?.filter { it.isNotBlank() } ?: emptyList()
            WorkCard(
                id = post.id?.toString() ?: "",
                thumb = preview,
                originalUrl = original,
                tags = tagList,
                title = "${source.displayName} #${post.id}","""
text = re.sub(r'            val original = post\.sample_url \?: post\.file_url \?: preview\s+WorkCard\(\s+id = post\.id\?\.toString\(\) \?: "",\s+thumb = preview,\s+originalUrl = original,\s+title = "\$\{source\.displayName\} #\$\{post\.id\}",', moebooru_replace, text)


with open("composeApp/src/commonMain/kotlin/com/aistudio/kamipaperbox/ApiClient.kt", "w") as f:
    f.write(text)

