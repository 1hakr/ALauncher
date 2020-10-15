package amirz.shade.customization;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserManager;
import android.util.Log;
import android.view.View;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.popup.SystemShortcut;
import com.android.launcher3.popup.SystemShortcutFactory;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.util.PackageManagerHelper;
import com.android.launcher3.views.Snackbar;

import java.net.URISyntaxException;

import amirz.shade.hidden.HiddenAppsDatabase;
import amirz.shade.util.AppReloader;
import amirz.unread.UnreadSession;

@SuppressWarnings("unused")
public class ShadeShortcutFactory extends SystemShortcutFactory {
    private static final String TAG = "ShadeShortcutFactory";

    public ShadeShortcutFactory(Context context) {
        super(new BottomSheetShortcut(),
                new SystemShortcut.Widgets(),
                new SystemShortcut.Install(),
                new SystemShortcut.DismissPrediction(),
                new HideApp(),
                new UnhideApp(),
                new UnInstall());
    }

    private static boolean canHide(ItemInfo item) {
        return (item instanceof com.android.launcher3.AppInfo
                || item instanceof com.android.launcher3.WorkspaceItemInfo);  //&& item.id == ItemInfo.NO_ID;
    }


    private static class BottomSheetShortcut extends SystemShortcut<Launcher> {
        private BottomSheetShortcut() {
            super(R.drawable.ic_info_no_shadow, R.string.app_info_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(Launcher launcher, ItemInfo itemInfo) {
            final View.OnClickListener onClickMore = v -> onClickMore(launcher, itemInfo, v);
            return new View.OnClickListener() {
                private InfoBottomSheet cbs;

                @Override
                public void onClick(View v) {
                    if (cbs == null) {
                        dismissTaskMenuView(launcher);
                        cbs = (InfoBottomSheet) launcher.getLayoutInflater().inflate(
                                R.layout.app_info_bottom_sheet,
                                launcher.getDragLayer(),
                                false);
                        cbs.setOnAppInfoClick(onClickMore);
                        cbs.populateAndShow(itemInfo);
                    }
                }
            };
        }

        private void onClickMore(Launcher launcher, ItemInfo itemInfo, View view) {
            dismissTaskMenuView(launcher);
            Rect sourceBounds = launcher.getViewBounds(view);
            Bundle opts = launcher.getAppTransitionManager()
                    .getActivityLaunchOptions(launcher, view).toBundle();
            new PackageManagerHelper(launcher).startDetailsActivityForInfo(
                    itemInfo, sourceBounds, opts);
            launcher.getUserEventDispatcher().logActionOnControl(LauncherLogProto.Action.Touch.TAP,
                    LauncherLogProto.ControlType.APPINFO_TARGET, view);
        }
    }

    public static class UnInstall extends SystemShortcut<Launcher> {
        public UnInstall() {
            super(R.drawable.ic_uninstall_no_shadow, R.string.uninstall_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                Launcher launcher, ItemInfo itemInfo) {
            UserManager userManager =
                    (UserManager) launcher.getSystemService(Context.USER_SERVICE);
            Bundle restrictions = userManager.getUserRestrictions(itemInfo.user);
            boolean uninstallDisabled = restrictions.getBoolean(UserManager.DISALLOW_APPS_CONTROL, false)
                    || restrictions.getBoolean(UserManager.DISALLOW_UNINSTALL_APPS, false);
            boolean isSystemApp = PackageManagerHelper.isSystemApp(launcher, itemInfo.getIntent());
            if (isSystemApp || uninstallDisabled) {
                return null;
            }
            return createOnClickListener(launcher, itemInfo);
        }

        public View.OnClickListener createOnClickListener(
                Launcher launcher, ItemInfo itemInfo) {
            return view -> {
                try {
                    ComponentName cn = itemInfo.getTargetComponent();
                    Intent intent = Intent.parseUri(launcher.getString(R.string.delete_package_intent), 0)
                            .setData(Uri.fromParts("package", cn.getPackageName(), cn.getClassName()))
                            .putExtra(Intent.EXTRA_USER, itemInfo.user);
                    launcher.startActivity(intent);
                    AbstractFloatingView.closeAllOpenViews(launcher);
                } catch (URISyntaxException e) {
                    Log.e(TAG, "Failed to parse intent to start uninstall activity for item=" + itemInfo);
                }
            };
        }
    }

    public static class HideApp extends SystemShortcut<Launcher> {
        public HideApp() {
            super(R.drawable.ic_eye_hide, R.string.hide_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                Launcher launcher, ItemInfo itemInfo) {
            boolean canHide = canHide(itemInfo);
            boolean isHidden = HiddenAppsDatabase.isHidden(launcher, itemInfo);
            if (isHidden || !canHide) {
                return null;
            }
            return createOnClickListener(launcher, itemInfo);
        }

        public View.OnClickListener createOnClickListener(
                Launcher launcher, ItemInfo itemInfo) {
            return view -> {
                boolean isHidden = HiddenAppsDatabase.isHidden(launcher, itemInfo);
                HiddenAppsDatabase.setHidden(launcher, itemInfo, !isHidden);
                UnreadSession.getInstance(launcher).forceUpdate(); // Show or hide notifications.
                AppReloader.get(launcher).reload(itemInfo);

                if (!isHidden) {
                    Runnable onUndoClicked = () -> {
                        HiddenAppsDatabase.setHidden(launcher, itemInfo, false);
                        UnreadSession.getInstance(launcher).forceUpdate();
                        AppReloader.get(launcher).reload(itemInfo);
                    };
                    Snackbar.show(launcher, R.string.item_hidden, R.string.undo, null, onUndoClicked);
                }
                AbstractFloatingView.closeAllOpenViews(launcher);
            };
        }
    }

    public static class UnhideApp extends SystemShortcut<Launcher> {
        public UnhideApp() {
            super(R.drawable.ic_eye_unhide, R.string.show_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                Launcher launcher, ItemInfo itemInfo) {
            boolean canHide = canHide(itemInfo);
            boolean isHidden = HiddenAppsDatabase.isHidden(launcher, itemInfo);
            if (!isHidden || !canHide) {
                return null;
            }
            return createOnClickListener(launcher, itemInfo);
        }

        public View.OnClickListener createOnClickListener(
                Launcher launcher, ItemInfo itemInfo) {
            return view -> {
                boolean isHidden = HiddenAppsDatabase.isHidden(launcher, itemInfo);
                HiddenAppsDatabase.setHidden(launcher, itemInfo, !isHidden);
                UnreadSession.getInstance(launcher).forceUpdate(); // Show or hide notifications.
                AppReloader.get(launcher).reload(itemInfo);

                if (!isHidden) {
                    Runnable onUndoClicked = () -> {
                        HiddenAppsDatabase.setHidden(launcher, itemInfo, false);
                        UnreadSession.getInstance(launcher).forceUpdate();
                        AppReloader.get(launcher).reload(itemInfo);
                    };
                    Snackbar.show(launcher, R.string.item_hidden, R.string.undo, null, onUndoClicked);
                }
                AbstractFloatingView.closeAllOpenViews(launcher);
            };
        }
    }
}
