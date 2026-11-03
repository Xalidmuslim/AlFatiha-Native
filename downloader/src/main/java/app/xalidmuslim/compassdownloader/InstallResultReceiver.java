package app.xalidmuslim.compassdownloader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.widget.Toast;

/** Handles the platform's user approval screen; no automatic silent install. */
public final class InstallResultReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent result) {
        int status = result.getIntExtra(PackageInstaller.EXTRA_STATUS,
                PackageInstaller.STATUS_FAILURE);
        if(status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm;
            if(Build.VERSION.SDK_INT >= 33) {
                confirm = result.getParcelableExtra(Intent.EXTRA_INTENT, Intent.class);
            } else {
                @SuppressWarnings("deprecation")
                Intent legacy = result.getParcelableExtra(Intent.EXTRA_INTENT);
                confirm = legacy;
            }
            if(confirm != null) {
                confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    context.startActivity(confirm);
                } catch(Exception error) {
                    Toast.makeText(context, "Откройте загрузчик и повторите установку.",
                            Toast.LENGTH_LONG).show();
                }
            }
        } else if(status == PackageInstaller.STATUS_SUCCESS) {
            Toast.makeText(context, "«Компас сердца» установлен.",
                    Toast.LENGTH_LONG).show();
        } else {
            String message = result.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);
            Toast.makeText(context,
                    "Не удалось установить: " + (message == null ? status : message),
                    Toast.LENGTH_LONG).show();
        }
    }
}
