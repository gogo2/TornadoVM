/*
 * Copyright 2012 James Clarkson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tornado.graal.compiler;

import org.graalvm.compiler.lir.phases.LIRPhaseSuite;
import org.graalvm.compiler.lir.phases.PostAllocationOptimizationPhase.PostAllocationOptimizationContext;
import org.graalvm.compiler.lir.phases.PreAllocationOptimizationPhase.PreAllocationOptimizationContext;
import org.graalvm.compiler.options.OptionValues;
import org.graalvm.compiler.phases.common.AddressLoweringPhase.AddressLowering;
import org.graalvm.compiler.phases.common.CanonicalizerPhase.CustomCanonicalizer;
import tornado.graal.phases.lir.TornadoAllocationStage;

public interface TornadoCompilerConfiguration {

    public TornadoAllocationStage createAllocationStage(OptionValues options);

    public TornadoSketchTier createSketchTier(OptionValues options, CustomCanonicalizer canonicalizer);

    public TornadoHighTier createHighTier(OptionValues options, CustomCanonicalizer canonicalizer);

    public TornadoLowTier createLowTier(OptionValues options, AddressLowering addressLowering);

    public TornadoMidTier createMidTier(OptionValues options);

    public LIRPhaseSuite<PostAllocationOptimizationContext> createPostAllocationOptimizationStage(OptionValues options);

    public LIRPhaseSuite<PreAllocationOptimizationContext> createPreAllocationOptimizationStage(OptionValues options);

}