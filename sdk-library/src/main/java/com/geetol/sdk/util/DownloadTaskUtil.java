package com.geetol.sdk.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 下载任务工具类
 *
 * @author pslilysm
 * @since 1.0.0
 */
public class DownloadTaskUtil {

    private static final OkHttpClient sClient = new OkHttpClient();

    /**
     * 新建一个下载任务
     *
     * @param url 下载链接
     * @param out 输出文件
     * @return RxJava3的Observable, 会回调百分比进度
     */
    public static Observable<Integer> newTask(String url, File out) {
        return Observable.create(emitter -> {
            FileUtils.forceMkdirParent(out);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try (Response response = sClient.newCall(request).execute();
                 InputStream is = response.body().byteStream();
                 OutputStream os = new FileOutputStream(out)) {
                byte[] buffer = new byte[8192];
                long totalCount = response.body().contentLength();
                long count = 0;
                int n;
                int progress = 0;
                while (IOUtils.EOF != (n = is.read(buffer))
                        && !Thread.currentThread().isInterrupted()) {
                    os.write(buffer, 0, n);
                    count += n;
                    int curProgress = Integer.parseInt(String.valueOf(count * 100 / totalCount));
                    if (curProgress > progress) {
                        progress = curProgress;
                        emitter.onNext(progress);
                    }
                }
            }
            emitter.onComplete();
        });
    }

}
