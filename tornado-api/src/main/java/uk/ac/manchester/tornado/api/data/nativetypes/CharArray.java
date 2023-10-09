package uk.ac.manchester.tornado.api.data.nativetypes;

import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import uk.ac.manchester.tornado.api.type.annotations.PanamaElementSize;

@PanamaElementSize(size = 2)
public class CharArray {
    private MemorySegment segment;
    private final int CHAR_BYTES = 2;

    private int numberOfElements;
    private int arrayHeaderSize;

    private int baseIndex;
    private int arraySizeHeaderPosition;

    private long segmentByteSize;

    public CharArray(int numberOfElements) {
        this.numberOfElements = numberOfElements;
        arrayHeaderSize = (int) TornadoNativeArray.ARRAY_HEADER;
        baseIndex = arrayHeaderSize / CHAR_BYTES;
        arraySizeHeaderPosition = baseIndex - 2;
        segmentByteSize = numberOfElements * CHAR_BYTES + arrayHeaderSize;

        segment = Arena.ofAuto().allocate(segmentByteSize, 1);
        segment.setAtIndex(JAVA_INT, 0, numberOfElements);
    }

    public void set(int index, char value) {
        segment.setAtIndex(JAVA_CHAR, baseIndex + index, value);
    }

    public char get(int index) {
        return segment.getAtIndex(JAVA_CHAR, baseIndex + index);
    }

    public void init(char value) {
        for (int i = 0; i < getSize(); i++) {
            segment.setAtIndex(JAVA_CHAR, baseIndex + i, value);
        }
    }

    public int getSize() {
        return numberOfElements;
    }

    public MemorySegment getSegment() {
        return segment;
    }

    public long getNumBytesOfSegment() {
        return segmentByteSize;
    }
}
