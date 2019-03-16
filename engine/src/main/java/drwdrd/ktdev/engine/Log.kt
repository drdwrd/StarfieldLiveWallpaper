package drwdrd.ktdev.engine

class Log {

    companion object {

        fun debug(tag : String, msg: String) {
            if(BuildConfig.DEBUG) {
                android.util.Log.d(tag, msg)
            }
        }

        fun info(tag : String, msg: String) {
            android.util.Log.i(tag, msg)
        }

        fun warning(tag : String, msg: String) {
            android.util.Log.w(tag, msg)
        }

        fun error(tag : String, msg: String) {
            android.util.Log.e(tag, msg)
        }

    }
}