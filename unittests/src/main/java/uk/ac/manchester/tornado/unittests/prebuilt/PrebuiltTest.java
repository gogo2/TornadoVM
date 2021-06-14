/*
 * Copyright (c) 2013-2020, APT Group, Department of Computer Science,
 * The University of Manchester.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package uk.ac.manchester.tornado.unittests.prebuilt;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import uk.ac.manchester.tornado.api.TaskSchedule;
import uk.ac.manchester.tornado.api.collections.types.Float8;
import uk.ac.manchester.tornado.api.collections.types.VectorFloat;
import uk.ac.manchester.tornado.api.common.Access;
import uk.ac.manchester.tornado.api.common.TornadoDevice;
import uk.ac.manchester.tornado.api.enums.TornadoVMBackendType;
import uk.ac.manchester.tornado.api.runtime.TornadoRuntime;
import uk.ac.manchester.tornado.unittests.common.TornadoTestBase;

public class PrebuiltTest extends TornadoTestBase {

    @Test
    public void testPrebuild01() {

        final int numElements = 8;
        int[] a = new int[numElements];
        int[] b = new int[numElements];
        int[] c = new int[numElements];

        String tornadoSDK = System.getenv("TORNADO_SDK");

        Arrays.fill(a, 1);
        Arrays.fill(b, 2);

        TornadoDevice defaultDevice = TornadoRuntime.getTornadoRuntime().getDriver(0).getDevice(0);
        String filePath = tornadoSDK + "/examples/generated/";

        TornadoVMBackendType backendType = TornadoRuntime.getTornadoRuntime().getBackendType(0);
        switch (backendType) {
            case PTX:
                filePath += "add.ptx";
                break;
            case OpenCL:
                filePath += "add.cl";
                break;
            case SPIRV:
                filePath += "add.spv";
                break;
            default:
                throw new RuntimeException("Backend not supported");
        }

        // @formatter:off
        new TaskSchedule("s0")
            .prebuiltTask("t0", 
                        "add", 
                        filePath,
                        new Object[] { a, b, c },
                        new Access[] { Access.READ, Access.READ, Access.WRITE }, 
                        defaultDevice,
                        new int[] { numElements })
            .streamOut(c)
            .execute();
        // @formatter:on

        for (int i = 0; i < c.length; i++) {
            assertEquals(a[i] + b[i], c[i]);
        }
    }

    @Test
    public void testPrebuild02() {

        final int numElements = 8;
        int[] a = new int[numElements];
        int[] b = new int[numElements];
        int[] c = new int[numElements];

        String tornadoSDK = System.getenv("TORNADO_SDK");

        Arrays.fill(a, 1);
        Arrays.fill(b, 2);

        TornadoDevice defaultDevice = TornadoRuntime.getTornadoRuntime().getDriver(0).getDevice(0);
        String filePath = tornadoSDK + "/examples/generated/";

        TornadoVMBackendType backendType = TornadoRuntime.getTornadoRuntime().getBackendType(0);
        switch (backendType) {
            case PTX:
                filePath += "add.ptx";
                break;
            case OpenCL:
                filePath += "add.cl";
                break;
            case SPIRV:
                filePath += "add.spv";
                break;
            default:
                throw new RuntimeException("Backend not supported");
        }

        // @formatter:off
        new TaskSchedule("s0")
                .prebuiltTask("t0",
                        "add",
                        filePath,
                        new Object[] { a, b, c },
                        new Access[] { Access.READ, Access.READ, Access.WRITE },
                        defaultDevice,
                        new int[] { numElements })
                .streamOut(c)
                .execute();
        // @formatter:on

        for (int i = 0; i < c.length; i++) {
            assertEquals(a[i] + b[i], c[i]);
        }
    }

    @Test
    public void testPrebuild03() {

        TornadoDevice defaultDevice = TornadoRuntime.getTornadoRuntime().getDriver(0).getDevice(0);
        String filePath = "/home/juan/manchester/tornado/tornado/";

        TornadoVMBackendType backendType = TornadoRuntime.getTornadoRuntime().getBackendType(0);
        if (backendType == TornadoVMBackendType.SPIRV) {
            filePath += "vectorFloat8.spv";
        } else {
            throw new RuntimeException("Backend not supported");
        }

        Float8 a = new Float8(1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f);
        Float8 b = new Float8(8f, 7f, 6f, 5f, 4f, 3f, 2f, 1f);
        VectorFloat output = new VectorFloat(1);

        // @formatter:off
        new TaskSchedule("s0")
                .prebuiltTask("t0",
                        "dotMethodFloat8",
                        filePath,
                        new Object[] { a, b, output },
                        new Access[] { Access.READ, Access.READ, Access.WRITE },
                        defaultDevice,
                        new int[] { 1 })
                .streamOut(output)
                .execute();
        // @formatter:on

        assertEquals(120, output.get(0), 0.001f);

    }

}
