package pl.gocards.ui.common.pager.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf

/**
 * @author Grzegorz Ziemski
 */
data class DynamicPagerDto<E>(
    private val items: State<List<E>?>,

    val settledPage: State<Int?>,
    val setSettledPage: (Int) -> Unit = {},

    val animateScrollToPage: State<Int?> = mutableStateOf(null),
    val clearAnimateScrollToPage: () -> Unit = {},

    val changePagerPage: State<Int?> = mutableStateOf(null),
    val clearChangePagerPage: () -> Unit = {},

    val userScrollEnabled: MutableState<Boolean> = mutableStateOf(false),
) {

    fun getSize(): Int {
        return items.value?.size ?: 0
    }

    companion object {
        @Composable
        fun <E> create(viewModel: DynamicPagerModel<E>): DynamicPagerDto<E> {
            return DynamicPagerDto(
                items = viewModel.getItemsState(),

                settledPage = viewModel.getSettledPageLiveData().observeAsState(),
                setSettledPage = { viewModel.setSettledPage(it) },

                animateScrollToPage = viewModel.getAnimateScrollToPageLiveData().observeAsState(),
                clearAnimateScrollToPage = { viewModel.clearAnimateScrollToPage() },

                changePagerPage = viewModel.getChangePageLiveData().observeAsState(),
                clearChangePagerPage = { viewModel.clearChangePage() },

                userScrollEnabled = viewModel.scrollEnabled
            )
        }
    }
}