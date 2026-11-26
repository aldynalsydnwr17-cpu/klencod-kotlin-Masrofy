package app.fnfmodmanager.data

import android.content.ContentResolver
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document
import java.io.InputStream
import java.io.OutputStream

/**
 * Thin wrapper over the Storage Access Framework for ONE granted tree.
 *
 * It talks to DocumentsContract directly (not DocumentFile) so that creating thousands of files
 * does not re-query the folder for every file. Methods throw on provider errors; callers decide
 * how to report them.
 */
class SafFs(private val cr: ContentResolver, val tree: Uri) {

    data class Child(
        val docId: String,
        val name: String,
        val mime: String,
        val size: Long,
        val modified: Long,
        val uri: Uri,
    ) {
        val isDir: Boolean get() = mime == Document.MIME_TYPE_DIR
    }

    data class Stat(val name: String?, val size: Long)

    fun rootDoc(): Uri = DocumentsContract.buildDocumentUriUsingTree(tree, DocumentsContract.getTreeDocumentId(tree))

    fun docUri(docId: String): Uri = DocumentsContract.buildDocumentUriUsingTree(tree, docId)

    fun rootName(): String = stat(rootDoc())?.name ?: ""

    fun children(parent: Uri): List<Child> {
        val parentId = DocumentsContract.getDocumentId(parent)
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(tree, parentId)
        val out = ArrayList<Child>()
        val cols = arrayOf(
            Document.COLUMN_DOCUMENT_ID, Document.COLUMN_DISPLAY_NAME, Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE, Document.COLUMN_LAST_MODIFIED,
        )
        cr.query(childrenUri, cols, null, null, null)?.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(0)
                out += Child(
                    docId = id,
                    name = c.getString(1) ?: "",
                    mime = c.getString(2) ?: "",
                    size = if (c.isNull(3)) -1L else c.getLong(3),
                    modified = if (c.isNull(4)) 0L else c.getLong(4),
                    uri = docUri(id),
                )
            }
        }
        return out
    }

    private fun normalize(created: Uri?): Uri? = created?.let { docUri(DocumentsContract.getDocumentId(it)) }

    fun mkdir(parent: Uri, name: String): Uri? =
        normalize(DocumentsContract.createDocument(cr, parent, Document.MIME_TYPE_DIR, name))

    fun createFile(parent: Uri, name: String, mime: String): Uri? =
        normalize(DocumentsContract.createDocument(cr, parent, mime, name))

    fun delete(uri: Uri): Boolean = try {
        DocumentsContract.deleteDocument(cr, uri)
    } catch (e: Exception) {
        false
    }

    fun stat(uri: Uri): Stat? = try {
        cr.query(uri, arrayOf(Document.COLUMN_DISPLAY_NAME, Document.COLUMN_SIZE), null, null, null)?.use { c ->
            if (c.moveToFirst()) Stat(c.getString(0), if (c.isNull(1)) -1L else c.getLong(1)) else null
        }
    } catch (e: Exception) {
        null
    }

    fun openIn(uri: Uri): InputStream = cr.openInputStream(uri) ?: error("cannot open $uri")

    /** mode "w" for a new file, "wt" to truncate an existing one. */
    fun openOut(uri: Uri, mode: String = "w"): OutputStream = cr.openOutputStream(uri, mode) ?: error("cannot write $uri")

    fun readBytes(uri: Uri): ByteArray = openIn(uri).use { it.readBytes() }
}
