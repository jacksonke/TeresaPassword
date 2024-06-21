package com.jacksonke.teresapassword


class Generator private constructor() {
    private var mSecret = "teresapassword"
    fun setSecret(secret: String) {
        mSecret = secret
    }

    fun generate(entity: SiteEntity): String {
        val siteName: String = entity.name
        val type: Int = entity.type
        val ver: Int = entity.ver

        if (siteName.trim().isEmpty()) {
            throw RuntimeException("site name is invalide")
        }
        return generate(siteName, mSecret, ver, type)
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun generate(sitename: String?, secret: String?, ver: Int, type: Int): String

    companion object {
        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }

        private var instance = Generator()
        fun instance(): Generator {
            return instance
        }
    }
}