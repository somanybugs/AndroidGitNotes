package lhg.gitnotes.note.bill;

import java.io.Serializable;
import java.util.Objects;

public class BillEntity implements Serializable {
    public static final long serialVersionUID = 0L;
    public String uuid;
    public String time;
    public String name;
    public String money;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillEntity that = (BillEntity) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
