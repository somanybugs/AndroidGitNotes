package lhg.gitnotes.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import lhg.gitnotes.app.AppBaseActivity;
import lhg.gitnotes.R;
import lhg.gitnotes.utils.AlipayUtil;
import lhg.gitnotes.ui.view.WeixinQrPayDialog;

import lhg.common.utils.SettingUI;
import lhg.common.utils.ToastUtil;
import lhg.common.utils.Utils;
import lhg.common.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;


public class SettingActivity extends AppBaseActivity {

    String qq = "976397296";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setSupportActionBar(findViewById(R.id.toolbar));
        showPrevArrowOnActionBar();

        List<View> itemViews = new ArrayList<>();
        itemViews.add(SettingUI.section(this));
        itemViews.add(SettingUI.click2H(this, "版本号", Utils.getAppVersionName(this), null));
        itemViews.add( SettingUI.click2H(this, "QQ交流群", qq, v -> {
            Utils.copy2Clipboard(getActivity(), qq);
            ToastUtil.show(getActivity(), "QQ群号码已经复制");
        }));
        ViewUtils.addItemViewsToLinearLayout(findViewById(R.id.ll_content), itemViews, false, true, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_goodpay) {
            goodPay();
        }
        return super.onOptionsItemSelected(item);
    }

    private void goodPay() {
        new AlertDialog.Builder(this)
                .setTitle("感谢您的支持")
                .setMessage("作为个人开发者,您的支持就是我们最大的动力,您的好评和打赏将会帮助我们将App做得越来越好!")
                .setPositiveButton("支付宝", (dialog, which) -> AlipayUtil.startAlipayClient(getActivity(), "fkx14109xgs4iyiav8nqxf1"))
                .setNegativeButton("微信", (dialogOut, which) -> {
                    ((AlertDialog)dialogOut).hide();
                    WeixinQrPayDialog weixin = new WeixinQrPayDialog(getActivity());
                    weixin.setOnDismissListener(dialog -> {
                        if (!((WeixinQrPayDialog)dialog).hasJumpToWeixin) {
                            ((AlertDialog)dialogOut).show();
                        }
                    });
                    weixin.show();
                })
                .setNeutralButton("好评", (dialog, which) -> launchAppDetail())
                .show();
    }

    public void launchAppDetail() {
        try {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}