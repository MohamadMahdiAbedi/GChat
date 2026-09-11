package ir.gchat

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.security.MessageDigest

fun Context.getFileName(uri: Uri): String? {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && index >= 0) {
            return cursor.getString(index)
        }
    }
    return null
}

fun Context.getFileSize(uri: Uri): Long? {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst() && index >= 0) {
            return cursor.getLong(index)
        }
    }
    return null
}

fun Context.sha256(uri: Uri): String? {
    val digest = MessageDigest.getInstance("SHA-256")

    contentResolver.openInputStream(uri)?.use { input ->
        val buffer = ByteArray(8192)

        while (true) {
            val read = input.read(buffer)
            if (read == -1) break

            digest.update(buffer, 0, read)
        }
    } ?: return null

    return digest.digest().joinToString("") {
        "%02x".format(it)
    }
}
