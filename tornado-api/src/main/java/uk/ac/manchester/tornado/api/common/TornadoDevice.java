/*
 * Copyright (c) 2013-2023, APT Group, Department of Computer Science,
 * The University of Manchester.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.manchester.tornado.api.common;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import uk.ac.manchester.tornado.api.TornadoDeviceContext;
import uk.ac.manchester.tornado.api.TornadoTargetDevice;
import uk.ac.manchester.tornado.api.enums.TornadoDeviceType;
import uk.ac.manchester.tornado.api.enums.TornadoVMBackendType;
import uk.ac.manchester.tornado.api.memory.TornadoDeviceObjectState;
import uk.ac.manchester.tornado.api.memory.TornadoMemoryProvider;

public interface TornadoDevice {

    /**
     * It allocates an object in the pre-defined heap of the target device. It also
     * ensures that there is enough space for the input object.
     *
     * @param object
     *     to be allocated
     * @param batchSize
     *     size of the object to be allocated. If this value is <= 0, then it
     *     allocates the sizeof(object).
     * @param state
     *     state of the object in the target device
     *     {@link TornadoDeviceObjectState}
     * @return an event ID
     */
    int allocate(Object object, long batchSize, TornadoDeviceObjectState state);

    int allocateObjects(Object[] objects, long batchSize, TornadoDeviceObjectState[] states);

    int deallocate(TornadoDeviceObjectState state);

    /**
     * It allocates and copy in the content of the object to the target device.
     *
     * @param object
     *     to be allocated
     * @param objectState
     *     state of the object in the target device
     *     {@link TornadoDeviceObjectState}
     * @param events
     *     list of pending events (dependencies)
     * @param batchSize
     *     size of the object to be allocated. If this value is <= 0, then it
     *     allocates the sizeof(object).
     * @param hostOffset
     *     offset in bytes for the copy within the host input array (or
     *     object)
     * @return an event ID
     */
    List<Integer> ensurePresent(Object object, TornadoDeviceObjectState objectState, int[] events, long batchSize, long hostOffset);

    /**
     * It always copies in the input data (object) from the host to the target
     * device.
     *
     * @param object
     *     to be copied
     * @param batchSize
     *     size of the object to be allocated. If this value is <= 0, then it
     *     allocates the sizeof(object).
     * @param hostOffset
     *     offset in bytes for the copy within the host input array (or
     *     object)
     * @param objectState
     *     state of the object in the target device
     *     {@link TornadoDeviceObjectState}
     * @param events
     *     list of previous events
     * @return and event ID
     */
    List<Integer> streamIn(Object object, long batchSize, long hostOffset, TornadoDeviceObjectState objectState, int[] events);

    /**
     * It copies a device buffer from the target device to the host. Copies are
     * non-blocking
     *
     * @param object
     *     to be copied.
     * @param hostOffset
     *     offset in bytes for the copy within the host input array (or
     *     object)
     * @param objectState
     *     state of the object in the target device
     *     {@link TornadoDeviceObjectState}
     * @param events
     *     of pending events
     * @return and event ID
     */
    int streamOut(Object object, long hostOffset, TornadoDeviceObjectState objectState, int[] events);

    /**
     * It copies a device buffer from the target device to the host. Copies are
     * blocking between the device and the host.
     *
     * @param object
     *     to be copied.
     * @param hostOffset
     *     offset in bytes for the copy within the host input array (or
     *     object)
     * @param objectState
     *     state of the object in the target device
     *     {@link TornadoDeviceObjectState}
     * @param events
     *     of pending events
     * @return and event ID
     */
    int streamOutBlocking(Object object, long hostOffset, TornadoDeviceObjectState objectState, int[] events);

    /**
     * It resolves a pending event.
     *
     * @param event
     *     ID
     * @return an object of type {@link Event}
     */
    Event resolveEvent(int event);

    void ensureLoaded();

    void flushEvents();

    int enqueueBarrier();

    int enqueueBarrier(int[] events);

    int enqueueMarker();

    int enqueueMarker(int[] events);

    void sync();

    void flush();

    void reset();

    void dumpEvents();

    String getDeviceName();

    String getDescription();

    String getPlatformName();

    TornadoDeviceContext getDeviceContext();

    TornadoTargetDevice getPhysicalDevice();

    TornadoMemoryProvider getMemoryProvider();

    TornadoDeviceType getDeviceType();

    long getMaxAllocMemory();

    long getMaxGlobalMemory();

    long getDeviceLocalMemorySize();

    long[] getDeviceMaxWorkgroupDimensions();

    String getDeviceOpenCLCVersion();

    Object getDeviceInfo();

    int getDriverIndex();

    /**
     * Returns the number of processors available to the JVM. We need to overwrite
     * this function only for Virtual Devices, where we read the value from the
     * descriptor file.
     */
    default int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    Object getAtomic();

    void setAtomicsMapping(ConcurrentHashMap<Object, Integer> mappingAtomics);

    TornadoVMBackendType getTornadoVMBackend();

    boolean isSPIRVSupported();

}
