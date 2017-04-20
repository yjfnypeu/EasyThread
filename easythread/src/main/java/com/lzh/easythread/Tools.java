package com.lzh.easythread;

final class Tools {

    static boolean isAndroid;

    /**
     * Reset thread name and set a UnCaughtExceptionHandler to wrap callback to notify user when occurs a exception
     * @param thread The thread who should be reset.
     * @param name  non-null, thread name
     * @param callback a callback to notify user.
     */
    static void resetThread(Thread thread, String name, final Callback callback) {
        thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                if (callback != null) {
                    callback.onError(t,e);
                }
            }
        });
        thread.setName(name);
    }

    static boolean isEmpty(CharSequence data) {
        return data == null || data.length() == 0;
    }

    static {
        try {
            Class.forName("android.os.Build");
            isAndroid = true;
        } catch (Exception e) {
            isAndroid = false;
        }
    }
}