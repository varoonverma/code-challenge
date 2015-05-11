package com.sliding.window;

import org.junit.BeforeClass;
import org.junit.Test;

import com.sliding.window.SlideQue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by varunverma on 9/05/2015.
 */
public class TaskGeneratorTest {
    private static List<File> fileList;
    private BlockingQueue<Double> buffer = new LinkedBlockingQueue<>();

    @BeforeClass
    public static void setUp() {
        fileList = new ArrayList<>();
        String[] fileNames = {"input1", "input2"};
        for (String fileName : fileNames) {
            ClassLoader classLoader = TaskGeneratorTest.class.getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            fileList.add(file);
        }
    }

    /**
     * BASIC level of test to check if all the input is parsed
     * and read into the buffer.
     */
    @Test
    public void testProducer() throws InterruptedException {
        SlideQue slideQue = new SlideQue(buffer, null, 0);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        slideQue.startProducers(fileList, executorService);

        executorService.shutdown();
        while (!executorService.isTerminated()) {
            // wait till the threads have read all the data.
            executorService.awaitTermination(2, TimeUnit.SECONDS);
        }
        assertEquals(200, buffer.size());
    }
}