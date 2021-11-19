package lhg.gitnotes.note.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;

import lhg.gitnotes.R;
import lhg.gitnotes.app.AppConstant;
import lhg.common.utils.ToastUtil;
import lhg.common.utils.Utils;

import org.jetbrains.annotations.NotNull;

public abstract class CreateNotePopMenu extends PopupMenu {

    CreateNoteOrFolderDialog dialog;
    Context context;

    public CreateNotePopMenu(@NonNull @NotNull Context context, @NonNull @NotNull View anchor) {
        super(context, anchor);
        inflate(R.menu.create_note);
        this.context = context;
        dialog = new CreateNoteOrFolderDialog(context) {
            @Override
            protected void onDialogSubmit(int itemId, String name) {
                onCreateNote(itemId, name);
            }
        };
        setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            switch (itemId) {
                case R.id.action_folder:
                    showDialog(itemId, R.string.folder, "");
                    break;
                case R.id.action_md:
                    showDialog(itemId, R.string.markdown, AppConstant.FileSuffix.MD);
                    break;
                case R.id.action_txt:
                    showDialog(itemId, R.string.txt, AppConstant.FileSuffix.TXT);
                    break;
                case R.id.action_todo:
                    showDialog(itemId, R.string.todo, AppConstant.FileSuffix.TODO);
                    break;
                case R.id.action_bill:
                    showDialog(itemId, R.string.bill, AppConstant.FileSuffix.BILL);
                    break;
                case R.id.action_pwd:
                    showDialog(itemId, R.string.password, AppConstant.FileSuffix.PWD);
                    break;
            }
            return true;
        });
    }

    private void showDialog(int itemId, int nameRes, String suffix) {
        dialog.itemId = itemId;
        dialog.setNameAndSuffix(context.getString(nameRes), suffix);
        dialog.show();
    }

    protected abstract void onCreateNote(int itemId, String name);


    private static abstract class CreateNoteOrFolderDialog extends AlertDialog {

        int itemId;
        TextView tvSuffix;
        EditText editText;


        public CreateNoteOrFolderDialog(Context context) {
            super(context);
            View view = View.inflate(getContext(), R.layout.dialog_create_note_or_folder, null);

            tvSuffix = view.findViewById(R.id.tvSuffix);
            editText = view.findViewById(R.id.editText);
            setView(view);
            setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), (dialog, which) -> doCreateType());
            setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), (dialog, which) -> {});
        }

        @Override
        public void show() {
            super.show();
            editText.setText(null);
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            editText.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            Utils.showKeyboard(editText);
        }

        public void setNameAndSuffix(String name, String suffix) {
            setTitle(getContext().getString(R.string.create) + " " + name);
            tvSuffix.setText(suffix);
        }

        @Override
        protected void onStop() {
            Utils.hideKeyboard(editText);
            super.onStop();
        }


        protected void doCreateType() {
            if (editText.length() == 0) {
                ToastUtil.show(getContext(), R.string.empty_input);
                return;
            }
            onDialogSubmit(itemId, editText.getText().toString() + tvSuffix.getText().toString());
        }

        protected abstract void onDialogSubmit(int itemId, String name);
    }

}
