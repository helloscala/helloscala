package helloscala.nosql.cassandra;

import com.datastax.driver.core.DataType;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.exceptions.InvalidTypeException;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static com.datastax.driver.core.ParseUtils.isQuoted;
import static com.datastax.driver.core.ParseUtils.quote;
import static com.datastax.driver.core.ParseUtils.unquote;

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-21.
 */
public class LocalDateTimeCode extends TypeCodec<LocalDateTime> {
    public static final LocalDateTimeCode instance = new LocalDateTimeCode();

    LocalDateTimeCode() {
        super(DataType.timestamp(), LocalDateTime.class);
    }

    @Override
    public ByteBuffer serialize(LocalDateTime value, ProtocolVersion protocolVersion) throws InvalidTypeException {
        return value == null ? null : timestamp().serialize(new java.util.Date(value.toInstant(ZoneOffset.ofHours(8)).toEpochMilli()), protocolVersion);
    }

    @Override
    public LocalDateTime deserialize(ByteBuffer bytes, ProtocolVersion protocolVersion) throws InvalidTypeException {
        return bytes == null || bytes.remaining() == 0 ? null : LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(timestamp().deserialize(bytes, protocolVersion).getTime()), ZoneOffset.ofHours(8));
    }

    @Override
    public LocalDateTime parse(String value) throws InvalidTypeException {
        if (value == null || value.isEmpty() || value.equalsIgnoreCase("NULL"))
            return null;
        if (isQuoted(value))
            value = unquote(value);
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_DATE_TIME);
    }

    @Override
    public String format(LocalDateTime value) throws InvalidTypeException {
        if (value == null)
            return "NULL";

        return quote(value.format(DateTimeFormatter.ISO_DATE_TIME));
    }
}
