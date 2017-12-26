/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-07.
 */
package helloscala.common;

import java.lang.management.ManagementFactory;

public class HSCommons {
    public static final String CONFIG_PATH_PREFIX = "helloscala";
    public static final String PERSISTENCE_PATH = CONFIG_PATH_PREFIX + ".persistence";
    public static final String CASSANDRA_PATH = PERSISTENCE_PATH + ".cassandra";
    public static final String ELASTICSEARCH_PATH = PERSISTENCE_PATH + ".elasticsearch";

    public static final int SHA256_HEX_LENGTH = 64;

    public static final int STATUS_DISABLE = 0;
    public static final int STATUS_ENABLE = 1;

    /**
     * 获取当前进程 pid
     */
    public static long getPid() {
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        return Long.valueOf(jvmName.split("@")[0]);
    }

}
