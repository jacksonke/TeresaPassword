package com.jacksonke.teresapassword

import android.content.Context
import io.objectbox.BoxStore
import io.objectbox.query.QueryBuilder

class DBHelper private constructor() {
    private var boxStore: BoxStore? = null
        private set
    private val isInited = false
    fun init(context: Context?) {
        if (isInited) {
            throw RuntimeException("boxStore has already been built")
        }
        boxStore = MyObjectBox.builder().androidContext(context).build()
    }

    fun queryAll(): List<SiteEntity> {
        val box = instance.boxStore!!.boxFor(
            SiteEntity::class.java
        )
        return box.all
    }

    fun queryBySite(site: String): SiteEntity? {
        val box = instance.boxStore!!.boxFor(
            SiteEntity::class.java
        )
        return box.query().equal(SiteEntity_.name, site, QueryBuilder.StringOrder.CASE_SENSITIVE).build().findFirst()
    }

    fun insertSiteEntity(entity: SiteEntity): Long {
        assert(entity.id == 0L)
        val box = instance.boxStore!!.boxFor(
            SiteEntity::class.java
        )
        return box.put(entity)
    }

    fun updateSiteEntity(entity: SiteEntity) {
        assert(entity.id != 0L)
        val box = instance.boxStore!!.boxFor(
            SiteEntity::class.java
        )
        box.put(entity)
    }

    fun rmSiteEntity(siteName: String) {
        val box = instance.boxStore!!.boxFor(
            SiteEntity::class.java
        )
        val siteEntities: List<SiteEntity> =
            box.query().equal(SiteEntity_.name, siteName, QueryBuilder.StringOrder.CASE_SENSITIVE).build().find()
        var item: SiteEntity? = null
        if (siteEntities.isNotEmpty()) {
            item = siteEntities[0]
        }
        if (item != null) {
            box.remove(item)
        }
    }

    companion object {
        val instance = DBHelper()
    }
}