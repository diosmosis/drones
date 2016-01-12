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
    private int totalIndent = 0;
    private byte[] spaceBuffer;
    private int currentIndentSpaceCount = 0;
    private boolean isAtLineStart = false;

    public IndentAwareOutputStream() {
        super();

        spaceBuffer = initSpaceBuffer();
    }

    public IndentAwareOutputStream(int size) {
        super(size);

        spaceBuffer = initSpaceBuffer();
    }

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

    public boolean isAtLineStart() {
        return isAtLineStart;
    }

    public int getCurrentIndentSpaceCount() {
        return currentIndentSpaceCount;
    }

    public int getTotalIndent() {
        return totalIndent;
    }

    public Integer getIndentStackTop() {
        return indentStack.isEmpty() ? null : indentStack.peek();
    }
}