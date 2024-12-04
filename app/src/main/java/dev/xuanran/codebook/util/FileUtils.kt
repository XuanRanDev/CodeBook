package dev.xuanran.codebook.util

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder

object FileUtils {
    fun readAssetTextFile(context: Context, fileName: String): String {
        val text = StringBuilder()
        val assetManager = context.getAssets()

        try {
            assetManager.open(fileName).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while ((reader.readLine().also { line = it }) != null) {
                        text.append(line).append("\n")
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return text.toString()
    }
}
