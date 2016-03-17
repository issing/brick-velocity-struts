package net.isger.brick;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BrickVelocityStrutsTest extends TestCase {

    public BrickVelocityStrutsTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(BrickVelocityStrutsTest.class);
    }

    public void testVelocity() {
        assertTrue(true);
    }

}
