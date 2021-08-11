package lhg.gitnotes.note.pwd;

import android.text.InputType;


import java.io.Serializable;

import lhg.entityui.annotation.InputView;
import lhg.entityui.annotation.ViewProps;
import lhg.entityui.validator.NotBlank;

public class EditPasswordEntity implements Serializable {

    @NotBlank(message = "名称不能为空")
    @ViewProps(name = "名称", sort = 1)
    @InputView(hint = "在此输入")
    public String name;

    @ViewProps(name = "账号", sort = 2)
    @InputView(hint = "在此输入", inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
    public String account;

    @NotBlank(message = "密码不能为空")
    @ViewProps(name = "密码", sort = 3)
    @InputView(hint = "在此输入", inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
    public String password;

    @ViewProps(name = "备注", sort = 4)
    @InputView(enterKey = InputView.WrapLine, hint = "在此输入")
    public String note;

    public void from(PasswordEntity item) {
        EditPasswordEntity entity = this;
        entity.account = item.account;
        entity.password = item.password;
        entity.name = item.name;
        entity.note = item.note;
    }

    public void to(PasswordEntity item) {
        EditPasswordEntity entity = this;
        entity.account = item.account;
        entity.password = item.password;
        entity.name = item.name;
        entity.note = item.note;
    }
}