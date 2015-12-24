package com.zinchenko.concurrent.task.nashorn;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by zdv on 25.12.2015.
 */
public class NashornProcessorTest {

    NashornProcessor processor;
    Thread concurrentThread;

    @Before
    public void setUp() throws Exception {
        processor = new NashornProcessor();
        concurrentThread = new Thread(() -> {
            processor.addEvalScript("while(true);");
        });
        concurrentThread.setName("concurrentThread");
        concurrentThread.start();
        Thread.sleep(100); // let the concurrentThread start before continue on main thread
    }

    @After
    public void tearDown() throws Exception {
        concurrentThread.stop();
        processor = null;
    }

    @Test
    public void testAddEvalScriptBusyIfAlreadyUsed() throws Exception {
        final String result = processor.addEvalScript("1+1");
        System.out.println(result);
        assertEquals("NashornProcessor must return busy status when used from concurrent thread ","Engine is busy", result);
    }
}