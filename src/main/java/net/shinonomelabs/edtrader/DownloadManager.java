package net.shinonomelabs.edtrader;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public abstract class DownloadManager extends Thread {
  private final String url_str;
  private volatile boolean started = false;
  private volatile boolean finished = false;
  private byte[] data;
  private volatile int amountDownloaded = 0;

  public DownloadManager(String url) {
    this.url_str = url;
  }

  public int getAmountDownloaded() {
    return this.amountDownloaded;
  }

  public boolean isFinished() {
    return finished;
  }

  public abstract void whenDownloaded();

  public byte[] getData() {
    return data;
  }

  @Override
  public void run() {
    URL url = null;
    try {
      url = new URL(this.url_str);
    } catch (MalformedURLException murle) {
      // TODO handle
      murle.printStackTrace();
    }

    if (url == null) return;

    try {
      HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
      conn.addRequestProperty("Accept-Encoding", "gzip");

      InputStream is = conn.getInputStream();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      byte[] buf = new byte[1024];
      int n;

      this.started = true;
      while ((n = is.read(buf)) != -1) {
        baos.write(buf, 0, n);
        this.amountDownloaded += n;
      }

      // decompress
      // TODO rewrite, this is abysmal
      if (conn.getHeaderField("Content-Encoding").equals("gzip")) {
        GZIPInputStream decompressor =
            new GZIPInputStream(new ByteArrayInputStream(baos.toByteArray()));
        this.data = decompressor.readAllBytes();
      }

      this.finished = true;
    } catch (IOException ioe) {
      // TODO handle
      ioe.printStackTrace();
    } finally {
      whenDownloaded();
    }
  }
}
