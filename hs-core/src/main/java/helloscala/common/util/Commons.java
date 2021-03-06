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
