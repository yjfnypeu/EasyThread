package com.lzh.easythread;

/**
 * A call to notify user when thread task occurs an error or completed
 */
public interface Callback {

    /**
     * This method will be invoked when thread has been occurs an error.
     * @param thread The thread who has been occurs an error
     * @param t The exception
     */
    void onError (Thread thread, Throwable t);

    /**
     * notify user to know that it completed.
     * @param thread The running thread
     */
    void onCompleted (Thread thread);
}
