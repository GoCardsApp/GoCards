package pl.gocards.util;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.ref.WeakReference;

/**
 * @author Grzegorz Ziemski
 */
public class FirebaseAnalyticsHelper {

    @NonNull
    public static final String PAGE = "page";

    @NonNull
    public static final String CREATE_SAMPLE_DECK = "create_sample_deck";

    @NonNull
    public static final String CREATE_DECK = "create_deck";

    @NonNull
    public static final String EXPORT_DECK = "export_deck";

    @NonNull
    public static final String IMPORT_DECK = "import_deck";

    @NonNull
    public static final String SYNC_DECK = "sync_deck";

    @NonNull
    public static final String CREATE_CARD = "create_card";

    @NonNull
    public static final String UPDATE_CARD = "save_card";

    @NonNull
    public static final String CLICK_AGAIN = "click_again";

    @NonNull
    public static final String CLICK_QUICK = "click_quick";

    @NonNull
    public static final String CLICK_EASY = "click_easy";

    @NonNull
    public static final String CLICK_HARD = "click_hard";

    @NonNull
    public static final String STUDY_REVERT_LEARNING_PROGRESS = "study_revert_learning_progress";

    @NonNull
    public static final String SLIDER_NEW_CARD = "slider_new_card";

    @NonNull
    public static final String SLIDER_EDIT_CARD = "slider_edit_card";

    @NonNull
    public static final String SLIDER_DELETE_CARD = "slider_delete_card";

    @NonNull
    public static final String SLIDER_DELETE_NEW_CARD = "slider_delete_new_card";

    @NonNull
    public static final String SLIDER_RESTORE_CARD = "slider_restore_card";

    @NonNull
    public static final String SLIDER_SCROLL = "slider_scroll";

    @NonNull
    public static final String SLIDER_STUDY_MODE = "slider_study_mode";

    @NonNull
    public static final String MENU_OPEN_DISCORD = "menu_open_discord";

    @NonNull
    public static final String DISCOVER_OPEN_DISCORD = "discover_open_discord";

    @NonNull
    public static final String DISCOVER_OPEN_FANPAGE = "discover_open_fanpage";

    @NonNull
    public static final String DISCOVER_OPEN_YOUTUBE = "discover_open_youtube";

    @NonNull
    public static final String DISCOVER_OPEN_REVIEW = "discover_open_review";

    @NonNull
    public static final String DISCOVER_OPEN_REVIEW_IN_APP = "discover_open_review_in_app";

    @NonNull
    public static final String DISCOVER_OPEN_REVIEW_FULL_PAGE = "discover_open_review_full_page";

    @NonNull
    public static final String STUDY_OPEN_REVIEW_IN_APP = "study_open_review_in_app";

    @NonNull
    public static final String DISCOVER_OPEN_FEEDBACK_REVIEW = "discover_open_feedback_review";

    @NonNull
    public static final String DISCOVER_OPEN_FEEDBACK_PREMIUM = "discover_open_feedback_premium";

    @NonNull
    public static final String EXPLORE_POLL_YES = "explore_poll_yes";

    @NonNull
    public static final String EXPLORE_POLL_NO = "explore_poll_no";

    @NonNull
    private final FirebaseAnalytics firebaseAnalytics;

    @Nullable
    private static WeakReference<FirebaseAnalyticsHelper> INSTANCE;

    private FirebaseAnalyticsHelper(@NonNull Context context) {
        this.firebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static synchronized FirebaseAnalyticsHelper getInstance(@NonNull Context context) {
        if (INSTANCE == null || INSTANCE.get() == null) {
            INSTANCE = new WeakReference<>(new FirebaseAnalyticsHelper(context));
        }
        return INSTANCE.get();
    }

    public void createSampleDeck() {
        firebaseAnalytics.logEvent(CREATE_SAMPLE_DECK, new Bundle());
        createDeck();
    }

    public void createDeck() {
        firebaseAnalytics.logEvent(CREATE_DECK, new Bundle());
    }

    public void importDeck() {
        firebaseAnalytics.logEvent(IMPORT_DECK, new Bundle());
        createDeck();
    }

    public void exportDeck() {
        firebaseAnalytics.logEvent(EXPORT_DECK, new Bundle());
    }

    public void syncDeck() {
        firebaseAnalytics.logEvent(SYNC_DECK, new Bundle());
    }

    public void createCard(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(CREATE_CARD, bundle);
    }

    public void updateCard(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(UPDATE_CARD, bundle);
    }

    public void again(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(CLICK_AGAIN, bundle);
    }

    public void quick(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(CLICK_QUICK, bundle);
    }

    public void easy(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(CLICK_EASY, bundle);
    }

    public void hard(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(CLICK_HARD, bundle);
    }

    public void revertLearningProgress(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(STUDY_REVERT_LEARNING_PROGRESS, bundle);
    }

    public void sliderNewCard(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(SLIDER_NEW_CARD, bundle);
    }

    public void sliderEditCard(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(SLIDER_EDIT_CARD, bundle);
    }

    public void sliderDeleteCard(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(SLIDER_DELETE_CARD, bundle);
    }

    public void sliderDeleteNewCard(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(SLIDER_DELETE_NEW_CARD, bundle);
    }

    public void sliderRestoreCard(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(SLIDER_RESTORE_CARD, bundle);
    }

    public void sliderScroll(
            Integer fromPage,
            String fromType,
            int toPage,
            String toType
    ) {
        var bundle = new Bundle();
        if (fromPage != null) {
            bundle.putInt("fromPage", fromPage);
        }
        bundle.putString("fromType", fromType);
        bundle.putInt("toPage", toPage);
        bundle.putString("toType", toType);
        firebaseAnalytics.logEvent(SLIDER_SCROLL, bundle);
    }

    public void sliderStudyMode(int page) {
        var bundle = new Bundle();
        bundle.putInt(PAGE, page);
        firebaseAnalytics.logEvent(SLIDER_STUDY_MODE, bundle);
    }

    public void menuOpenDiscord() {
        firebaseAnalytics.logEvent(MENU_OPEN_DISCORD, new Bundle());
    }

    public void discoverOpenDiscord() {
        firebaseAnalytics.logEvent(DISCOVER_OPEN_DISCORD, new Bundle());
    }

    public void discoverOpenFanpage() {
        firebaseAnalytics.logEvent(DISCOVER_OPEN_FANPAGE, new Bundle());
    }

    public void discoverOpenYoutube() {
        firebaseAnalytics.logEvent(DISCOVER_OPEN_YOUTUBE, new Bundle());
    }

    public void discoverOpenReview() {
        firebaseAnalytics.logEvent(DISCOVER_OPEN_REVIEW, new Bundle());
    }

    public void discoverOpenReviewInApp() {
        firebaseAnalytics.logEvent(DISCOVER_OPEN_REVIEW_IN_APP, new Bundle());
    }

    public void discoverOpenReviewFullPage() {
        firebaseAnalytics.logEvent(DISCOVER_OPEN_REVIEW_FULL_PAGE, new Bundle());
    }

    public void studyOpenReviewInApp() {
        firebaseAnalytics.logEvent(STUDY_OPEN_REVIEW_IN_APP, new Bundle());
    }

    public void setDiscoverOpenFeedbackReview() {
        firebaseAnalytics.logEvent(DISCOVER_OPEN_FEEDBACK_PREMIUM, new Bundle());
    }

    public void setDiscoverOpenFeedbackPremium() {
        firebaseAnalytics.logEvent(DISCOVER_OPEN_FEEDBACK_PREMIUM, new Bundle());
    }

    public void explorePollYes() {
        firebaseAnalytics.logEvent(EXPLORE_POLL_YES, new Bundle());
    }

    public void explorePollNo() {
        firebaseAnalytics.logEvent(EXPLORE_POLL_NO, new Bundle());
    }
}
