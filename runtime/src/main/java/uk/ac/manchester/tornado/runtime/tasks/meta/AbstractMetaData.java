/*
 * This file is part of Tornado: A heterogeneous programming framework:
 * https://github.com/beehive-lab/tornado
 *
 * Copyright (c) 2013-2019, APT Group, School of Computer Science,
 * The University of Manchester. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Authors: James Clarkson
 *
 */
package uk.ac.manchester.tornado.runtime.tasks.meta;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static uk.ac.manchester.tornado.runtime.tasks.meta.MetaDataUtils.resolveDevice;

import java.util.Arrays;
import java.util.List;

import uk.ac.manchester.tornado.api.common.TornadoDevice;
import uk.ac.manchester.tornado.api.common.TornadoEvents;
import uk.ac.manchester.tornado.api.mm.TaskMetaDataInterface;
import uk.ac.manchester.tornado.api.profiler.TornadoProfiler;
import uk.ac.manchester.tornado.runtime.TornadoAcceleratorDriver;
import uk.ac.manchester.tornado.runtime.TornadoCoreRuntime;
import uk.ac.manchester.tornado.runtime.common.Tornado;
import uk.ac.manchester.tornado.runtime.common.TornadoAcceleratorDevice;

public abstract class AbstractMetaData implements TaskMetaDataInterface {

    private String id;
    private TornadoAcceleratorDevice device;
    private boolean shouldRecompile;
    private final boolean isDeviceDefined;
    private int driverIndex;
    private int deviceIndex;
    private boolean deviceManuallySet;
    private long numThreads;
    private final List<String> compileCLOptions = Arrays.asList("-cl-single-precision-constant", "-cl-denorms-are-zero", "-cl-opt-disable", "-cl-strict-aliasing", "-cl-mad-enable",
            "-cl-no-signed-zeros", "-cl-unsafe-math-optimizations", "-cl-finite-math-only", "-cl-fast-relaxed-math", "-w");
    private TornadoProfiler profiler;

    private static final int DEFAULT_DRIVER_INDEX = 0;
    private static final int DEFAULT_DEVICE_INDEX = 0;

    private static String getProperty(String key) {
        return System.getProperty(key);
    }

    public TornadoAcceleratorDevice getDevice() {
        if (device == null) {
            device = resolveDevice(Tornado.getProperty(id + ".device", driverIndex + ":" + deviceIndex));
        }
        return device;
    }

    private int getDeviceIndex(int driverIndex, TornadoDevice device) {
        TornadoAcceleratorDriver driver = TornadoCoreRuntime.getTornadoRuntime().getDriver(driverIndex);
        int devs = driver.getDeviceCount();
        int index = 0;
        for (int i = 0; i < devs; i++) {
            if (driver.getDevice(i).getPlatformName().equals(device.getPlatformName()) && (driver.getDevice(i).getDeviceName().equals(device.getDeviceName()))) {
                index = i;
                break;
            }
        }
        return index;
    }

    boolean isDeviceManuallySet() {
        return deviceManuallySet;
    }

    /**
     * Set a device in the default driver in Tornado.
     *
     * @param device
     */
    public void setDevice(TornadoDevice device) {
        this.driverIndex = DEFAULT_DRIVER_INDEX;
        this.deviceIndex = getDeviceIndex(DEFAULT_DRIVER_INDEX, device);
        if (device instanceof TornadoAcceleratorDevice) {
            this.device = (TornadoAcceleratorDevice) device;
        }
        deviceManuallySet = true;
    }

    /**
     * Set a device from a specific Tornado driver.
     *
     * @param driverIndex
     * @param device
     */
    public void setDriverDevice(int driverIndex, TornadoAcceleratorDevice device) {
        this.driverIndex = deviceIndex;
        this.deviceIndex = getDeviceIndex(driverIndex, device);
        this.device = device;
    }

    public int getDriverIndex() {
        return driverIndex;
    }

    public int getDeviceIndex() {
        return deviceIndex;
    }

    public String getCpuConfig() {
        return cpuConfig;
    }

    public String getId() {
        return id;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean shouldDumpEvents() {
        return dumpEvents;
    }

    public boolean shouldDumpProfiles() {
        return dumpProfiles;
    }

    public boolean shouldDumpSchedule() {
        return dumpTaskSchedule;
    }

    public boolean shouldDebugKernelArgs() {
        return debugKernelArgs;
    }

    public boolean shouldPrintCompileTimes() {
        return printCompileTimes;
    }

    public boolean shouldPrintKernelExecutionTime() {
        return printKernelExecutionTime;
    }

    public boolean shouldRecompile() {
        return shouldRecompile;
    }

    public void setRecompiled() {
        shouldRecompile = false;
    }

    public String getCompilerFlags() {
        return composeBuiltOptions(compilerFlagsOpenCL);
    }

    public int getOpenCLGpuBlockX() {
        return openclGpuBlockX;
    }

    public int getOpenCLGpuBlock2DX() {
        return openclGpuBlock2DX;
    }

    public int getOpenCLGpuBlock2DY() {
        return openclGpuBlock2DY;
    }

    public boolean shouldUseOpenclRelativeAddresses() {
        return openclUseRelativeAddresses;
    }

    public boolean enableOpenclBifs() {
        return openclEnableBifs;
    }

    public boolean shouldUseDefaultOpenCLScheduling() {
        return openclUseScheduling;
    }

    public boolean shouldUseOpenclWaitActive() {
        return openclWaitActive;
    }

    public boolean shouldUseVmWaitEvent() {
        return vmWaitEvent;
    }

    public boolean enableExceptions() {
        return enableExceptions;
    }

    public boolean enableProfiling() {
        return enableProfiling;
    }

    public boolean enableOooExecution() {
        return enableOooExecution;
    }

    public boolean shouldUseOpenclBlockingApiCalls() {
        return openclUseBlockingApiCalls;
    }

    public boolean enableParallelization() {
        return enableParallelization;
    }

    public boolean enableVectors() {
        return enableVectors;
    }

    public boolean enableMemChecks() {
        return enableMemChecks;
    }

    public boolean enableThreadCoarsener() {
        return useThreadCoarsener;
    }

    public boolean enableAutoParallelisation() {
        return enableAutoParallelisation;
    }

    public boolean shouldUseVMDeps() {
        return vmUseDeps;
    }

    /*
     * Forces the executing kernel to output its arguments before execution
     */
    private final boolean debug;
    private final boolean dumpEvents;
    private final boolean dumpProfiles;
    private final boolean debugKernelArgs;
    private final boolean printCompileTimes;
    private boolean printKernelExecutionTime;

    // private final boolean forceAllToGpu;
    private boolean isOpenclCompilerFlagsDefined;
    private final boolean isOpenclGpuBlockXDefined;
    private final int openclGpuBlockX;
    private final boolean isOpenclGpuBlock2DXDefined;
    private final int openclGpuBlock2DX;
    private final boolean isOpenclGpuBlock2DYDefined;
    private final int openclGpuBlock2DY;
    private final boolean openclUseRelativeAddresses;
    private final boolean openclEnableBifs;
    private String compilerFlagsOpenCL;

    /*
     * Allows the OpenCL driver to select the size of local work groups
     */
    private final boolean openclUseScheduling;
    private final boolean openclWaitActive;
    private final boolean vmWaitEvent;
    private final boolean enableExceptions;
    private final boolean enableProfiling;
    private final boolean enableOooExecution;
    private final boolean openclUseBlockingApiCalls;
    private final boolean enableParallelization;
    private final boolean enableVectors;
    private final boolean enableMemChecks;
    private final boolean useThreadCoarsener;
    private final boolean dumpTaskSchedule;
    private final boolean vmUseDeps;
    private final boolean coarsenWithCpuConfig;
    private final boolean enableAutoParallelisation;
    private final boolean isEnableParallelizationDefined;

    private final boolean isCpuConfigDefined;
    private final String cpuConfig;

    public boolean isDeviceDefined() {
        return isDeviceDefined;
    }

    boolean isEnableParallelizationDefined() {
        return isEnableParallelizationDefined;
    }

    public boolean isOpenclCompilerFlagsDefined() {
        return isOpenclCompilerFlagsDefined;
    }

    @Override
    public void setCompilerFlags(String value) {
        compilerFlagsOpenCL = value;
        isOpenclCompilerFlagsDefined = true;
    }

    public String composeBuiltOptions(String rawFlags) {
        String[] temp;
        rawFlags = rawFlags.replace(",", " ");
        temp = rawFlags.split(" ");

        for (String str : temp) {
            if (!compileCLOptions.contains(str)) {
                rawFlags = " ";
            }
        }
        return rawFlags;
    }

    public boolean isOpenclGpuBlockXDefined() {
        return isOpenclGpuBlockXDefined;
    }

    public boolean isOpenclGpuBlock2DXDefined() {
        return isOpenclGpuBlock2DXDefined;
    }

    public boolean isOpenclGpuBlock2DYDefined() {
        return isOpenclGpuBlock2DYDefined;
    }

    public boolean isCpuConfigDefined() {
        return isCpuConfigDefined;
    }

    public boolean shouldCoarsenWithCpuConfig() {
        return coarsenWithCpuConfig;
    }

    @Override
    public List<TornadoEvents> getProfiles() {
        return null;
    }

    @Override
    public void setGlobalWork(long[] global) {

    }

    @Override
    public void setLocalWork(long[] local) {

    }

    @Override
    public long[] getGlobalWork() {
        return null;
    }

    @Override
    public long[] getLocalWork() {
        return null;
    }

    protected static String getDefault(String keySuffix, String id, String defaultValue) {
        if (getProperty(id + "." + keySuffix) == null) {
            return Tornado.getProperty("tornado" + "." + keySuffix, defaultValue);
        } else {
            return getProperty(id + "." + keySuffix);
        }
    }

    @Override
    public void setNumThreads(long threads) {
        this.numThreads = threads;
    }

    @Override
    public long getNumThreads() {
        return numThreads;
    }

    AbstractMetaData(String id) {
        this.id = id;
        shouldRecompile = true;

        isDeviceDefined = getProperty(id + ".device") != null;
        if (isDeviceDefined) {
            int[] a = MetaDataUtils.resolveDriverDeviceIndexes(getProperty(id + ".device"));
            driverIndex = a[0];
            deviceIndex = a[1];
        } else {
            driverIndex = DEFAULT_DRIVER_INDEX;
            deviceIndex = DEFAULT_DEVICE_INDEX;
        }

        debugKernelArgs = parseBoolean(getDefault("debug.kernelargs", id, "True"));
        printCompileTimes = parseBoolean(getDefault("debug.compiletimes", id, "False"));
        printKernelExecutionTime = parseBoolean(getProperty("tornado.debug.executionTime"));
        openclUseRelativeAddresses = parseBoolean(getDefault("opencl.userelative", id, "False"));
        openclWaitActive = parseBoolean(getDefault("opencl.wait.active", id, "False"));
        coarsenWithCpuConfig = parseBoolean(getDefault("coarsener.ascpu", id, "False"));

        /*
         * Allows the OpenCL driver to select the size of local work groups
         */
        openclUseScheduling = parseBoolean(getDefault("opencl.schedule", id, "True"));
        vmWaitEvent = parseBoolean(getDefault("vm.waitevent", id, "False"));
        enableExceptions = parseBoolean(getDefault("exceptions.enable", id, "False"));
        enableProfiling = parseBoolean(getDefault("profiling.enable", id, "False"));
        enableOooExecution = parseBoolean(getDefault("ooo-execution.enable", id, "False"));
        openclUseBlockingApiCalls = parseBoolean(getDefault("opencl.blocking", id, "False"));

        enableParallelization = parseBoolean(getDefault("parallelise", id, "True"));
        isEnableParallelizationDefined = getProperty(id + ".parallelise") != null;

        enableVectors = parseBoolean(getDefault("vectors.enable", id, "True"));
        openclEnableBifs = parseBoolean(getDefault("bifs.enable", id, "False"));
        debug = parseBoolean(getDefault("debug", id, "False"));
        enableMemChecks = parseBoolean(getDefault("memory.check", id, "False"));
        dumpEvents = parseBoolean(getDefault("events.dump", id, "True"));
        dumpProfiles = parseBoolean(getDefault("profiles.print", id, "False"));
        dumpTaskSchedule = parseBoolean(getDefault("schedule.dump", id, "False"));

        compilerFlagsOpenCL = (getProperty("tornado.opencl.compiler.options") == null) ? "-w" : getProperty("tornado.opencl.compiler.options");
        isOpenclCompilerFlagsDefined = getProperty("tornado.opencl.compiler.options") != null;

        openclGpuBlockX = parseInt(getDefault("opencl.gpu.block.x", id, "256"));
        isOpenclGpuBlockXDefined = getProperty(id + ".opencl.gpu.block.x") != null;

        openclGpuBlock2DX = parseInt(getDefault("opencl.gpu.block2d.x", id, "4"));
        isOpenclGpuBlock2DXDefined = getProperty(id + ".opencl.gpu.block2d.x") != null;

        openclGpuBlock2DY = parseInt(getDefault("opencl.gpu.block2d.y", id, "4"));
        isOpenclGpuBlock2DYDefined = getProperty(id + ".opencl.gpu.block2d.y") != null;

        cpuConfig = getDefault("cpu.config", id, null);
        isCpuConfigDefined = getProperty(id + ".cpu.config") != null;
        useThreadCoarsener = Boolean.parseBoolean(getDefault("coarsener", id, "False"));
        enableAutoParallelisation = Boolean.parseBoolean(getDefault("parallelise.auto", id, "False"));
        vmUseDeps = Boolean.parseBoolean(getDefault("vm.deps", id, "False"));
    }

    public void attachProfiler(TornadoProfiler profiler) {
        this.profiler = profiler;
    }

    public TornadoProfiler getProfiler() {
        return this.profiler;
    }

}
