package lhg.gitnotes.app;

import lhg.common.utils.FileUtils;

import java.util.Arrays;
import java.util.List;

public class AppConstant {

    public static final int Password_Max_Error_Count = 5;
    public static final int Password_Lock_Seconds = 60;
    public static final int Password_Clip_AutoClear_Seconds = 15;
    public static final int CloseAllActivity_After_GotoBackground_Seconds = 15;

    public static class FileSuffix {
        public static final String MD = ".md";
        public static final String TXT = ".txt";
        public static final String PWD = ".pwd";
        public static final String TODO = ".todo";
        public static final String BILL = ".bill";
        public static final String FOLDER = "folder";

        static final List<String> SupportSuffixs = Arrays.asList(
                AppConstant.FileSuffix.MD, AppConstant.FileSuffix.TXT,
                AppConstant.FileSuffix.PWD, AppConstant.FileSuffix.TODO,
                AppConstant.FileSuffix.BILL
        );
        public static boolean support(String fileName) {
            String suffix = "." + FileUtils.getSuffix(fileName).toLowerCase();
            return SupportSuffixs.contains(suffix);
        }
    }

}
