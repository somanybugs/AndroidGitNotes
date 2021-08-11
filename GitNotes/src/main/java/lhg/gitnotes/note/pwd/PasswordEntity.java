package lhg.gitnotes.note.pwd;

import java.io.Serializable;
import java.util.Objects;

public class PasswordEntity implements Serializable {
    public static final long serialVersionUID = 0L;
    public String uuid;
    public long time;
    public String name;
    public String account;
    public String password;
    public String note;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordEntity that = (PasswordEntity) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}