package uk.ac.manchester.tornado.drivers.cuda.graal.lir;

import org.graalvm.compiler.lir.LIRInstructionClass;
import org.graalvm.compiler.lir.LabelRef;
import org.graalvm.compiler.nodes.cfg.Block;
import uk.ac.manchester.tornado.drivers.cuda.graal.asm.PTXAssembler;
import uk.ac.manchester.tornado.drivers.cuda.graal.compiler.PTXCompilationResultBuilder;
import uk.ac.manchester.tornado.drivers.cuda.graal.lir.PTXLIRStmt.AbstractInstruction;

import static uk.ac.manchester.tornado.drivers.cuda.graal.asm.PTXAssemblerConstants.COLON;
import static uk.ac.manchester.tornado.drivers.cuda.graal.asm.PTXAssemblerConstants.TAB;

public class PTXControlFlow {

    protected static void emitBlock(int blockId, PTXAssembler asm) {
        emitBlockRef(blockId, asm);
        asm.emitSymbol(COLON);
    }

    protected static void emitBlockRef(int blockId, PTXAssembler asm) {
        asm.emitBlock(blockId);
    }

    public static class LoopInitOp extends AbstractInstruction {
        public static final LIRInstructionClass<LoopInitOp> TYPE = LIRInstructionClass.create(LoopInitOp.class);
        private final LabelRef block;

        public LoopInitOp(LabelRef b) {
            super(TYPE);
            this.block = b;
        }

        @Override
        public void emitCode(PTXCompilationResultBuilder crb, PTXAssembler asm) {
            emitBlock(block.label().getBlockId(), asm);
            asm.eol();
        }
    }

    public static class LoopBreakOp extends AbstractInstruction {

        public static final LIRInstructionClass<LoopBreakOp> TYPE = LIRInstructionClass.create(LoopBreakOp.class);

        public LoopBreakOp() {
            super(TYPE);
        }

        @Override
        public void emitCode(PTXCompilationResultBuilder crb, PTXAssembler asm) {
            asm.loopBreak();
            asm.delimiter();
            asm.eol();
        }

    }

    public static class LoopExit extends AbstractInstruction {
        public static final LIRInstructionClass<LoopExit> TYPE = LIRInstructionClass.create(LoopExit.class);

        private final Block block;

        public LoopExit(Block block) {
            super(TYPE);
            this.block = block;
        }

        @Override
        public void emitCode(PTXCompilationResultBuilder crb, PTXAssembler asm) {
            emitBlock(block.getId(), asm);
            asm.eol();
        }
    }

    public static class Branch extends AbstractInstruction {
        public static final LIRInstructionClass<Branch> TYPE = LIRInstructionClass.create(Branch.class);
        private final LabelRef destination;
        private final boolean isConditional;

        public Branch(LabelRef destination) {
            this(destination, true);
        }

        public Branch(LabelRef destination, boolean isConditional) {
            super(TYPE);
            this.destination = destination;
            this.isConditional = isConditional;
        }

        @Override
        public void emitCode(PTXCompilationResultBuilder crb, PTXAssembler asm) {
            asm.emitSymbol(TAB);
            asm.emit("bra");
            if (!isConditional) asm.emit(".uni");
            asm.emitSymbol(TAB);

            emitBlockRef(destination.label().getBlockId(), asm);
            asm.delimiter();
            asm.eol();
        }
    }

    public static class BlockRef extends AbstractInstruction {
        public static final LIRInstructionClass<BlockRef> TYPE = LIRInstructionClass.create(BlockRef.class);
        private final LabelRef ref;

        public BlockRef(LabelRef lirBlock) {
            super(TYPE);
            this.ref = lirBlock;
        }

        @Override
        public void emitCode(PTXCompilationResultBuilder crb, PTXAssembler asm) {
            emitBlock(ref.label().getBlockId(), asm);
        }
    }
}
