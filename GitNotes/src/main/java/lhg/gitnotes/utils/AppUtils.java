package lhg.gitnotes.utils;

import java.io.File;
import java.util.List;

public class AppUtils {




    public static File[] pathToFiles(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            return null;
        }
        File files[] = new File[paths.size()];
        for (int i = 0; i < files.length; i++) {
            files[i] = new File(paths.get(i));
        }
        return files;
    }

}
