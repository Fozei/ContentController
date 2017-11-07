package com.ewedo.devsearch.callback;

import java.util.List;

/**
 * Created by fozei on 17-11-7.
 */

public interface OnGetResultCallback {
    void onGetResult(List<String> list);

    void onError(Exception e);

    void onProcessChange(int i);
}
