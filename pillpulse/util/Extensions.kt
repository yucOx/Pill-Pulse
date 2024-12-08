package com.yucox.pillpulse.util

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.yucox.pillpulse.data.locale.entity.AlarmRealm
import com.yucox.pillpulse.domain.model.AlarmInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.isVisible() = visibility == View.VISIBLE
fun View.isGone() = visibility == View.GONE
fun View.isInvisible() = visibility == View.INVISIBLE

fun Fragment.showToast(message: String) {
    requireActivity().showToast(message)
}

fun Fragment.showToastLong(message: String) {
    requireActivity().showToastLong(message)
}

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Context.showToastLong(message: String, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, message, duration).show()
}

fun View.showKeyboard() {
    val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
    imm.showSoftInput(this, 0)
}

fun View.hideKeyboard() {
    val imm =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showSnackbar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
    Snackbar.make(this, message, duration).show()
}

fun View.showSnackbarWithAction(
    message: String,
    actionText: String,
    duration: Int = Snackbar.LENGTH_INDEFINITE,
    action: () -> Unit
) {
    Snackbar.make(this, message, duration)
        .setAction(actionText) { action() }
        .show()
}

// Date Extensions
fun Date?.toFormattedDateString(): String {
    if (this == null) return ""
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return sdf.format(this)
}

fun Date?.toFormattedTimeString(): String {
    if (this == null) return ""
    val shf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return shf.format(this)
}

// String Extensions
fun String?.toDate(): Date {
    if (this == null) return Date()
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    return try {
        sdf.parse(this) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}

fun String?.toTime(): Date {
    if (this == null) return Date()
    val shf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return try {
        shf.parse(this) ?: Date()
    } catch (e: Exception) {
        Date()
    }
}

fun AlarmRealm.toAlarmInfo() : AlarmInfo {
    return AlarmInfo(
        requestCode = requestCode,
        pillName = pillName,
        info = info,
        repeating = repeating,
        userMail = userMail,
        alarmLocation = id.toHexString(),
        alarmTime = alarmTime?.toTime() ?: Date(),
        onOrOff = onOrOff
    )
}
