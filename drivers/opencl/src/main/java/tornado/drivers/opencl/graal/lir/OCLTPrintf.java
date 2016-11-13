package tornado.drivers.opencl.graal.lir;

import com.oracle.graal.compiler.common.LIRKind;
import com.oracle.graal.lir.Opcode;
import jdk.vm.ci.meta.Value;
import tornado.drivers.opencl.graal.asm.OCLAssembler;
import tornado.drivers.opencl.graal.compiler.OCLCompilationResultBuilder;

@Opcode("TPRINTF")
public class OCLTPrintf extends OCLLIROp {

    private Value[] inputs;

    public OCLTPrintf(Value[] inputs) {
        super(LIRKind.Illegal);
        this.inputs = inputs;
    }

    @Override
    public void emit(OCLCompilationResultBuilder crb, OCLAssembler asm) {
        asm.emit("if( ");
        asm.emit("get_global_id(0) == ");
        asm.emitValue(crb, inputs[0]);

        asm.emit(" && get_global_id(1) == ");
        asm.emitValue(crb, inputs[1]);

        asm.emit(" && get_global_id(2) == ");
        asm.emitValue(crb, inputs[2]);
        asm.emit(" )");
        asm.beginScope();

        asm.indent();
        asm.emit("printf( \"tornado[%3d,%3d,%3d]> ");
        asm.emitValue(crb, inputs[3]);
        asm.emit("\", ");
        for (int i = 0; i < 3; i++) {
            asm.emitValue(crb, inputs[i]);
            asm.emit(", ");
        }
        for (int i = 4; i < inputs.length - 1; i++) {
            asm.emitValue(crb, inputs[i]);
            asm.emit(", ");
        }
        asm.emitValue(crb, inputs[inputs.length - 1]);
        asm.emit(")");
        asm.delimiter();
        asm.eol();
        asm.endScope();

    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<%s,%s,%s> tprintf( %s", inputs[0], inputs[1], inputs[2], inputs[3]));
        for (int i = 4; i < inputs.length - 1; i++) {
            sb.append(inputs[i]);
            sb.append(", ");
        }
        sb.append(inputs[inputs.length - 1]);
        sb.append(" )");
        return sb.toString();
    }

}