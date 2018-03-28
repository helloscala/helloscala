/*
 * Copyright 2017 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package helloscala.nosql.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.extras.codecs.arrays.DoubleArrayCodec;
import com.datastax.driver.extras.codecs.arrays.FloatArrayCodec;
import com.datastax.driver.extras.codecs.arrays.IntArrayCodec;
import com.datastax.driver.extras.codecs.arrays.LongArrayCodec;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import com.datastax.driver.extras.codecs.jdk8.LocalDateCodec;
import com.datastax.driver.extras.codecs.jdk8.LocalTimeCodec;
import com.datastax.driver.extras.codecs.jdk8.ZonedDateTimeCodec;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import helloscala.common.Configuration;
import helloscala.common.Configuration$;

/**
 * Created by yangbajing(yangbajing@gmail.com) on 2017-03-21.
 */
public class CassandraHelper {

    /**
     * 获得 Cassandra 连接 Cluster
     */
    public static Cluster getCluster(CassandraConf c) {
        Cluster.Builder builder = Cluster.builder()
                .addContactPoints(c.nodes())
//                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .withClusterName(c.clusterName());

        if (c.username().isDefined() && c.password().isDefined()) {
            builder = builder.withCredentials(c.username().get(), c.password().get());
        }

        Cluster cluster = builder.build();

        cluster.getConfiguration().getCodecRegistry()
                .register(new ZonedDateTimeCodec(cluster.getMetadata().newTupleType(DataType.timestamp(), DataType.varchar())))
                .register(new IntArrayCodec())
                .register(new FloatArrayCodec())
                .register(new DoubleArrayCodec())
                .register(new LongArrayCodec())
                .register(LocalDateTimeCode.instance)
                .register(InstantCodec.instance)
                .register(LocalDateCodec.instance)
                .register(LocalTimeCodec.instance);

        return cluster;
    }

    public static Cluster getClusterFromConfigPath(String path) {
        return getCluster(getConf(path));
    }

    public static CassandraConf getConf(String path) {
        Configuration config = Configuration$.MODULE$.apply(ConfigFactory.load().getConfig(path));
        return getConf(config);
    }

    public static CassandraConf getConf(Config config) {
        return getConf(Configuration$.MODULE$.apply(config));
    }

    public static CassandraConf getConf(Configuration config) {
        return CassandraConf$.MODULE$.apply(config);
    }

}
