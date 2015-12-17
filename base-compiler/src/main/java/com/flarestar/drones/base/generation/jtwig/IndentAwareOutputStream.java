package com.flarestar.drones.base.generation.jtwig;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Stack;

/**
 * TODO
 */
public class IndentAwareOutputStream extends ByteArrayOutputStream {
    private static final int SPACE_BUFFER_SIZE = 32;
    private static final int TAB_SIZE = 4;

    private Stack<Integer> indentStack = new Stack<>();
    public int totalIndent = 0;
    private byte[] spaceBuffer;
    public int currentIndentSpaceCount = 0;
    private boolean isAtLineStart = false;

    public IndentAwareOutputStream() {
        super();

        spaceBuffer = initSpaceBuffer();
    }

    public IndentAwareOutputStream(int size) {
        super(size);

        spaceBuffer = initSpaceBuffer();
    }

    // TODO: so how do we do this? not sure.
    @Override
    public synchronized void write(byte[] bytes, final int startOffset, final int length) {
        final int endOffset = startOffset + length;

        int offset = startOffset;
        while (offset < endOffset) {
            if (isAtLineStart) {
                int count = countUntilNonSpace(bytes, offset, endOffset);
                super.write(bytes, offset, count);

                offset += count;
                isAtLineStart = false;
            } else {
                int count = countUntilAfterNewline(bytes, offset, endOffset);
                super.write(bytes, offset, count);

                offset += count;
                isAtLineStart = true;

                if (bytes[offset - 1] == '\n') {
                    currentIndentSpaceCount = 0;
                    writeCurrentIndent();
                }
            }
        }
    }

    @Override
    public synchronized void write(int b) {
        super.write(b);
    }

    public void pushIndent() {
        indentStack.push(currentIndentSpaceCount);
        totalIndent += currentIndentSpaceCount;

        currentIndentSpaceCount = 0;
    }

    public void popIndent() {
        currentIndentSpaceCount = indentStack.pop();
        totalIndent -= currentIndentSpaceCount;
    }

    private byte[] initSpaceBuffer() {
        byte[] result = new byte[SPACE_BUFFER_SIZE];
        Arrays.fill(result, (byte)' ');
        return result;
    }

    private int countUntilAfterNewline(byte[] bytes, final int offset, final int endOffset) {
        int i = offset;
        for (; i < endOffset; ++i) {
            if (bytes[i] == '\n') {
                return i - offset + 1;
            }
        }
        return i - offset;
    }

    private int countUntilNonSpace(byte[] bytes, final int offset, final int endOffset) {
        int i = offset;
        for (; i < endOffset; ++i) {
            switch (bytes[i]) {
                case ' ':
                    ++currentIndentSpaceCount;
                    break;
                case '\t':
                    currentIndentSpaceCount += TAB_SIZE;
                    break;
                default:
                    return i - offset;
            }
        }
        return i - offset;
    }

    private void writeCurrentIndent() {
        int indent = totalIndent;
        while (indent > 0) {
            final int amount = Math.min(SPACE_BUFFER_SIZE, indent);

            super.write(spaceBuffer, 0, amount);

            indent -= amount;
        }
    }
}
/*
- so instead, we keep track of the current indent here
  - while there is still data
    - if at line start, count indent until non space char, write spaces, then set atLineStart = false
    - if not at line start (at this point), write chars until newline found (including newline), write current indent, then set atLineStart = true & currentIndent = 0
- on {% render}, we push/pop

 */