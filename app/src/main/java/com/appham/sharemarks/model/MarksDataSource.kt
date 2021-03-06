package com.appham.sharemarks.model

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.appham.sharemarks.presenter.MarksContract
import com.jskierbi.cupboard.cupboard

/**
 * @author thomas
 */
class MarksDataSource(context: Context) : MarksContract.Model {
    private val dbHelper: DatabaseSQLiteHelper = DatabaseSQLiteHelper(context)

    private var db: SQLiteDatabase = dbHelper.writableDatabase

    //region MarksContract interface methods
    override fun open() {
        db = dbHelper.writableDatabase
    }

    override fun close() {
        dbHelper.close()
    }

    override fun getMarks(deleted: Int): MutableList<MarkItem> =
            cupboard(db).query(MarkItem::class.java)
                    .withSelection("deleted == ?", deleted.toString()).list().asReversed()

    override fun getMarksByDomain(domain: String, deleted: Int): MutableList<MarkItem> =
            cupboard(db).query(MarkItem::class.java)
                    .withSelection("domain = ? AND deleted = ?", domain, deleted.toString()).list()
                    .asReversed()

    override fun putMark(item: MarkItem): Long = cupboard(db).put(item)

    override fun setMarkDeleted(item: MarkItem, toDelete: Boolean): Int {
        val values = ContentValues(1)
        values.put("deleted", toDelete)
        return cupboard(db).update(MarkItem::class.java, values,
                "_id = ?", item._id.toString())
    }

    override fun dropItem(item: MarkItem): Boolean = cupboard(db).delete(item)
//endregion

}