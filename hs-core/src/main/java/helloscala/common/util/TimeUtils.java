package helloscala.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-08-04.
 */
public abstract class TimeUtils {

    public static Date toDate(LocalDateTime ldt) {
        return Date.from(toInstant(ldt));
    }

    public static long toEpochMilli(LocalDateTime ldt) {
        return toInstant(ldt).toEpochMilli();
    }

    public static Instant toInstant(LocalDateTime ldt) {
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }
}
