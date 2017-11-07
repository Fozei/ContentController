package com.ewedo.devsearch.callback;

import java.util.List;

/**
 * Created by fozei on 17-11-7.
 */

public interface OnGetResultCallback {
    void onGetResult(List<String> list);

    void onError();

    void processFinish(int i);
}
