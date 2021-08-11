package lhg.gitnotes.note.todo;

import java.io.Serializable;
import java.util.Objects;

public class TodoEntity implements Serializable {
    public static final long serialVersionUID = 0L;
    public String uuid;
    public long time;
    public String content;
    public long checkTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoEntity that = (TodoEntity) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    public boolean isChecked() {
        return checkTime > 0;
    }
}