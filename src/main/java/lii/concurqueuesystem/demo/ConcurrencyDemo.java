package lii.concurqueuesystem.demo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;


public class ConcurrencyDemo {
    private static final Logger logger = Logger.getLogger(ConcurrencyDemo.class.getName());

    private static int unsafeCounter = 0;

    private static AtomicInteger safeCounter = new AtomicInteger(0);

    private static int synchronizedCounter = 0;
    private static final Object counterLock = new Object();

    private static final Lock lock1 = new ReentrantLock();
    private static final Lock lock2 = new ReentrantLock();


    public static void demonstrateRaceCondition() {
        logger.info("=== RACE CONDITION DEMONSTRATION ===");

        final int numThreads = 10;
        final int incrementsPerThread = 1000;
        final int expectedTotal = numThreads * incrementsPerThread;

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);

        unsafeCounter = 0;
        safeCounter.set(0);
        synchronizedCounter = 0;

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < incrementsPerThread; j++) {

                        unsafeCounter++;

                        safeCounter.incrementAndGet();

                        synchronized (counterLock) {
                            synchronizedCounter++;
                        }

                        if (j % 100 == 0) {
                            Thread.yield();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(10, TimeUnit.SECONDS);

            logger.info(String.format("Expected total: %d", expectedTotal));
            logger.info(String.format("Unsafe counter result: %d (Lost updates: %d)",
                    unsafeCounter, expectedTotal - unsafeCounter));
            logger.info(String.format("AtomicInteger result: %d (Lost updates: %d)",
                    safeCounter.get(), expectedTotal - safeCounter.get()));
            logger.info(String.format("Synchronized counter result: %d (Lost updates: %d)",
                    synchronizedCounter, expectedTotal - synchronizedCounter));

        } catch (InterruptedException e) {
            logger.severe("Race condition demo interrupted");
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }

        logger.info("=== RACE CONDITION DEMO COMPLETE ===\n");
    }

    public static void demonstrateDeadlock() {
        logger.info("=== DEADLOCK DEMONSTRATION ===");

        logger.info("Creating deadlock scenario...");

        Thread thread1 = new Thread(() -> {
            try {
                logger.info("Thread 1: Acquiring lock1");
                lock1.lock();
                logger.info("Thread 1: Acquired lock1");

                Thread.sleep(100); // Give other thread chance to acquire lock2

                logger.info("Thread 1: Trying to acquire lock2");
                if (lock2.tryLock(2, TimeUnit.SECONDS)) {
                    try {
                        logger.info("Thread 1: Acquired lock2 - No deadlock!");
                    } finally {
                        lock2.unlock();
                        logger.info("Thread 1: Released lock2");
                    }
                } else {
                    logger.warning("Thread 1: Failed to acquire lock2 - DEADLOCK DETECTED!");
                }

            } catch (InterruptedException e) {
                logger.info("Thread 1 interrupted");
                Thread.currentThread().interrupt();
            } finally {
                lock1.unlock();
                logger.info("Thread 1: Released lock1");
            }
        }, "DeadlockDemo-Thread1");

        Thread thread2 = new Thread(() -> {
            try {
                logger.info("Thread 2: Acquiring lock2");
                lock2.lock();
                logger.info("Thread 2: Acquired lock2");

                Thread.sleep(100);

                logger.info("Thread 2: Trying to acquire lock1");
                if (lock1.tryLock(2, TimeUnit.SECONDS)) {
                    try {
                        logger.info("Thread 2: Acquired lock1 - No deadlock!");
                    } finally {
                        lock1.unlock();
                        logger.info("Thread 2: Released lock1");
                    }
                } else {
                    logger.warning("Thread 2: Failed to acquire lock1 - DEADLOCK DETECTED!");
                }

            } catch (InterruptedException e) {
                logger.info("Thread 2 interrupted");
                Thread.currentThread().interrupt();
            } finally {
                lock2.unlock();
                logger.info("Thread 2: Released lock2");
            }
        }, "DeadlockDemo-Thread2");

        thread1.start();
        thread2.start();

        try {
            thread1.join(5000);
            thread2.join(5000);
        } catch (InterruptedException e) {
            logger.severe("Deadlock demo interrupted");
            Thread.currentThread().interrupt();
        }

        logger.info("\nDemonstrating deadlock resolution with ordered locking...");
        demonstrateOrderedLocking();

        logger.info("=== DEADLOCK DEMO COMPLETE ===\n");
    }

    private static void demonstrateOrderedLocking() {
        Thread thread1 = new Thread(() -> {
            try {
                logger.info("Thread 1 (Ordered): Acquiring lock1 first");
                lock1.lock();
                logger.info("Thread 1 (Ordered): Acquired lock1");

                Thread.sleep(50);

                logger.info("Thread 1 (Ordered): Acquiring lock2 second");
                lock2.lock();
                logger.info("Thread 1 (Ordered): Acquired lock2 - SUCCESS!");

                Thread.sleep(100);

            } catch (InterruptedException e) {
                logger.info("Thread 1 (Ordered) interrupted");
                Thread.currentThread().interrupt();
            } finally {
                lock2.unlock();
                lock1.unlock();
                logger.info("Thread 1 (Ordered): Released both locks");
            }
        }, "OrderedLocking-Thread1");

        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(25);

                logger.info("Thread 2 (Ordered): Acquiring lock1 first");
                lock1.lock();
                logger.info("Thread 2 (Ordered): Acquired lock1");

                logger.info("Thread 2 (Ordered): Acquiring lock2 second");
                lock2.lock();
                logger.info("Thread 2 (Ordered): Acquired lock2 - SUCCESS!");

                Thread.sleep(100);

            } catch (InterruptedException e) {
                logger.info("Thread 2 (Ordered) interrupted");
                Thread.currentThread().interrupt();
            } finally {
                lock2.unlock();
                lock1.unlock();
                logger.info("Thread 2 (Ordered): Released both locks");
            }
        }, "OrderedLocking-Thread2");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
            logger.info("Ordered locking completed successfully - No deadlock!");
        } catch (InterruptedException e) {
            logger.severe("Ordered locking demo interrupted");
            Thread.currentThread().interrupt();
        }
    }


    public static void demonstrateVolatileVisibility() {
        logger.info("=== VOLATILE VISIBILITY DEMONSTRATION ===");

        VisibilityDemo demo = new VisibilityDemo();

        Thread writer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(1000);
                    demo.updateValues(i);
                    logger.info(String.format("Writer: Updated values to %d", i));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "VolatileDemo-Writer");

        Thread reader = new Thread(() -> {
            try {
                int lastNonVolatileValue = 0;
                int lastVolatileValue = 0;

                while (!Thread.currentThread().isInterrupted()) {
                    int currentNonVolatile = demo.getNonVolatileValue();
                    int currentVolatile = demo.getVolatileValue();

                    if (currentNonVolatile != lastNonVolatileValue || currentVolatile != lastVolatileValue) {
                        logger.info(String.format("Reader: Non-volatile=%d, Volatile=%d",
                                currentNonVolatile, currentVolatile));
                        lastNonVolatileValue = currentNonVolatile;
                        lastVolatileValue = currentVolatile;
                    }

                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "VolatileDemo-Reader");

        reader.start();
        writer.start();

        try {
            writer.join();
            Thread.sleep(1000);
            reader.interrupt();
            reader.join();
        } catch (InterruptedException e) {
            logger.severe("Volatile demo interrupted");
            Thread.currentThread().interrupt();
        }

        logger.info("=== VOLATILE DEMO COMPLETE ===\n");
    }


    private static class VisibilityDemo {
        private int nonVolatileValue = 0;
        private volatile int volatileValue = 0;

        public void updateValues(int newValue) {
            nonVolatileValue = newValue;
            volatileValue = newValue;
        }

        public int getNonVolatileValue() {
            return nonVolatileValue;
        }

        public int getVolatileValue() {
            return volatileValue;
        }
    }


    public static void runAllDemonstrations() {
        logger.info("Starting concurrency demonstrations...\n");

        demonstrateRaceCondition();
        demonstrateVolatileVisibility();
        demonstrateDeadlock();

        logger.info("All concurrency demonstrations completed.");
    }
}
