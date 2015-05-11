package com.sliding.window;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sliding.window.Item;
import com.sliding.window.SlideQue;

import java.io.File;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by varunverma on 9/05/2015.
 */
public class SlideQueTest {
    private SlideQue slideQue;
    private List<Double> resultList;
    private BlockingQueue<Double> buffer;
    private Deque<Item<Double>> window;
    private static String[] fileNames = {"input1", "input2", "input3"}; 
    private static List<File> fileList;

    @BeforeClass
    public static void setUp() {
        fileList = new ArrayList<>();
        for (String fileName : fileNames) {
            ClassLoader classLoader = TaskGeneratorTest.class.getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            fileList.add(file);
        }
    }

    @Before
    public void initialize() {
        buffer = new LinkedBlockingQueue<>(Arrays.asList(86.99, 31.4, -65.77, -44.31, 53.96, -33.68, -38.23, -62.65, 54.23, 7.05, 97.26, 74.82, 72.93, -26.01, 39.5, 44.66, 4.56, -9.4, -98.38, -26.23));
        resultList = new ArrayList<>(Arrays.asList(86.99, 86.99, 86.99, 31.4, 53.96, 53.96, 53.96, -33.68, 54.23, 54.23, 97.26, 97.26, 97.26, 74.82, 72.93, 44.66, 44.66, 44.66, 4.56, -9.4, -9.4));
        window = new ArrayDeque<>();
    }

    /**
     * Items from the buffer are passed to the getGetMaxFromWindow function one by one and
     * the maximum value(actual result) is compared with the
     * pre-calculated result.
     */
    @Test
    public void testGetMaxFromWindow() {
        slideQue = new SlideQue(buffer, window, 3);
        while(!buffer.isEmpty()) {
        	Item<Double> item = slideQue.getItemFromBuffer();
            if (item != null) {
                double actualResult = slideQue.getMaxFromWindow(item);
                double expectedResult = resultList.remove(0);
                assertEquals(expectedResult, actualResult, 0.0);
            }
        }
    }

    /**
     * An invalid WINDOW_SIZE argument will default the WINDOW_SIZE to be set as
     * 1. Therefore, every item received from the buffer will be the maximum item.
     * 
     * Hence, the output will mirror the buffer. 
     */
    @Test
    public void testInvalidWindowSize() {
        slideQue = new SlideQue(buffer, window, -10);
        
        // since WINDOW_SIZE is one, output mirrors the input buffer.
        resultList.clear();
        resultList.addAll(buffer);
        
        while(!buffer.isEmpty()) {
        	Item<Double> item = slideQue.getItemFromBuffer();
            if (item != null) {
                double actualResult = slideQue.getMaxFromWindow(item);
                double expectedResult = resultList.remove(0);
                assertEquals(expectedResult, actualResult, 0.0);
            }
        }
    }


    /**
     * Tests to check if the E2E scenario processes all the items supplied to the
     * buffer. The logic for testing the correctness of the algorithm for finding the
     * maximum is tested in another unit test {@link #testGetMaxFromWindow}.
     */
    @Test
    public void testE2E() {
        slideQue = new SlideQue(new LinkedBlockingQueue<Double>(), window, 3);
        slideQue.go(fileList);
        assertEquals(300, slideQue.getTotalItemsProcessed());
    }

    @Test
    public void testPrepareFileList() {
        slideQue = new SlideQue(null, null, -1);
        
        List<String> fileNamesList = new ArrayList<>(Arrays.asList(fileNames));
        List<File> fileList = slideQue.prepareFileList(fileNames);
        for (File file : fileList) {
            assertTrue(fileNamesList.contains(file.getName()));
        }
    }
}