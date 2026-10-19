package isel.dei.pdm.demos.demo8puzzle.shared

import android.util.Log

const val APP_TAG = "Demo8PuzzleApp"

fun buildLLogTag(className: String) = "$APP_TAG.$className"
fun buildLogMessage(className: String, message: String) = "$APP_TAG.$className: $message"

fun <T : Any> T.log(message: String) {
    this::class.java.simpleName.also {
        Log.v(buildLLogTag(it), buildLogMessage(it, message))
    }
}