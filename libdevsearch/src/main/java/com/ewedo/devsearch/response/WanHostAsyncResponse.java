package com.ewedo.devsearch.response;

import android.util.SparseArray;

/**
 * Created by fozei on 17-11-7.
 */

interface WanHostAsyncResponse {

    /**
     * Delegate to handle integer outputs
     *
     * @param output
     */
    void processFinish(int output);

    /**
     * Delegate to handle boolean outputs
     *
     * @param output
     */
    void processFinish(boolean output);

    /**
     * Delegate to handle Map outputs
     *
     * @param output
     */
    void processFinish(SparseArray<String> output);
}

