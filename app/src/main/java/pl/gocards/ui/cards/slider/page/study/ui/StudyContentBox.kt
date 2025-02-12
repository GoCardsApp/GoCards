package pl.gocards.ui.cards.slider.page.study.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.viewinterop.AndroidView
import com.linkifytext.LinkifyText
import pl.gocards.room.entity.deck.DeckConfig
import pl.gocards.room.entity.deck.DeckConfig.Companion.STUDY_CARD_FONT_SIZE_MAX
import pl.gocards.room.entity.deck.DeckConfig.Companion.STUDY_CARD_FONT_SIZE_MIN
import pl.gocards.room.util.HtmlUtil


/**
 * @author Grzegorz Ziemski
 * TODO Scrollbars are not implemented in Compose yet.
 * https://developer.android.com/jetpack/androidx/compose-roadmap
 */
@Composable
@SuppressLint("SetJavaScriptEnabled")
fun StudyContentBox(
    page: Int,
    pagerState: PagerState,
    content: String,
    isSimpleHtml: Boolean,
    isFullHtml: Boolean,
    fontSize: MutableState<TextUnit?>,
    modifier: Modifier,
    height: Int,
    onScroll: (enabled: Boolean) -> Unit
) {
    if (isFullHtml) {
        HtmlStudyBox(
            page,
            pagerState.currentPage,
            content,
            modifier,
            height,
            onScroll
        )
    } else {
        TextStudyBox(
            content,
            isSimpleHtml,
            fontSize,
            modifier
        )
    }
}


@Composable
private fun HtmlStudyBox(
    page: Int,
    activePage: Int,
    content: String,
    modifier: Modifier,
    height: Int,
    @SuppressWarnings("unused")
    onScroll: (enabled: Boolean) -> Unit
) {
    val width = remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .onGloballyPositioned { coordinates ->
                width.intValue = coordinates.size.width
            },
        contentAlignment = Alignment.Center
    ) {
        if (page == activePage) {
            val textColor =
                LocalTextStyle.current.color.takeOrElse { LocalContentColor.current }
                    .toArgb()

            val context = LocalContext.current

            val processedContent = remember(content, width.intValue, height) {
                processContent(context, content, width.intValue, height, textColor)
            }

            val webView = remember { ScrollAwareWebView(context, onScroll) }

            AndroidView(
                factory = { context -> webView },
                update = { webView ->
                    webView.loadDataWithBaseURL(null, processedContent, "text/html", "UTF-8", null)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
private fun ScrollAwareWebView(
    context: Context,
    onScroll: (enabled: Boolean) -> Unit
): ScrollAwareWebView {
    return ScrollAwareWebView(context).apply {
        webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest
            ): Boolean {
                val intent = Intent(Intent.ACTION_VIEW, request.url)
                view!!.context.startActivity(intent)
                return true
            }
        }
        webChromeClient = WebChromeClient()

        settings.apply {
            javaScriptEnabled = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
        }


        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        onScrollStart = { onScroll(true) }
        onScrollStop = { onScroll(false) }
    }
}

private fun processContent(
    context: Context,
    content: String,
    width: Int,
    height: Int,
    textColor: Int
): String {
    val textHexColor = String.format("#%06X", textColor).replace("#FF", "#")
    var bodyContent = HtmlUtil.getInstance().replaceYtIframe(content, pxToDp(width), pxToDp(height))
    bodyContent = HtmlUtil.getInstance().replaceYtPortraitIframe(bodyContent, pxToDp(height))
    bodyContent = bodyContent.replace("\n", "<br/>")

    return context.assets.open("study_content_template.html")
        .bufferedReader()
        .use { it.readText() }
        .replace("{{body_font_colour}}", textHexColor)
        .replace("{{content}}", bodyContent)
}

@Composable
private fun TextStudyBox(
    content: String,
    isSimpleHtml: Boolean,
    fontSize: MutableState<TextUnit?>,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit, zoomFontSize(fontSize)),
        contentAlignment = Alignment.Center
    ) {
        LinkifyText(
            text = if (isSimpleHtml) {
                HtmlUtil.getInstance().fromHtml(content).toString()
            } else {
                content
            },
            modifier = Modifier.verticalScroll(rememberScrollState()),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium.copy(
                lineHeight = 1.2 * (fontSize.value ?: DeckConfig.STUDY_CARD_FONT_SIZE_DEFAULT.sp),
                fontSize = fontSize.value ?: DeckConfig.STUDY_CARD_FONT_SIZE_DEFAULT.sp
            )
        )
    }
}

/**
 * C_U_36 Pinch-to-zoom the term/definition
 * @author Grzegorz Ziemski
 */
@SuppressLint("ReturnFromAwaitPointerEventScope")
private fun zoomFontSize(fontSize: MutableState<TextUnit?>): suspend PointerInputScope.() -> Unit {
    return {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val zoom = event.calculateZoom()
                if (zoom != 0f) {
                    val fontSizeVal = fontSize.value ?: DeckConfig.STUDY_CARD_FONT_SIZE_DEFAULT.sp
                    val newSize = fontSizeVal * zoom
                    if (newSize.value > STUDY_CARD_FONT_SIZE_MIN
                        && newSize.value < STUDY_CARD_FONT_SIZE_MAX
                        && fontSizeVal != newSize
                    ) {
                        fontSize.value = newSize
                        event.changes.forEach { it.consume() }
                    }
                }
            }
        }
    }
}

fun pxToDp(px: Int): Int {
    return (px / Resources.getSystem().displayMetrics.density).toInt()
}