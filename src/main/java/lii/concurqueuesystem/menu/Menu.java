package lii.concurqueuesystem.menu;

import lii.concurqueuesystem.ConcurQueueSystemApplication;
import lii.concurqueuesystem.demo.ConcurrencyDemo;
import lii.concurqueuesystem.logging.TaskLogger;

import java.util.Scanner;
import java.util.logging.Logger;

public class Menu {

    private static final Logger logger = Logger.getLogger(Menu.class.getName());
    private static final TaskLogger taskLogger = new TaskLogger(Menu.class);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        displayWelcomeBanner();

        while (true) {
            displayMainMenu();
            int choice = getUserChoice();

            if (!handleMenuChoice(choice)) {
                break;
            }

            if (choice != 9) {
                System.out.println("\nPress Enter to return to main menu...");
                scanner.nextLine();
            }
        }

        taskLogger.logSystemEvent("Thank you for using ConcurQueue System!");
        scanner.close();
    }

    private static void displayWelcomeBanner() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 CONCURQUEUE - MULTITHREADED JOB PROCESSING PLATFORM 🚀");
        System.out.println("=".repeat(60));
        System.out.println("Educational platform for exploring concurrent programming concepts");
        System.out.println("=".repeat(60) + "\n");
    }

    private static void displayMainMenu() {
        System.out.println("\n📋 SIMULATION MENU");
        System.out.println("━".repeat(40));
        System.out.println("1️⃣  Demonstrate Synchronization Challenges");
        System.out.println("2️⃣  Run Full Queue System (2 minutes)");
        System.out.println("3️⃣  Quick System Demo (30 seconds)");
        System.out.println("4️⃣  Performance Stress Test");
        System.out.println("5️⃣  Deadlock Detection Demo");
        System.out.println("6️⃣  Thread Pool Analysis");
        System.out.println("7️⃣  Queue Behavior Analysis");
        System.out.println("8️⃣  Custom Configuration");
        System.out.println("9️⃣  Exit");
        System.out.println("━".repeat(40));
        System.out.print("Select an option (1-9): ");
    }

    private static int getUserChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static boolean handleMenuChoice(int choice) {
        System.out.println();

        switch (choice) {
            case 1:
                runSynchronizationDemo();
                break;
            case 2:
                runFullSystem();
                break;
            case 3:
                runQuickDemo();
                break;
            case 4:
                runStressTest();
                break;
            case 5:
                runDeadlockDemo();
                break;
            case 6:
                runThreadPoolAnalysis();
                break;
            case 7:
                runQueueAnalysis();
                break;
            case 8:
                runCustomConfiguration();
                break;
            case 9:
                taskLogger.logSystemEvent("Exiting ConcurQueue System...");
                return false;
            default:
                taskLogger.logSystemWarning("Invalid option. Please select 1-9.");
                break;
        }

        return true;
    }

    private static void runSynchronizationDemo() {
        taskLogger.logSystemEvent("🔄 Starting Synchronization Challenges Demo...");
        System.out.println("This demo will show race conditions, deadlocks, and volatile visibility issues.\n");

        try {
            ConcurrencyDemo.runAllDemonstrations();
            taskLogger.logSystemEvent("✅ Synchronization demo completed successfully!");
        } catch (Exception e) {
            taskLogger.logSystemError("❌ Error during synchronization demo: " + e.getMessage());
        }
    }

    private static void runFullSystem() {
        taskLogger.logSystemEvent("🏭 Starting Full Queue System (2 minutes)...");
        System.out.println("Running complete system with producers, consumers, retry logic, and monitoring.\n");

        try {
            ConcurQueueSystemApplication system = new ConcurQueueSystemApplication();
            system.start();

            Thread.sleep(120000);

            taskLogger.logSystemEvent("Demonstrating concurrency concepts...");
            ConcurrencyDemo.runAllDemonstrations();

            system.shutdown();
            taskLogger.logSystemEvent("✅ Full system demo completed!");

        } catch (InterruptedException e) {
            taskLogger.logSystemEvent("Full system demo interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            taskLogger.logSystemError("❌ Error during full system demo: " + e.getMessage());
        }
    }

    private static void runQuickDemo() {
        taskLogger.logSystemEvent("⚡ Starting Quick Demo (30 seconds)...");
        System.out.println("Quick demonstration of the queue system in action.\n");

        try {
            ConcurQueueSystemApplication system = new ConcurQueueSystemApplication();
            system.start();

            Thread.sleep(10000);

            system.shutdown();
            taskLogger.logSystemEvent("✅ Quick demo completed!");

        } catch (InterruptedException e) {
            taskLogger.logSystemEvent("Quick demo interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            taskLogger.logSystemError("❌ Error during quick demo: " + e.getMessage());
        }
    }

    private static void runStressTest() {
        taskLogger.logSystemEvent("💪 Starting Performance Stress Test...");
        System.out.println("Running high-load simulation to test system limits.\n");

        System.out.print("Enter number of producer threads (default 10): ");
        String input = scanner.nextLine().trim();
        int producers = input.isEmpty() ? 10 : Integer.parseInt(input);

        System.out.print("Enter test duration in seconds (default 60): ");
        input = scanner.nextLine().trim();
        int duration = input.isEmpty() ? 60 : Integer.parseInt(input);

        try {
            taskLogger.logSystemEvent(String.format("Running stress test with %d producers for %d seconds", producers, duration));

            ConcurQueueSystemApplication system = new ConcurQueueSystemApplication();
            system.start();
            Thread.sleep(duration * 1000L);
            system.shutdown();

            taskLogger.logSystemEvent("✅ Stress test completed!");

        } catch (Exception e) {
            taskLogger.logSystemError("❌ Error during stress test: " + e.getMessage());
        }
    }

    private static void runDeadlockDemo() {
        taskLogger.logSystemEvent("🔒 Starting Deadlock Detection Demo...");
        System.out.println("Demonstrating deadlock scenarios and prevention techniques.\n");

        try {
            ConcurrencyDemo.demonstrateDeadlock();
            taskLogger.logSystemEvent("✅ Deadlock demo completed!");
        } catch (Exception e) {
            taskLogger.logSystemError("❌ Error during deadlock demo: " + e.getMessage());
        }
    }

    private static void runThreadPoolAnalysis() {
        taskLogger.logSystemEvent("🧵 Starting Thread Pool Analysis...");
        System.out.println("Analyzing thread pool behavior and performance characteristics.\n");

        try {
            ConcurQueueSystemApplication system = new ConcurQueueSystemApplication();
            system.start();

            taskLogger.logSystemEvent("Collecting thread pool metrics for 45 seconds...");
            Thread.sleep(45000);

            system.shutdown();
            taskLogger.logSystemEvent("✅ Thread pool analysis completed!");

        } catch (Exception e) {
            taskLogger.logSystemError("❌ Error during thread pool analysis: " + e.getMessage());
        }
    }

    private static void runQueueAnalysis() {
        taskLogger.logSystemEvent("📊 Starting Queue Behavior Analysis...");
        System.out.println("Analyzing queue dynamics, priority handling, and throughput.\n");

        try {
            taskLogger.logSystemEvent("Demonstrating race conditions first...");
            ConcurrencyDemo.demonstrateRaceCondition();

            taskLogger.logSystemEvent("Now running queue system for analysis...");
            ConcurQueueSystemApplication system = new ConcurQueueSystemApplication();
            system.start();
            Thread.sleep(60000);
            system.shutdown();

            taskLogger.logSystemEvent("✅ Queue analysis completed!");

        } catch (Exception e) {
            taskLogger.logSystemError("❌ Error during queue analysis: " + e.getMessage());
        }
    }

    private static void runCustomConfiguration() {
        taskLogger.logSystemEvent("⚙️ Starting Custom Configuration...");
        System.out.println("Configure your own simulation parameters.\n");

        try {
            System.out.print("Enter worker pool size (default 5): ");
            String workers = scanner.nextLine().trim();

            System.out.print("Enter queue capacity (default 100): ");
            String capacity = scanner.nextLine().trim();

            System.out.print("Enter simulation duration in seconds (default 60): ");
            String duration = scanner.nextLine().trim();

            System.out.print("Include concurrency demos? (y/n, default n): ");
            String includeDemos = scanner.nextLine().trim().toLowerCase();

            int durationSec = duration.isEmpty() ? 60 : Integer.parseInt(duration);

            taskLogger.logSystemEvent("Starting custom configuration...");
            taskLogger.logSystemEvent(String.format("Workers: %s, Capacity: %s, Duration: %ds",
                    workers.isEmpty() ? "5" : workers,
                    capacity.isEmpty() ? "100" : capacity,
                    durationSec));

            ConcurQueueSystemApplication system = new ConcurQueueSystemApplication();
            system.start();
            Thread.sleep(durationSec * 1000L);

            if ("y".equals(includeDemos) || "yes".equals(includeDemos)) {
                taskLogger.logSystemEvent("Running concurrency demonstrations...");
                ConcurrencyDemo.runAllDemonstrations();
            }

            system.shutdown();
            taskLogger.logSystemEvent("✅ Custom configuration completed!");

        } catch (Exception e) {
            taskLogger.logSystemError("❌ Error during custom configuration: " + e.getMessage());
        }
    }
}