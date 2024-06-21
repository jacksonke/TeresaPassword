package com.jacksonke.teresapassword

import io.objectbox.android.BuildConfig
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
class SiteEntity {
    @Id
    var id: Long
    var name: String
    var ver: Int
    var type: Int = Constants.TYPE_NUMBER_ABC

    constructor(siteName: String) {
        id = 0
        name = siteName
        ver = 0
        type = Constants.TYPE_NUMBER_ABC
    }

    constructor() {
        id = 0
        name = ""
        ver = 0
    }

    override fun toString(): String {
        return name
    }

    val debugString: String
        get() {
            var ret = ""
            if (BuildConfig.DEBUG) {
                ret = (ret + " id:" + id
                        + " name:" + name
                        + " ver:" + ver
                        + " type:" + type)
            }
            return ret
        }
}