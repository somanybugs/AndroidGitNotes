package lhg.gitnotes.note.pwd.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import lhg.gitnotes.R;
import lhg.gitnotes.note.pwd.PasswordEntity;
import lhg.gitnotes.utils.PasswordCopyUtils;
import lhg.common.utils.Utils;

/**
 *
 *
 * Author: liuhaoge
 * Date: 2020/10/27 14:04
 * Note:
 */
public class ViewPasswordDialog extends AlertDialog {

    private final PasswordEntity item;
    TextView tv_title;


    protected ViewPasswordDialog(@NonNull Context context, PasswordEntity item) {
        super(context);
        this.item = item;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_view_password);
        findViewById(R.id.tv_edit).setOnClickListener(v -> {
            dismiss();
            clickOnEdit();
        });
        initViews();
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        int width = (int) (Utils.screenSizeInPixel(getContext()).x * 0.8f);
        getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void show() {
        super.show();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    protected void clickOnEdit() {

    }

    private void initViews() {
        tv_title = findViewById(R.id.tvTitle);
        tv_title.setText(item.name);
        String password = null;
        if (!TextUtils.isEmpty(item.password)) {
            password =item.password;
        }
        LinearLayout ll = findViewById(R.id.ll_content);
        createItem(ll, "帐号", item.account);
        createItem(ll, "密码", password);
        if (!TextUtils.isEmpty(item.note)) {
            createItem(ll, "备注", item.note);
        }
    }

    private View createItem(LinearLayout ll, String title, String value) {
        View.inflate(getContext(), R.layout.item_password_copy, ll);
        View v = ll.getChildAt(ll.getChildCount()-1);
        v.setOnClickListener(v1 -> PasswordCopyUtils.copy(getContext(), value));
        ((TextView) v.findViewById(android.R.id.text1)).setText(title);
        ((TextView) v.findViewById(android.R.id.text2)).setText(value);
        return v;
    }



}
