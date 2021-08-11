package lhg.gitnotes.note.pwd.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import lhg.gitnotes.R;
import lhg.common.utils.ToastUtil;
import lhg.common.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 *
 *
 * Author: liuhaoge
 * Date: 2020/10/27 14:04
 * Note:
 */
public class GenPasswordDialog extends AlertDialog {

    TextView tv_password;
    SeekBar seekBar;
    static int MIN_LEN = 4;
    static int MAX_LEN = 25;
    final List<CheckBox> checkBoxes = new ArrayList<>();
    int passLen = 6;


    public GenPasswordDialog(@NonNull Context context) {
        super(context);
        View view = View.inflate(getContext(), R.layout.dialog_gen_password, null);
        setView(view);

        setButton(BUTTON_POSITIVE, context.getString(android.R.string.copy), (dialog, which) -> doCopy());
        setButton(BUTTON_NEUTRAL, context.getString(R.string.refresh), (dialog, which) -> doRefresh());
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), (dialog, which) -> {});

        checkBoxes.add(view.findViewById(R.id.cb_low_letter));
        checkBoxes.add(view.findViewById(R.id.cb_up_letter));
        checkBoxes.add(view.findViewById(R.id.cb_number));
        checkBoxes.add(view.findViewById(R.id.cb_special));

        CompoundButton.OnCheckedChangeListener cbL = (buttonView, isChecked) -> {
            if ((!isChecked) && checkedLetterTypeCount() == 0) {
                buttonView.setChecked(true);
                ToastUtil.show(getContext(), "请至少选中一种字符类型");
            } else {
                doRefresh();
            }
        };
        for (CheckBox cb : checkBoxes) {
            cb.setOnCheckedChangeListener(cbL);
        }

        tv_password = view.findViewById(R.id.tv_password);
        seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int index = Math.round ((MAX_LEN - MIN_LEN) * seekBar.getProgress()/seekBar.getMax());
                passLen = MIN_LEN + index;
                onPassLenChange(passLen);
            }
        });

        onPassLenChange(passLen);
    }


    private void onPassLenChange(int passLen) {
        setTitle("随机生成" +passLen + "位密码 ");
        float block = seekBar.getMax() * 1f/(MAX_LEN - MIN_LEN);
        int p = (int) ((passLen-MIN_LEN) * block);
        seekBar.setProgress(p);
        doRefresh();
    }

    @Override
    public void show() {
        super.show();
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void doCopy() {
        Utils.copy2Clipboard(getContext(), tv_password.getText().toString());
        dismiss();
    }

    private int checkedLetterTypeCount() {
        int checkCount = 0;
        for (CheckBox cb : checkBoxes) {
            if (cb.isChecked()) {
                checkCount++;
            }
        }
        return checkCount;
    }

    String special = "~!@#$%^&*(()_+=-,./<>?;':";
    String letters = "abcdefghijklmnopqrstuvwxyz";
    private void doRefresh() {
        Random random = new Random();
        StringBuilder pass = new StringBuilder();
        while (pass.length() < passLen) {
            int i = random.nextInt(checkBoxes.size());
            if (!checkBoxes.get(i).isChecked()) {
                continue;
            }
            switch (checkBoxes.get(i).getId()) {
                case R.id.cb_low_letter:
                    pass.append(letters.charAt(random.nextInt(letters.length())));
                    break;
                case R.id.cb_up_letter:
                    pass.append(Character.toUpperCase(letters.charAt(random.nextInt(letters.length()))));
                    break;
                case R.id.cb_number:
                    pass.append(random.nextInt(10));
                    break;
                case R.id.cb_special:
                    pass.append(special.charAt(random.nextInt(special.length())));
                    break;
            }
        }
        tv_password.setText(pass);
    }
}
