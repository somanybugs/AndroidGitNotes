package lhg.gitnotes.utils;

import lhg.common.utils.NamedThreadFactory;

import java.util.concurrent.Executors;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.internal.schedulers.ExecutorScheduler;

public class ThreadUtils {

    public static final Scheduler FILE_SCHEDULER = new ExecutorScheduler(Executors.newSingleThreadExecutor(new NamedThreadFactory("FileThread")), true, true);

}
