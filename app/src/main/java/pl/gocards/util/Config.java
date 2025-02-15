package pl.gocards.util;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author Grzegorz Ziemski
 */
public class Config {

    private static Config INSTANCE;

    private boolean databaseExternalStorage;

    private boolean testFilesExternalStorage;

    protected Config(@NonNull Context context) {
        try (InputStream is = context.getAssets().open("config.properties")) {
            Properties props = new Properties();
            props.load(is);
            databaseExternalStorage = Boolean.parseBoolean(
                    props.getProperty("database.external_storage", "")
            );
            testFilesExternalStorage = Boolean.parseBoolean(
                    props.getProperty("test.files.external_storage", "")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public static synchronized Config getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new Config(context);
        }
        return INSTANCE;
    }

    public static Config getInstance() {
        return INSTANCE;
    }

    public boolean isDatabaseExternalStorage() {
        return databaseExternalStorage;
    }

    public boolean isTestFilesExternalStorage() {
        return testFilesExternalStorage;
    }

    public boolean isCrashlyticsEnabled(@NonNull Context context) {
        return getPropertyBoolean(context, "crashlytics.enabled", "true");
    }

    public boolean showExceptionInDialogException(@NonNull Context context) {
        return getPropertyBoolean(context, "DialogException.showException", "false");
    }

    public boolean isPremiumMockEnabled(@NonNull Context context) {
        return getPropertyBoolean(context, "premium.mock.enabled", "false");
    }

    public boolean isReviewMockEnabled(@NonNull Context context) {
        return getPropertyBoolean(context, "review.mock.enabled", "false");
    }

    public String discordUrl(@NonNull Context context) {
        return getPropertyString(context, "discord.url");
    }

    public String fanpageUrl(@NonNull Context context) {
        return getPropertyString(context, "fanpage.url");
    }

    public String appPageUrl(@NonNull Context context) {
        return getPropertyString(context, "google-play.app-page.url");
    }

    public String subscriptionsUrl(@NonNull Context context) {
        return getPropertyString(context, "google-play.subscriptions.url");
    }

    public String youtubeUrl(@NonNull Context context) {
        return getPropertyString(context, "youtube.url");
    }

    public String feedbackReviewUrl(@NonNull Context context) {
        return getPropertyString(context, "feedback.review.url");
    }

    public String feedbackPremiumUrl(@NonNull Context context) {
        return getPropertyString(context, "feedback.premium.url");
    }

    public boolean isUnderConstruction(@NonNull Context context) {
        return getPropertyBoolean(context, "underConstruction", "true");
    }

    private String getPropertyString(@NonNull Context context, String key) {
        return getProperty(context, props -> props.getProperty(key, ""));
    }

    private boolean getPropertyBoolean(@NonNull Context context, String key, String defaultValue) {
        return Boolean.TRUE.equals(getProperty(context, props -> Boolean.parseBoolean(
                props.getProperty(key, defaultValue)
        )));
    }

    private <T> T getProperty(@NonNull Context context, ProcessProperty<T> fn) {
        try (InputStream is = context.getAssets().open("config.properties")) {
            Properties props = new Properties();
            props.load(is);
            return fn.run(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    interface ProcessProperty<T> {
        T run(Properties props);
    }
}
