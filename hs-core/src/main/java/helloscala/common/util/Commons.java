package helloscala.common.util;

import java.lang.management.ManagementFactory;

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-08-03.
 */
public class Commons {

    /**
     * 获取当前进程 pid
     */
    public static long getPid() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        return Long.valueOf(jvmName.split("@")[0]);
    }

}
