package app.xalidmuslim.compassdownloader;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/** A tiny native downloader for users whose browser truncates 11 MB APK downloads.
 * No analytics, ads, accounts, extra permissions, or external-storage access.
 * It never installs unverified data: exact length AND SHA-256 must match.
 */
public final class DownloadActivity extends Activity {
    private static final long TOTAL = 10934336L;
    private static final int CHUNK = 524288;
    private static final int PARTS = (int)((TOTAL + CHUNK - 1) / CHUNK);
    private static final String HASH =
            "889f57814f8c688541f5549e02bd07429061a04d177fcf69f23cdd7bb70dc51b";
    private static final String REPO = "Xalidmuslim/AlFatiha-Native";
    private static final String TAG = "compass-full-morph-motion-20261103";
    private static final String TARGET_PACKAGE =
            "app.alfatiha.tafsir.compassfullmorph1103";

    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private final AtomicBoolean running = new AtomicBoolean();
    private TextView message;
    private TextView detail;
    private ProgressBar progress;
    private Button download;
    private Button install;
    private File target;
    private volatile boolean verified;
    private long lastProgressTime;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        target = new File(getFilesDir(), "Compass-Heart-Full-Morph-Motion.apk");
        makeUi();
        download.setOnClickListener(v -> startDownload());
        install.setOnClickListener(v -> installVerified());
        worker.execute(() -> {
            try {
                if(target.length() == TOTAL && verifyHash(target)) {
                    verified = true;
                    post("APK уже скачан и проверен. Можно установить.", TOTAL, true);
                } else {
                    post("Нажмите «Скачать / продолжить». Прогресс сохранится.", target.length(), false);
                }
            } catch(Exception ex) {
                post("Нажмите «Скачать / продолжить».", target.length(), false);
            }
        });
    }

    private int dp(float v) {
        return (int)(getResources().getDisplayMetrics().density * v + 0.5f);
    }

    private void makeUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setPadding(dp(24), dp(30), dp(24), dp(24));
        root.setBackgroundColor(Color.rgb(247, 244, 238));

        TextView title = new TextView(this);
        title.setText("Компас сердца");
        title.setTextSize(27);
        title.setTextColor(Color.rgb(38, 56, 46));
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(56)));

        TextView subtitle = new TextView(this);
        subtitle.setText("Full Morph Motion — надёжная загрузка");
        subtitle.setTextSize(15);
        subtitle.setTextColor(Color.rgb(73, 91, 77));
        subtitle.setGravity(Gravity.CENTER);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(48)));

        message = new TextView(this);
        message.setText("Проверяю сохранённый файл…");
        message.setTextSize(16);
        message.setTextColor(Color.rgb(30, 48, 39));
        message.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams msg = new LinearLayout.LayoutParams(-1, dp(88));
        root.addView(message, msg);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(1000);
        progress.setProgress(0);
        root.addView(progress, new LinearLayout.LayoutParams(-1, dp(16)));

        detail = new TextView(this);
        detail.setText("0 из 10,9 МБ");
        detail.setGravity(Gravity.CENTER);
        detail.setTextSize(14);
        detail.setTextColor(Color.rgb(100, 109, 103));
        root.addView(detail, new LinearLayout.LayoutParams(-1, dp(55)));

        download = new Button(this);
        download.setText("Скачать / продолжить");
        download.setAllCaps(false);
        root.addView(download, new LinearLayout.LayoutParams(-1, dp(57)));

        install = new Button(this);
        install.setText("Установить приложение");
        install.setAllCaps(false);
        install.setEnabled(false);
        LinearLayout.LayoutParams ins = new LinearLayout.LayoutParams(-1, dp(57));
        ins.topMargin = dp(12);
        root.addView(install, ins);

        TextView foot = new TextView(this);
        foot.setText("Файл загружается частями по 512 КБ. Если сеть оборвётся, "
                + "нажмите «Продолжить» — уже полученные данные останутся. "
                + "Установка разрешена только после проверки SHA-256.");
        foot.setTextSize(12);
        foot.setTextColor(Color.rgb(111, 117, 108));
        foot.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ft = new LinearLayout.LayoutParams(-1, -2);
        ft.topMargin = dp(18);
        root.addView(foot, ft);

        setContentView(root);
    }

    private void post(String status, long bytes, boolean done) {
        final long clamped = Math.max(0, Math.min(TOTAL, bytes));
        main.post(() -> {
            if(isFinishing() || isDestroyed()) return;
            message.setText(status);
            progress.setProgress((int)(clamped * 1000 / TOTAL));
            detail.setText(String.format(Locale.US, "%.1f из %.1f МБ",
                    clamped / 1000000f, TOTAL / 1000000f));
            install.setEnabled(done);
            download.setEnabled(!running.get());
            download.setText(done ? "Скачать заново" : "Скачать / продолжить");
        });
    }

    private void startDownload() {
        if(!running.compareAndSet(false,true)) return;
        verified = false;
        download.setEnabled(false);
        install.setEnabled(false);
        lastProgressTime = 0;
        worker.execute(() -> {
            try {
                fetchParts();
                if(target.length() != TOTAL || !verifyHash(target))
                    throw new IOException("Проверка контрольной суммы не пройдена. "
                            + "Файл повреждён или отличается от оригинала.");
                verified = true;
                post("Загрузка завершена. APK проверен, можно устанавливать.", TOTAL, true);
            } catch(Exception e) {
                post("Загрузка остановилась: " + e.getMessage()
                        + " Нажмите «Продолжить» для повтора.", target.length(), false);
            } finally {
                running.set(false);
                main.post(() -> {
                    if(!isFinishing() && !isDestroyed()) download.setEnabled(true);
                });
            }
        });
    }

    private String partUrl(int number,int mirror) {
        String filename = String.format(Locale.US, "Compass-Full-Morph-%02d.part", number);
        if(mirror == 0)
            return "https://raw.githubusercontent.com/" + REPO
                    + "/apk-mobile-direct/" + filename;
        return "https://github.com/" + REPO + "/releases/download/" + TAG + "/"
                + filename;
    }

    private void fetchParts() throws Exception {
        try(RandomAccessFile file = new RandomAccessFile(target,"rw")) {
            if(file.length() > TOTAL) file.setLength(0);
            for(int n=0; n<PARTS; n++) {
                long beginning = (long)n*CHUNK;
                long ending = Math.min(TOTAL, beginning+CHUNK);
                if(file.length() >= ending) continue;
                // Always keep previously verified-length pieces. A partial
                // piece resumes with HTTP Range, or restarts only this piece.
                if(file.length() < beginning) file.setLength(beginning);
                int attempts = 0;
                while(file.length() < ending) {
                    if(attempts >= 8)
                        throw new IOException("часть " + (n+1) + "/" + PARTS
                                + " не загрузилась после повторов");
                    long offset = file.length()-beginning;
                    HttpURLConnection conn = null;
                    try {
                        conn = (HttpURLConnection)new URL(partUrl(n,attempts%2)).openConnection();
                        conn.setConnectTimeout(13000);
                        conn.setReadTimeout(16000);
                        conn.setRequestProperty("Accept-Encoding","identity");
                        conn.setRequestProperty("User-Agent","Compass-Heart-Resumable-Downloader/1.0");
                        if(offset > 0)
                            conn.setRequestProperty("Range","bytes="+offset+"-");
                        int response = conn.getResponseCode();
                        if(response != 200 && response != 206)
                            throw new IOException("HTTP "+response);
                        if(response == 200 && offset > 0) {
                            // Server ignored Range: restart this 512KB part.
                            file.setLength(beginning);
                            offset = 0;
                        }
                        file.seek(beginning+offset);
                        long remaining = ending-(beginning+offset);
                        try(java.io.InputStream in = conn.getInputStream()) {
                            byte[] buffer = new byte[16*1024];
                            while(remaining > 0) {
                                int r=in.read(buffer,0,(int)Math.min(buffer.length,remaining));
                                if(r < 0) break;
                                if(r == 0) continue;
                                file.write(buffer,0,r);
                                remaining-=r;
                                long now=android.os.SystemClock.uptimeMillis();
                                if(now-lastProgressTime>240) {
                                    lastProgressTime=now;
                                    post("Скачиваю часть "+(n+1)+" из "+PARTS+"…",
                                            file.length(),false);
                                }
                            }
                        }
                        if(file.length() < ending)
                            throw new IOException("неполная часть");
                    } catch(IOException error) {
                        attempts++;
                        if(attempts >= 8) throw error;
                        Thread.sleep(550L);
                    } finally {
                        if(conn != null) conn.disconnect();
                    }
                }
            }
        }
    }

    private boolean verifyHash(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try(FileInputStream in = new FileInputStream(file)) {
            byte[] bytes=new byte[64*1024];
            int n;
            while((n=in.read(bytes)) != -1) digest.update(bytes,0,n);
        }
        StringBuilder actual = new StringBuilder();
        for(byte b:digest.digest())actual.append(String.format(Locale.US,"%02x",b&0xff));
        return HASH.equals(actual.toString());
    }

    private void installVerified() {
        if(!verified || running.get()) {
            post("Сначала нужно полностью скачать и проверить APK.", target.length(),false);
            return;
        }
        if(!getPackageManager().canRequestPackageInstalls()) {
            message.setText("Разрешите установку приложений для «Загрузчика Компаса», "
                    + "затем вернитесь и снова нажмите «Установить».");
            Intent settings = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                    Uri.parse("package:"+getPackageName()));
            startActivity(settings);
            return;
        }
        install.setEnabled(false);
        worker.execute(() -> {
            try {
                if(target.length()!=TOTAL || !verifyHash(target))
                    throw new IOException("APK не прошёл проверку SHA-256");
                PackageInstaller installer=getPackageManager().getPackageInstaller();
                PackageInstaller.SessionParams params=new PackageInstaller.SessionParams(
                        PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                params.setAppPackageName(TARGET_PACKAGE);
                params.setSize(TOTAL);
                int id=installer.createSession(params);
                try(PackageInstaller.Session session=installer.openSession(id);
                    FileInputStream in=new FileInputStream(target)) {
                    try(OutputStream out=session.openWrite("base.apk",0,TOTAL)) {
                        byte[] b=new byte[64*1024];
                        int n;
                        while((n=in.read(b)) != -1)out.write(b,0,n);
                        session.fsync(out);
                    }
                    Intent result=new Intent(this,InstallResultReceiver.class);
                    PendingIntent pi=PendingIntent.getBroadcast(this,id,result,
                            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
                    session.commit(pi.getIntentSender());
                }
                post("Подтвердите установку в системном окне Android.", TOTAL,true);
            } catch(Exception e) {
                post("Установка: "+e.getMessage(), TOTAL,true);
            } finally {
                main.post(()->{
                    if(!isDestroyed())install.setEnabled(true);
                });
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        worker.shutdown();
    }
}
