package com.flarestar.drones.base.generation.jtwig;

import flarestar.bdd.annotations.Describe;
import flarestar.bdd.annotations.It;
import flarestar.bdd.runner.Runner;
import org.junit.runner.RunWith;

import static flarestar.bdd.Assert.expect;

@RunWith(Runner.class)
@Describe(IndentAwareOutputStream.class)
public class IndentAwareOutputStreamTest {

    private IndentAwareOutputStream stream;

    public void beforeEach() { // TODO: should run this in nested classes. make change in junit-bdd-lite
        this.stream = new IndentAwareOutputStream();
    }

    @It("should correctly initialize indent detection state after construction")
    public void testConstruction() {
        expect(stream.isAtLineStart()).to().be().false_();
        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(0);
        expect(stream.getTotalIndent()).to().be().equal(0);
        expect(stream.getIndentStackTop()).to().be().null_();
    }

    @It("should write bytes w/o inserting indents if there is no newline in the byte array when #write() is called")
    public void testWriteSuccess1() throws Throwable {
        byte[] bytes = "data".getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.toString("UTF-8")).to().be().equal("data");
        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(0);
        expect(stream.getTotalIndent()).to().be().equal(0);
        expect(stream.isAtLineStart()).to().be().true_(); // TODO: probably should be false
    }

    @It("should remember indents after writing a newline character when #write() is called")
    public void testWriteSuccess2() throws Throwable {
        String data = "abc\n  \t ";

        byte[] bytes = data.getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.toString("UTF-8")).to().be().equal(data);
        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(7);
        expect(stream.getTotalIndent()).to().be().equal(0);
        expect(stream.isAtLineStart()).to().be().false_();
    }

    @It("should reset the current indent count when a newline is encountered during #write()")
    public void testWriteSuccess3() throws Throwable {
        String firstChunk = "abc\n  ";

        byte[] bytes = firstChunk.getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(2);

        String secondChunk = "something\n";

        bytes = secondChunk.getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(0);
        expect(stream.isAtLineStart()).to().be().true_();
    }

    @It("should remember the current indent count when #pushIndent() is called")
    public void testPushIndentSuccess1() throws Throwable {
        byte[] bytes = "abc\n  ".getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(2);

        stream.pushIndent();

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(0);
        expect(stream.isAtLineStart()).to().be().false_();
        expect(stream.getTotalIndent()).to().be().equal(2);
        expect(stream.getIndentStackTop()).to().be().equal(2);
    }

    @It("should accumulate indents when #pushIndent() is called multiple times")
    public void testPushIndentSuccess2() throws Throwable {
        byte[] bytes = "abc\n  ".getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(2);

        stream.pushIndent();

        bytes = "def\n   ".getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(3);

        stream.pushIndent();

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(0);
        expect(stream.isAtLineStart()).to().be().false_();
        expect(stream.getTotalIndent()).to().be().equal(5);
        expect(stream.getIndentStackTop()).to().be().equal(3);
    }

    @It("should insert the accumulated total indent when #write() is called after #pushIndent()")
    public void testWriteSuccess4() throws Throwable {
        byte[] bytes = "abc\n  ".getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(2);

        stream.pushIndent();

        bytes = "def\n   ghi\n hij".getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.toString("UTF-8")).to().be().equal("abc\n  def\n     ghi\n   hij");
    }

    @It("should pop and reset the current indent space count when #popIndent() is called after a #pushIndent()")
    public void testPopIndentSuccess() throws Throwable {
        byte[] bytes = "abc\n  ".getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(2);

        stream.pushIndent();

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(0);
        expect(stream.getIndentStackTop()).to().be().equal(2);
        expect(stream.getTotalIndent()).to().be().equal(2);

        stream.popIndent();

        expect(stream.getCurrentIndentSpaceCount()).to().be().equal(2);
        expect(stream.getIndentStackTop()).to().be().null_();
        expect(stream.getTotalIndent()).to().be().equal(0);
    }

    @It("should correctly insert indents when #pushIndent() and #popIndent() called during a series of #write() calls")
    public void testWriteSuccess5() throws Throwable {
        byte[] bytes = "abc\n  ".getBytes();
        stream.write(bytes, 0, bytes.length);

        stream.pushIndent();

        expect(stream.toString("UTF-8")).to().be().equal("abc\n  ");

        bytes = "def\n \n  ghi\n   ".getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.toString("UTF-8")).to().be().equal("abc\n  def\n   \n    ghi\n     ");

        stream.pushIndent();

        bytes = "jkl\n  mnop".getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.toString("UTF-8")).to().be().equal("abc\n  def\n   \n    ghi\n     jkl\n       mnop");

        stream.popIndent();

        bytes = "\nqrst\nuvw".getBytes();
        stream.write(bytes, 0, bytes.length);

        expect(stream.toString("UTF-8")).to().be().equal("abc\n  def\n   \n    ghi\n     jkl\n       mnop\n  qrst\n  uvw");
    }
}
