package dev.dworks.apps.alauncher.extras.tvrecommendations.service;

import android.net.Uri;

public interface DbMigrationContract {
    Uri CONTENT_UPDATE_URI = Uri.parse("content://com.android.google.tvrecommendations.migration/migrated");
    Uri CONTENT_URI = Uri.parse("content://com.android.google.tvrecommendations.migration/data");
}
