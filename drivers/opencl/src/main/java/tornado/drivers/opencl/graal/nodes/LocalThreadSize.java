/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tornado.drivers.opencl.graal.nodes;

import com.oracle.graal.graph.Node;
import com.oracle.graal.graph.NodeClass;
import com.oracle.graal.lir.Variable;
import com.oracle.graal.lir.gen.LIRGeneratorTool;
import com.oracle.graal.nodeinfo.NodeInfo;
import com.oracle.graal.nodes.ConstantNode;
import com.oracle.graal.nodes.calc.FloatingNode;
import com.oracle.graal.nodes.spi.LIRLowerable;
import com.oracle.graal.nodes.spi.NodeLIRBuilderTool;
import jdk.vm.ci.meta.Value;
import tornado.drivers.opencl.graal.OCLStampFactory;
import tornado.drivers.opencl.graal.asm.OCLAssembler.OCLUnaryIntrinsic;
import tornado.drivers.opencl.graal.lir.OCLKind;
import tornado.drivers.opencl.graal.lir.OCLLIRInstruction;
import tornado.drivers.opencl.graal.lir.OCLUnary;

@NodeInfo
public class LocalThreadSize extends FloatingNode implements LIRLowerable {

    public static final NodeClass<LocalThreadSize> TYPE = NodeClass.create(LocalThreadSize.class);

    @Node.Input
    protected ConstantNode index;

    public LocalThreadSize(ConstantNode value) {
        super(TYPE, OCLStampFactory.getStampFor(OCLKind.INT));
        assert stamp != null;
        index = value;
    }

    @Override
    public void generate(NodeLIRBuilderTool gen) {
        LIRGeneratorTool tool = gen.getLIRGeneratorTool();
        Variable result = tool.newVariable(tool.getLIRKind(stamp));
        tool.append(new OCLLIRInstruction.AssignStmt(result, new OCLUnary.Intrinsic(OCLUnaryIntrinsic.LOCAL_SIZE, tool.getLIRKind(stamp), (Value) index.asConstant())));
        gen.setResult(this, result);
    }

}
