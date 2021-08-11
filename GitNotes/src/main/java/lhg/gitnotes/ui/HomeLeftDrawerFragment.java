package lhg.gitnotes.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import lhg.gitnotes.app.AppBaseFragment;
import lhg.gitnotes.R;
import lhg.gitnotes.git.GitConfig;
import lhg.gitnotes.git.GitContext;
import lhg.gitnotes.git.ui.GitRepoAddActivity;

import lhg.common.adapter.RecyclerClickAdapter;
import lhg.common.utils.DrawableUtils;
import lhg.common.utils.FileUtils;
import lhg.common.utils.ToastUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeLeftDrawerFragment extends AppBaseFragment {

    TextView tvEmptyInDrawer;
    RecyclerView recyclerView;
    MyAdapter adapter;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_left_drawer, null);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvEmptyInDrawer = findViewById(R.id.tvEmptyInDrawer);
        recyclerView = findViewById(R.id.recyclerView);
        initRecyclerView();
        adapter.setOnItemClickListener((adapter, data, holder) -> {
            closeDrawer();
            if (!GitConfig.isEquals(data, GitContext.instance().getGitConfig().getValue())) {
                GitContext.instance().getGitConfig().postValue(data);
            }
        });

        findViewById(R.id.ivAddRepo).setOnClickListener(v -> {
            closeDrawer();
            startActivity(new Intent(getContext(), GitRepoAddActivity.class));
        });
        findViewById(R.id.tvSetting).setOnClickListener(v -> {
            closeDrawer();
            startActivity(new Intent(getContext(), SettingActivity.class));
        });

        GitContext.instance().getAllGitConfigs().observe(getViewLifecycleOwner(), gitConfigs -> initContents(gitConfigs));
        GitContext.instance().getGitConfig().observe(getViewLifecycleOwner(), gc -> adapter.setCurrent(gc));
        initContents(GitContext.instance().getAllGitConfigs().getValue());
        adapter.setCurrent(GitContext.instance().getGitConfig().getValue());
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter = new MyAdapter());
        Drawable divider = getResources().getDrawable(R.drawable.divider_password_recycler);
        DividerItemDecoration dividerItem = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        dividerItem.setDrawable(divider);
        recyclerView.addItemDecoration(dividerItem);
    }

    private void initContents(List<GitConfig> gitConfigs) {
        if (gitConfigs == null || gitConfigs.isEmpty()) {
            tvEmptyInDrawer.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }
        tvEmptyInDrawer.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        adapter.setDatas(gitConfigs);
    }

    static void deleteNoteRepo(Context context, GitConfig gitConfig) {
        Completable.fromAction(() -> {
            FileUtils.delete(new File(gitConfig.getRootDir()));
            GitContext.instance().loadAllRepos();
            GitContext.instance().loadCurrent();
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> ToastUtil.show(context, context.getString(R.string.delete) + " " + context.getString(R.string.success)),
                        throwable -> new AlertDialog.Builder(context)
                                .setTitle(R.string.error)
                                .setMessage(throwable.getLocalizedMessage())
                                .setPositiveButton(android.R.string.ok, null)
                                .show());
    }

    static void showDeleteNoteRepo(Context context, GitConfig gitConfig) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.confirm_delete_repo))
                .setMessage(gitConfig.getRepoName() + "\n" + gitConfig.getUrl())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteNoteRepo(context, gitConfig))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static class VH extends RecyclerView.ViewHolder {

        TextView tvTitle;
        TextView tvDetail;
        GitConfig gitConfig;

        public VH(@NonNull @NotNull ViewGroup parent) {
            super(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note_repo, parent, false));
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            itemView.setBackground(DrawableUtils.listItemBackgroundPrimary(parent.getContext()));
            itemView.findViewById(R.id.ivMore).setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                popupMenu.getMenu().add(v.getContext().getString(R.string.delete));
                popupMenu.setOnMenuItemClickListener(item -> {
                    showDeleteNoteRepo(v.getContext(), gitConfig);
                    return true;
                });
                popupMenu.show();
            });
        }

        public void update(GitConfig gc, boolean isOpen) {
            this.gitConfig = gc;
            tvTitle.setText(gc.getRepoName());
            tvDetail.setText(gc.getUrl());
            itemView.setSelected(isOpen);
        }
    }


    private void closeDrawer() {
        findCallback(CloseCallback.class).closeDrawer();
    }

    private static class MyAdapter extends RecyclerClickAdapter<GitConfig, VH> {

        List<GitConfig> datas;
        GitConfig current;

        public void setDatas(List<GitConfig> datas) {
            this.datas = datas;
            notifyDataSetChanged();
        }

        public void setCurrent(GitConfig current) {
            this.current = current;
            notifyDataSetChanged();
        }

        @Override
        public GitConfig getItem(int postion) {
            return datas.get(postion);
        }

        @NonNull
        @NotNull
        @Override
        public VH onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            return new VH(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull HomeLeftDrawerFragment.VH holder, int position) {
            GitConfig gc = getItem(position);
            holder.update(gc, GitConfig.isEquals(current, gc));
        }

        @Override
        public int getItemCount() {
            return datas != null ? datas.size() : 0;
        }
    }

    public interface CloseCallback {
        void closeDrawer();
    }

}
