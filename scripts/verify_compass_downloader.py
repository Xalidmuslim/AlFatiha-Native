#!/usr/bin/env python3
"""Regression and artifact checks for the standalone resumable downloader."""
from pathlib import Path
from hashlib import sha256
import sys

ROOT = Path(__file__).resolve().parents[1]
src = (ROOT / "downloader/src/main/java/app/xalidmuslim/compassdownloader/DownloadActivity.java").read_text()
receiver = (ROOT / "downloader/src/main/java/app/xalidmuslim/compassdownloader/InstallResultReceiver.java").read_text()
manifest = (ROOT / "downloader/src/main/AndroidManifest.xml").read_text()
settings = (ROOT / "settings.gradle").read_text()

EXPECTED = "889f57814f8c688541f5549e02bd07429061a04d177fcf69f23cdd7bb70dc51b"
TOTAL = 10_934_336
SIZE = 524_288

assert "include ':downloader'" in settings
assert 'android.permission.INTERNET' in manifest
assert 'android.permission.REQUEST_INSTALL_PACKAGES' in manifest
assert 'android:allowBackup="false"' in manifest
assert "new RandomAccessFile(target,\"rw\")" in src
assert 'conn.setRequestProperty("Range","bytes="+offset+"-")' in src
assert "file.setLength(beginning)" in src
assert EXPECTED in src
assert "new File(getFilesDir()" in src
assert "verifyHash(target)" in src and "session.commit(pi.getIntentSender())" in src
assert "STATUS_PENDING_USER_ACTION" in receiver
assert "context.startActivity(confirm)" in receiver
print("Downloader safety and resume contracts: OK")

if len(sys.argv) > 1:
    apk = Path(sys.argv[1])
    chunks = Path(sys.argv[2])
    raw = apk.read_bytes()
    assert len(raw) == TOTAL, (len(raw), TOTAL)
    assert sha256(raw).hexdigest() == EXPECTED
    parts = list(sorted(chunks.glob("Compass-Full-Morph-*.part")))
    assert len(parts) == (TOTAL + SIZE - 1) // SIZE, len(parts)
    assert b"".join(p.read_bytes() for p in parts) == raw
    assert all(1 <= p.stat().st_size <= SIZE for p in parts)
    print(f"APK round-trip: {len(parts)} checked chunks, {len(raw):,} bytes, SHA256 {EXPECTED}")
