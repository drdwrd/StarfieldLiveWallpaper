package drwdrd.ktdev.engine

class Log {

    companion object {

        lateinit var tag : String

        fun debug(msg: String) {
            if(BuildConfig.DEBUG) {
                android.util.Log.d(tag, msg)
            }
        }

        fun info(msg: String) {
            android.util.Log.i(tag, msg)
        }

        fun warning(msg: String) {
            android.util.Log.w(tag, msg)
        }

        fun error(msg: String) {
            android.util.Log.e(tag, msg)
        }

    }
}