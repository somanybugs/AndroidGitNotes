package lhg.gitnotes.note;

import java.io.Serializable;

public class LockFileEntity implements Serializable {
    public String key;//encrypted by password(user input) 256bit. hex;
    public String signature;//encrypted random by key(128)/aes, hex
    public String random;// hex
}
