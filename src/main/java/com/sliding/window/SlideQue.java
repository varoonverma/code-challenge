package com.sliding.window;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by varunverma on 9/05/2015.
 */
public class SlideQue {
    // constants
    private final int WINDOW_SIZE;
    private final int CPU = Runtime.getRuntime().availableProcessors();
    
    private final int WINDOW_IS_EMPTY = 0;
    private final int INPUT_GREATER_THAN_HEAD = 1;
    private final int INPUT_GREATER_THAN_TAIL = 2;
    private final int INPUT_LESSER_THAN_TAIL = 3;

    private int inputValueIndex = -1;

    // not a constant, final used to guard against object re-assignment.
    private final ExecutorService executorService = Executors.newFixedThreadPool(CPU * 2);

    // Most Deque operations run in Amortised constant O(1).
    private final Deque<Item<Double>> window;
    private final BlockingQueue<Double> buffer;

    SlideQue(BlockingQueue<Double> buffer, Deque<Item<Double>> window, int WINDOW_SIZE) {
        this.buffer = buffer;
        this.window = window;
        // guard against illegal WINDOW_SIZE
        this.WINDOW_SIZE = WINDOW_SIZE < 1 ? 1 : WINDOW_SIZE;
    }

    public static void main(String[] args) {
        BlockingQueue<Double> buffer = new LinkedBlockingQueue<>();
        Deque<Item<Double>> window = new ArrayDeque<>();

        SlideQue slideQue = new SlideQue(buffer, window, Integer.parseInt(args[0]));
        List<File> fileList = slideQue.prepareFileList(args);

        slideQue.go(fileList);
    }

    void go(List<File> fileList) {
        // Kick-Off producers threads for processing the list of files.
        startProducers(fileList, executorService);
        // Pop an item from the buffer to process the max sliding window.
        processBuffer();
    }

    void startProducers(List<File> fileList, ExecutorService executorService) {
        for (File file : fileList) {
            executorService.execute(new TaskGenerator(file, buffer));
        }
    }

    void processBuffer() {
        executorService.shutdown();
        // keep processing till the Threads have stopped and Buffer is empty.
        while (!executorService.isTerminated() || !buffer.isEmpty()) {
            Item<Double> inputItem = getItemFromBuffer();

            // don't insert null on the queue, try again.
            if (inputItem != null) {
                double result = getMaxFromWindow(inputItem);
                printOutput(inputItem.getValue(), result);
            }
        }
    }

    /**
     * Algorithm executes in amortized constant time O(1).
     * Deque retains only set of elements eligible for becoming a Maximum
     * in the future, with the current maximum at the HEAD.
     * <p>
     * Elements are removed from the deque based on the value of the new inputItem
     * available from the buffer.
     *
     * @param inputItem next item ready to be pushed on the window.
     * @return The maximum element in the window after the insert operation.
     */
	 double getMaxFromWindow(Item<Double> inputItem) {

		// No processing is required.
		// Each input is MAXIMUM.
		if (WINDOW_SIZE == 1) {
			return inputItem.getValue();
		}

        int scenario = identifyScenario(inputItem);
        
		switch (scenario) {

		case WINDOW_IS_EMPTY:
			window.add(inputItem);
			break;

		case INPUT_GREATER_THAN_HEAD:
			// Empty the queue and insert the inputItem as the new maximum.
			window.clear();
			window.add(inputItem);
			break;

		case INPUT_GREATER_THAN_TAIL:
			// Since values smaller than the new inputItem will never be
			// eligible for selection as maximum, therefore they are removed.
			Item<Double> tailItem;
			do {
				window.removeLast();
				// keep popping items less than the inputItem from the TAIL
				tailItem = window.peekLast();
			} while (inputItem.getValue() > tailItem.getValue());

			window.addLast(inputItem);
			break;

		case INPUT_LESSER_THAN_TAIL:
			window.addLast(inputItem);
			break;
		}

		return window.peekFirst().getValue();
	}

	private int identifyScenario(Item<Double> inputItem) {

		if (window.isEmpty()) {
			return WINDOW_IS_EMPTY;
		}

		// Check the eligibility of HEAD to be a member of WINDOW
		if (inputValueIndex - window.peekFirst().getIndex() >= WINDOW_SIZE) {
			window.poll();
		}

		Item<Double> headItem = window.peekFirst();
		if (inputItem.getValue() >= headItem.getValue()) {
			return INPUT_GREATER_THAN_HEAD;
		}

		Item<Double> tailItem = window.peekLast();
		if (inputItem.getValue() > tailItem.getValue()) {
			return INPUT_GREATER_THAN_TAIL;
		}

		return INPUT_LESSER_THAN_TAIL;
	}

    /**
     * Picks raw data from the buffer and wraps it as an {@link Item}. Besides the raw
     * value, Item also holds the index position This helps to track the sequential position
     * the item had on the buffer.
     *
     * The information of the index helps to keep track of the input's eligibility to
     * stay on the window queue.
     *
     * @return Raw value from the buffer wrapped as an {@link Item}
     */
    Item<Double> getItemFromBuffer() {
        Item<Double> inputItem = null;
        try {
            // if the thread is interrupted from unknown source, it could return a null
            // value to the caller.
            inputItem = new Item<>(buffer.take(), ++inputValueIndex);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return inputItem;
    }

    /**
     * Convert the fileNames to a real File list and return to the user.
     *
     * @param fileNames String array of file names
     * @return List of files based on the input supplied.
     */
    List<File> prepareFileList(String[] fileNames) {
        List<File> fileList = new ArrayList<>();
        for (int i = 1; i < fileNames.length; i++) {
            fileList.add(new File(fileNames[i]));
        }
        return fileList;
    }

    /**
     * Aids in testing as the value is the total number of items processed
     * at the time of method call.
     *
     * @return The total count of items processed.
     */
    int getTotalItemsProcessed() {
        return inputValueIndex + 1;
    }

    private void printOutput(double inputValue, double result) {
        System.out.println(inputValue + " " + result);
    }
}