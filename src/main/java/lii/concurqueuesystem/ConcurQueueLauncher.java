package lii.concurqueuesystem;

import lii.concurqueuesystem.demo.ConcurrencyDemo;
import lii.concurqueuesystem.menu.Menu;


public class ConcurQueueLauncher {

        public static void main(String[] args) {
            if (args.length > 0) {
                String mode = args[0].toLowerCase();

                switch (mode) {
                    case "direct":
                    case "auto":
                        ConcurQueueSystemApplication.main(new String[]{"direct"});
                        break;
                    case "demo":
                        System.out.println("Running Concurrency Demonstrations...\n");
                        ConcurrencyDemo.runAllDemonstrations();
                        break;
                    case "help":
                    case "-h":
                    case "--help":
                        printUsage();
                        break;
                    default:
                        System.out.println("Unknown option: " + args[0]);
                        printUsage();
                        break;
                }
            } else {
                Menu.main(args);
            }
        }

        private static void printUsage() {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("CONCURQUEUE SYSTEM - USAGE");
            System.out.println("=".repeat(60));
            System.out.println("Interactive Mode (default):");
            System.out.println("  java ConcurQueueLauncher");
            System.out.println();
            System.out.println("Direct Modes:");
            System.out.println("  java ConcurQueueLauncher direct   - Run full system (2 min)");
            System.out.println("  java ConcurQueueLauncher demo     - Run concurrency demos only");
            System.out.println("  java ConcurQueueLauncher help     - Show this help message");
            System.out.println("=".repeat(60) + "\n");
        }
}
