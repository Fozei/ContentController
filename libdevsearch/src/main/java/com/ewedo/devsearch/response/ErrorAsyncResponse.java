package com.ewedo.devsearch.response;

/**
 * Created by fozei on 17-11-7.
 */

interface ErrorAsyncResponse {

    /**
     * Delegate to bubble up errors
     *
     * @param output
     */
    <T extends Throwable> void processFinish(T output);
}
