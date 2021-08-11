package lhg.gitnotes.ui.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import lhg.common.utils.FileUtils;
import lhg.common.utils.ImageUtils;
import lhg.common.utils.ToastUtil;
import lhg.common.utils.Utils;

import lhg.gitnotes.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Author: liuhaoge
 * Date: 2020/11/26 13:19
 * Note:
 */
public class WeixinQrPayDialog extends AlertDialog {
    public boolean hasJumpToWeixin = false;
    InputStream inputStream;
    public WeixinQrPayDialog(@NonNull Context context) {
        super(context);
        byte[] data = FileUtils.readBuff(context.getAssets(), "wc_zanshang.jpg");
        inputStream = new ByteArrayInputStream(data);
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_weixin_qrpay);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        ImageView imageView = findViewById(R.id.iv);
        imageView.setImageBitmap(bitmap);
        findViewById(R.id.btn).setOnClickListener(v -> {
            saveImage();
        });
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        int width = (int) (Utils.screenSizeInPixel(getContext()).x * 0.8f);
        getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void saveImage() {
        try {
            inputStream.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ImageUtils.addBitmapToAlbum(getContext(), "weixin_qr_pay.jpg", inputStream, "image/jpeg");
        ToastUtil.show(getContext(), "已保存到相册");
        toWeChatScanDirect(getContext());
        dismiss();
    }

    public void toWeChatScanDirect(Context context) {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            intent.setFlags(335544320);
            intent.setAction("android.intent.action.VIEW");
            context.startActivity(intent);
            hasJumpToWeixin = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
