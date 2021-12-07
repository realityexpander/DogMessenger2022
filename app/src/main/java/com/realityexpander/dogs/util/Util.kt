package com.realityexpander.dogs.util

import android.Manifest
import android.content.Context
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.realityexpander.dogs.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

val PERMISSION_SEND_SMS = Manifest.permission.SEND_SMS.hashCode().shr(16)
val PERMISSION_WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE.hashCode().shr(16)

fun getProgressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 10f
        centerRadius = 50f
        start()
    }
}

fun ImageView.loadImage(uri: String?, progressDrawable: CircularProgressDrawable) {
    val options = RequestOptions()
        .placeholder(progressDrawable)
        .error(R.mipmap.ic_dog_icon)
    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(uri)
        .into(this)
}

@BindingAdapter("android:imageUrl")
fun loadImage(view: ImageView, url: String?) {
    view.loadImage(url, getProgressDrawable(view.context))
}

fun Long?.getDateString(): String {
    var dateLong: Long? = this ?: return "unknown date"

    val sdf = SimpleDateFormat("EEE MMM dd, yyyy hh:mm a", Locale.US)
    val resultDate = Date(dateLong!!)

    return sdf.format(resultDate)
}

fun Long?.getDateStringWithSeconds(): String {
    var dateLong: Long? = this ?: return "unknown date"

    val sdf = SimpleDateFormat("EEE MMM dd, yyyy hh:mm:ss a", Locale.US)
    val resultDate = Date(dateLong!!)

    return sdf.format(resultDate)
}

/**
 * Swaps the order of the elements of the given [Pair] in a type-safe manner.
 *
 * Example:
 * ```
 * Pair("David", 32).swap()  // Pair(32, "David")
 * ```
 */
fun <A, B> Pair<A, B>.swap(): Pair<B, A> = this.second to this.first

infix fun <A, B, C> Pair<A, B>.to(third: C): Triple<A, B, C> = Triple(first, second, third)

fun Boolean.whenTrue(block: (Boolean) -> Unit): Boolean {
    if (this) block(this)
    return this
}

fun Boolean.whenFalse(block: (Boolean) -> Unit): Boolean {
    if (!this) block(this)
    return this
}

fun <T : Any> Optional<T>.toNullable(): T? = this.orElse(null)

/**
 * Returns the [cartesian product](https://en.wikipedia.org/wiki/Cartesian_product)
 * of the two given collections. The function works generically on any two types.
 *
 * @return A list of [Pair] with the value's order corresponding to the order that
 * the arguments were passed into the function.
 */
fun <T, U> Collection<T>.cartesianProduct(collection2: Collection<U>): List<Pair<T, U>> {
    return this.flatMap { lhs: T -> collection2.map { rhs: U -> lhs to rhs } }
}

/**
 * Returns the first [`n`][percentage] percent of items out of the given [Collection].
 * The percentage gets rounded down to the next integer regarding the size of the returned [List].
 */
fun <T> Collection<T>.takePercent(percentage: Double): List<T> {
    require(percentage in 0.0..1.0) { "A floating point percentage is required" }
    return this.take((this.size * percentage).roundToInt())
}

/**
 * Merges the given [collection][Collection] of type-equivalent [maps][Map] together,
 * while grouping the values of each key together and finally applying the given
 * [lambda][valueMergeFunction] to each value to merge the list of values of each key
 * into a single value such that the returned map has the same type-signature again.
 */
fun <K, V> Collection<Map<K, V>>.merge(valueMergeFunction: (List<V>) -> V): Map<K, V> {
    return this
        .flatMap { it.entries }
        .groupBy({ it.key }) { it.value }
        .mapValues { valueMergeFunction(it.value) }
}

/**
 * Returns if the given range contains the [other] range.
 */
operator fun <T : Comparable<T>> ClosedRange<T>.contains(other: ClosedRange<T>): Boolean {
    return this.start in other && this.endInclusive in other
}

/**
 * Returns if the given range overlaps with the [other] range.
 */
fun <T : Comparable<T>> ClosedRange<T>.overlaps(other: ClosedRange<T>): Boolean {
    return this.start in other || this.endInclusive in other
}

/**
 * Returns any given [Map] with its keys and values reversed to a [MultiValueMap]
 * such that duplicate values will group together their keys.
 * Note that the returned type is immutable.
 */
//fun <K, V> Map<K, V>.reverseToMultiMap(): MultiValueMap<V, K> {
//    return HashMap<V, MutableList<K>>(this.count()).also { reversedMap: HashMap<V, MutableList<K>> ->
//        this.entries.forEach { entry: Map.Entry<K, V> ->
//            reversedMap[entry.value] =
//                reversedMap.getOrPut(entry.value) { mutableListOf() }.also { it.add(entry.key) }
//        }
//    }.toMultiMap()
//}
//
///**
// * Converts any given [Map] to a [MultiValueMap].
// * Note that the returned type is immutable.
// */
//fun <K, V> Map<K, List<V>>.toMultiMap(): MultiValueMap<K, V> {
//    return unmodifiableMultiValueMap(toMultiValueMap(this))
//}
//
///**
// * Returns how many percent of the items of the given [Collection] satisfy the given [predicate].
// *
// * @return A percentage represented as a floating-point number between 0 and 1.
// */
//fun <T> Collection<T>.percentage(predicate: (T) -> Boolean): Double {
//    return this.count { predicate(it) } / this.count().toDouble()
//}