package pl.gocards.ui.cards.slider.page.study.ui

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Grzegorz Ziemski
 */
class ScrollAwareWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr), CoroutineScope by MainScope() {

    var onScrollStart: (() -> Unit)? = null
    var onScrollStop: (() -> Unit)? = null

    private var isScrolling = false

    private var debounceJob: Job? = null

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        if (!isScrolling) {
            isScrolling = true
            onScrollStart?.invoke()
        }

        debounceJob?.cancel()
        debounceJob = launch {
            delay(200)
            isScrolling = false
            onScrollStop?.invoke()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}