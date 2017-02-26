package cn.yhq.http.upload;

import android.content.Context;

import cn.yhq.dialog.core.DialogBuilder;
import cn.yhq.dialog.core.IDialog;

/**
 * Created by Yanghuiqiang on 2016/12/23.
 */

public class DefaultOnMd5CheckListener implements OnMd5CheckListener {
    private IDialog dialog;

    public DefaultOnMd5CheckListener(Context context) {
        dialog = DialogBuilder.loadingDialog0(context).setMessage("正在进行秒传检测...").create();
    }

    @Override
    public void start() {
        dialog.show();
    }

    @Override
    public void complete() {
        dialog.dismiss();
    }

}
