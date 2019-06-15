package drwdrd.ktdev.engine

import android.content.Context
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.reflect.KClass

interface LogOutput {
    fun debug(tag : String, msg : String)
    fun info(tag : String, msg : String)
    fun warning(tag : String, msg : String)
    fun error(tag : String, msg : String)
    fun verbose(tag: String, msg : String)
}

class FileLogOutput(context: Context, name: String) : LogOutput, Closeable, Flushable {

    private val logFileWriter : BufferedWriter
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    init {
        val file = File(context.getExternalFilesDir(null), name)
        logFileWriter = BufferedWriter(FileWriter(file, true))
    }

    override fun debug(tag: String, msg: String) {
        logFileWriter.write("%s Debug/%s: %s\n".format(dateFormatter.format(Date()), tag, msg))
        logFileWriter.flush()
    }

    override fun error(tag: String, msg: String) {
        logFileWriter.write("%s Error/%s: %s\n".format(dateFormatter.format(Date()), tag, msg))
        logFileWriter.flush()
    }

    override fun info(tag: String, msg: String) {
        logFileWriter.write("%s Info/%s: %s\n".format(dateFormatter.format(Date()), tag, msg))
        logFileWriter.flush()
    }

    override fun verbose(tag: String, msg: String) {
        logFileWriter.write("%s Verbose/%s: %s\n".format(dateFormatter.format(Date()), tag, msg))
        logFileWriter.flush()
    }

    override fun warning(tag: String, msg: String) {
        logFileWriter.write("%s Warning/%s: %s\n".format(dateFormatter.format(Date()), tag, msg))
        logFileWriter.flush()
    }

    override fun close() {
        logFileWriter.close()
        if(Log.logOutput == this) {
            Log.logOutput = null
        }
    }

    override fun flush() {
        logFileWriter.flush()
    }
}

class Log {

    companion object {

        var logOutput : LogOutput? = null

        fun debug(tag : String, msg: String) {
            android.util.Log.d(tag, msg)
            logOutput?.debug(tag, msg)
        }

        fun info(tag : String, msg: String) {
            android.util.Log.i(tag, msg)
            logOutput?.info(tag, msg)
        }

        fun warning(tag : String, msg: String) {
            android.util.Log.w(tag, msg)
            logOutput?.warning(tag, msg)
        }

        fun error(tag : String, msg: String) {
            android.util.Log.e(tag, msg)
            logOutput?.error(tag, msg)
        }

        fun verbose(tag : String, msg : String) {
            android.util.Log.v(tag, msg)
            logOutput?.verbose(tag, msg)
        }

    }
}

inline fun <reified T> T.logi(message: String, onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null) =
    log(onlyInDebugMode) { Log.info(getClassTag(enclosingClass), message) }

inline fun <reified T> T.logi(onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null, lazyMessage: () -> String) {
    log(onlyInDebugMode) { Log.info(getClassTag(enclosingClass), lazyMessage.invoke()) }
}

inline fun <reified T> T.logd(message: String, onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null) =
    log(onlyInDebugMode) { Log.debug(getClassTag(enclosingClass), message) }

inline fun <reified T> T.logd(onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null, lazyMessage: () -> String) {
    log(onlyInDebugMode) { Log.debug(getClassTag(enclosingClass), lazyMessage.invoke()) }
}

inline fun <reified T> T.logv(message: String, onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null) =
    log(onlyInDebugMode) { Log.verbose(getClassTag(enclosingClass), message) }

inline fun <reified T> T.logv(onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null, lazyMessage: () -> String) {
    log(onlyInDebugMode) { Log.verbose(getClassTag(enclosingClass), lazyMessage.invoke()) }
}

inline fun <reified T> T.loge(message: String, onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null) =
    log(onlyInDebugMode) { Log.error(getClassTag(enclosingClass), message) }

inline fun <reified T> T.loge(onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null, lazyMessage: () -> String) {
    log(onlyInDebugMode) { Log.error(getClassTag(enclosingClass), lazyMessage.invoke()) }
}

inline fun <reified T> T.logw(message: String, onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null) =
    log(onlyInDebugMode) { Log.warning(getClassTag(enclosingClass), message) }

inline fun <reified T> T.logw(onlyInDebugMode: Boolean = true, enclosingClass: KClass<*>? = null, lazyMessage: () -> String) {
    log(onlyInDebugMode) { Log.warning(getClassTag(enclosingClass), lazyMessage.invoke()) }
}

inline fun log(onlyInDebugMode: Boolean, logger: () -> Unit) {
    when {
        onlyInDebugMode && BuildConfig.DEBUG -> logger()
        !onlyInDebugMode -> logger()
    }
}

inline fun <reified T> T.getClassTag(enclosingClass: KClass<*>?): String =

    if(T::class.java.simpleName.isNotBlank()) {
        T::class.java.simpleName
    }
    else { // Enforce the caller to pass a class to retrieve its simple name
        enclosingClass?.simpleName ?: throw IllegalArgumentException("enclosingClass cannot be null when invoked from an anonymous class")
    }

