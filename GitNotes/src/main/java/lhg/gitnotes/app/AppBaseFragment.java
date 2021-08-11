package lhg.gitnotes.app;

import androidx.fragment.app.Fragment;

import lhg.common.BaseFragment;
import lhg.common.view.LoadingDialog;

public class AppBaseFragment extends BaseFragment {
    LoadingDialog loadingDialog;

    public void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(getContext());
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    public void hideLoadingDialog() {
        if (loadingDialog == null) {
            return;
        }
        if (loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void onDestroyView() {
        hideLoadingDialog();
        super.onDestroyView();
    }

    protected  <T> T findCallback(Class<T> clazz) {
        Fragment parent = getParentFragment();
        while (parent != null && !clazz.isAssignableFrom(parent.getClass())) {
            parent = parent.getParentFragment();
        }
        if (parent != null && clazz.isAssignableFrom(parent.getClass())) {
            return (T) parent;
        }
        if (getActivity() != null && clazz.isAssignableFrom(getActivity().getClass())) {
            return (T) getActivity();
        }
        return null;
    }
}
