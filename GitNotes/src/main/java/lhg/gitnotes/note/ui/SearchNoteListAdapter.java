package lhg.gitnotes.note.ui;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import lhg.gitnotes.R;
import lhg.gitnotes.note.FileEntity;
import lhg.gitnotes.note.FolderEntity;
import lhg.gitnotes.note.NoteEntity;

public class SearchNoteListAdapter extends BaseNoteListAdapter {
    final String repoDir;

    public SearchNoteListAdapter(String repoDir) {
        this.repoDir = repoDir;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 1) {
            return new NoteVH2(parent, repoDir);
        } else {
            return new FolderVH2(parent, repoDir);
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((BaseVH) holder).update(getItem(position));
    }

    @Override
    public int getItemCount() {
        return datas == null ? 0 : datas.size();
    }

    @Override
    public FileEntity getItem(int postion) {
        return datas.get(postion);
    }


    public static class NoteVH2 extends NoteVH {
        private String repoDir;
        private TextView tvPath;

        public NoteVH2(@NonNull ViewGroup parent, String repoDir) {
            super(parent, R.layout.item_note_search);
            this.repoDir = repoDir;
        }

        public NoteVH2(@NonNull View itemView) {
            super(itemView);
            tvPath = itemView.findViewById(R.id.tvPath);

        }

        @Override
        public void update(NoteEntity data) {
            super.update(data);
            tvPath.setText(data.file.getParent().substring(repoDir.length()));
        }
    }

    public static class FolderVH2 extends FolderVH {
        private String repoDir;
        private TextView tvPath;

        public FolderVH2(@NonNull ViewGroup parent, String repoDir) {
            super(parent, R.layout.item_folder);
            this.repoDir = repoDir;
        }

        public FolderVH2(@NonNull View itemView) {
            super(itemView);
            tvPath = itemView.findViewById(R.id.tvPath);
        }

        @Override
        public void update(FolderEntity data) {
            super.update(data);
            tvPath.setText(data.file.getAbsolutePath().substring(repoDir.length()));
        }
    }

}
