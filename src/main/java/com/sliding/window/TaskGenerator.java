package com.sliding.window;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

/**
 * Created by varunverma on 6/05/2015.
 */
class TaskGenerator implements Runnable {
    private final File file;
    private final BlockingQueue<Double> buffer;

    TaskGenerator(File file, BlockingQueue<Double> buffer) {
        this.file = file;
        this.buffer = buffer;
    }

    @Override
    public void run() {
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextDouble()) {
                double d = scanner.nextDouble();
                buffer.put(d);
            }
        } catch (FileNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}