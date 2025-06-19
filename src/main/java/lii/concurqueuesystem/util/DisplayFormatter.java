package lii.concurqueuesystem.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DisplayFormatter {

    private static final String RESET = "\u001B[0m";
    private static final String BRIGHT_BLUE = "\u001B[94m";
    private static final String BRIGHT_GREEN = "\u001B[92m";
    private static final String BRIGHT_YELLOW = "\u001B[93m";
    private static final String BRIGHT_RED = "\u001B[91m";
    private static final String BRIGHT_CYAN = "\u001B[96m";
    private static final String BRIGHT_MAGENTA = "\u001B[95m";
    private static final String BOLD = "\u001B[1m";
    private static final String DIM = "\u001B[2m";

    private static final String TOP_LEFT = "╔";
    private static final String TOP_RIGHT = "╗";
    private static final String BOTTOM_LEFT = "╚";
    private static final String BOTTOM_RIGHT = "╝";
    private static final String HORIZONTAL = "═";
    private static final String VERTICAL = "║";
    private static final String T_DOWN = "╦";
    private static final String T_UP = "╩";
    private static final String T_RIGHT = "╠";
    private static final String T_LEFT = "╣";
    private static final String CROSS = "╬";

    public static String createSystemStatusDisplay(
            int mainQueueSize, int retryQueueSize, int activeThreads,
            int corePoolSize, int maxPoolSize, long completedTasks,
            int submitted, int processing, int completed, int failed,
            int retry, int abandoned, int totalProcessed, double avgTime) {

        StringBuilder display = new StringBuilder();

        display.append(BRIGHT_CYAN).append(BOLD);
        display.append("\n").append(TOP_LEFT).append(HORIZONTAL.repeat(58)).append(TOP_RIGHT).append("\n");
        display.append(VERTICAL).append("                    📊 SYSTEM STATUS MONITOR                   ").append(VERTICAL).append("\n");
        display.append(VERTICAL).append("                  ").append(getCurrentTimestamp()).append("                  ").append(VERTICAL).append("\n");
        display.append(T_RIGHT).append(HORIZONTAL.repeat(58)).append(T_LEFT).append("\n");
        display.append(RESET);

        display.append(BRIGHT_BLUE).append(VERTICAL).append(" 🗃️  QUEUE STATUS").append(" ".repeat(43)).append(VERTICAL).append(RESET).append("\n");
        display.append(VERTICAL).append(String.format("   Main Queue: %s%-6d%s │ Retry Queue: %s%-6d%s          ",
                getQueueColor(mainQueueSize), mainQueueSize, RESET,
                getQueueColor(retryQueueSize), retryQueueSize, RESET)).append(VERTICAL).append("\n");

        display.append(BRIGHT_CYAN).append(T_RIGHT).append(HORIZONTAL.repeat(58)).append(T_LEFT).append(RESET).append("\n");

        display.append(BRIGHT_GREEN).append(VERTICAL).append(" 🧵 THREAD POOL STATUS").append(" ".repeat(37)).append(VERTICAL).append(RESET).append("\n");
        display.append(VERTICAL).append(String.format("   Active: %s%-3d%s │ Core: %-3d │ Max: %-3d │ Completed: %s%-8d%s",
                getThreadColor(activeThreads, corePoolSize), activeThreads, RESET,
                corePoolSize, maxPoolSize,
                BRIGHT_GREEN, completedTasks, RESET)).append(VERTICAL).append("\n");

        display.append(BRIGHT_CYAN).append(T_RIGHT).append(HORIZONTAL.repeat(58)).append(T_LEFT).append(RESET).append("\n");

        display.append(BRIGHT_YELLOW).append(VERTICAL).append(" 📋 TASK STATUS BREAKDOWN").append(" ".repeat(33)).append(VERTICAL).append(RESET).append("\n");
        display.append(VERTICAL).append(String.format("   Submitted: %s%-4d%s │ Processing: %s%-4d%s │ Completed: %s%-4d%s   ",
                BRIGHT_CYAN, submitted, RESET,
                BRIGHT_YELLOW, processing, RESET,
                BRIGHT_GREEN, completed, RESET)).append(VERTICAL).append("\n");
        display.append(VERTICAL).append(String.format("   Failed: %s%-7d%s │ Retry: %s%-7d%s │ Abandoned: %s%-4d%s   ",
                BRIGHT_RED, failed, RESET,
                BRIGHT_MAGENTA, retry, RESET,
                BRIGHT_RED, abandoned, RESET)).append(VERTICAL).append("\n");

        display.append(BRIGHT_CYAN).append(T_RIGHT).append(HORIZONTAL.repeat(58)).append(T_LEFT).append(RESET).append("\n");

        display.append(BRIGHT_MAGENTA).append(VERTICAL).append(" ⚡ PERFORMANCE METRICS").append(" ".repeat(36)).append(VERTICAL).append(RESET).append("\n");
        display.append(VERTICAL).append(String.format("   Total Processed: %s%-8d%s │ Avg Time: %s%.2f ms%s      ",
                BRIGHT_GREEN, totalProcessed, RESET,
                getPerformanceColor(avgTime), avgTime, RESET)).append(VERTICAL).append("\n");

        display.append(BRIGHT_CYAN).append(BOTTOM_LEFT).append(HORIZONTAL.repeat(58)).append(BOTTOM_RIGHT).append(RESET).append("\n");

        return display.toString();
    }

    public static String createFinalStatisticsDisplay(
            int totalProcessed, double avgProcessingTime, int remainingQueue,
            int retryQueue, int totalTracked, Map<String, Long> statusBreakdown) {

        StringBuilder display = new StringBuilder();

        display.append(BRIGHT_GREEN).append(BOLD);
        display.append("\n").append("🎊").append(HORIZONTAL.repeat(66)).append("🎊").append("\n");
        display.append("🏆                    FINAL SYSTEM STATISTICS                    🏆").append("\n");
        display.append("🎊").append(HORIZONTAL.repeat(66)).append("🎊").append("\n");
        display.append(RESET);

        display.append(BRIGHT_CYAN).append(TOP_LEFT).append(HORIZONTAL.repeat(66)).append(TOP_RIGHT).append(RESET).append("\n");

        display.append(BRIGHT_BLUE).append(VERTICAL).append(" 📈 PROCESSING SUMMARY").append(" ".repeat(45)).append(VERTICAL).append(RESET).append("\n");
        display.append(BRIGHT_CYAN).append(T_RIGHT).append(HORIZONTAL.repeat(66)).append(T_LEFT).append(RESET).append("\n");

        display.append(VERTICAL).append(String.format("   🎯 Total Tasks Processed: %s%s%-12d%s                    ",
                BOLD, BRIGHT_GREEN, totalProcessed, RESET)).append(VERTICAL).append("\n");
        display.append(VERTICAL).append(String.format("   ⏱️  Average Processing Time: %s%s%.2f ms%s                  ",
                BOLD, getPerformanceColor(avgProcessingTime), avgProcessingTime, RESET)).append(VERTICAL).append("\n");
        display.append(VERTICAL).append(String.format("   📊 Processing Rate: %s%s%.2f tasks/sec%s                   ",
                BOLD, BRIGHT_YELLOW, totalProcessed / (avgProcessingTime / 1000.0), RESET)).append(VERTICAL).append("\n");

        display.append(BRIGHT_CYAN).append(T_RIGHT).append(HORIZONTAL.repeat(66)).append(T_LEFT).append(RESET).append("\n");

        display.append(BRIGHT_YELLOW).append(VERTICAL).append(" 🗂️  QUEUE STATUS").append(" ".repeat(50)).append(VERTICAL).append(RESET).append("\n");
        display.append(BRIGHT_CYAN).append(T_RIGHT).append(HORIZONTAL.repeat(66)).append(T_LEFT).append(RESET).append("\n");

        display.append(VERTICAL).append(String.format("   📥 Remaining in Main Queue: %s%-8d%s                     ",
                getRemainingColor(remainingQueue), remainingQueue, RESET)).append(VERTICAL).append("\n");
        display.append(VERTICAL).append(String.format("   🔄 Tasks in Retry Queue: %s%-8d%s                        ",
                getRemainingColor(retryQueue), retryQueue, RESET)).append(VERTICAL).append("\n");
        display.append(VERTICAL).append(String.format("   📊 Total Tasks Tracked: %s%-8d%s                         ",
                BRIGHT_CYAN, totalTracked, RESET)).append(VERTICAL).append("\n");

        display.append(BRIGHT_CYAN).append(T_RIGHT).append(HORIZONTAL.repeat(66)).append(T_LEFT).append(RESET).append("\n");

        display.append(BRIGHT_MAGENTA).append(VERTICAL).append(" 📋 DETAILED STATUS BREAKDOWN").append(" ".repeat(37)).append(VERTICAL).append(RESET).append("\n");
        display.append(BRIGHT_CYAN).append(T_RIGHT).append(HORIZONTAL.repeat(66)).append(T_LEFT).append(RESET).append("\n");

        for (Map.Entry<String, Long> entry : statusBreakdown.entrySet()) {
            String status = entry.getKey();
            Long count = entry.getValue();
            String icon = getStatusIcon(status);
            String color = getStatusColor(status);

            display.append(VERTICAL).append(String.format("   %s %s%s%s: %s%-12d%s                           ",
                    icon, color, status, RESET, color, count, RESET)).append(VERTICAL).append("\n");
        }

        display.append(BRIGHT_CYAN).append(T_RIGHT).append(HORIZONTAL.repeat(66)).append(T_LEFT).append(RESET).append("\n");

        long completed = statusBreakdown.getOrDefault("COMPLETED", 0L);
        double successRate = totalTracked > 0 ? (double) completed / totalTracked * 100 : 0;

        display.append(BRIGHT_GREEN).append(VERTICAL).append(String.format(" 🎯 SUCCESS RATE: %s%.1f%%%s",
                BOLD, successRate, RESET)).append(" ".repeat(48)).append(VERTICAL).append(RESET).append("\n");

        display.append(BRIGHT_CYAN).append(BOTTOM_LEFT).append(HORIZONTAL.repeat(66)).append(BOTTOM_RIGHT).append(RESET).append("\n");

        display.append(BRIGHT_GREEN).append("🌟").append(HORIZONTAL.repeat(66)).append("🌟").append(RESET).append("\n");
        display.append(BRIGHT_GREEN).append(BOLD).append("    🎉 CONCURQUEUE SYSTEM ANALYSIS COMPLETE! 🎉").append(RESET).append("\n");
        display.append(BRIGHT_GREEN).append("🌟").append(HORIZONTAL.repeat(66)).append("🌟").append(RESET).append("\n");

        return display.toString();
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private static String getQueueColor(int size) {
        if (size > 50) return BRIGHT_RED;
        if (size > 20) return BRIGHT_YELLOW;
        return BRIGHT_GREEN;
    }

    private static String getThreadColor(int active, int core) {
        if (active >= core) return BRIGHT_YELLOW;
        return BRIGHT_GREEN;
    }

    private static String getPerformanceColor(double avgTime) {
        if (avgTime > 3000) return BRIGHT_RED;
        if (avgTime > 1500) return BRIGHT_YELLOW;
        return BRIGHT_GREEN;
    }

    private static String getRemainingColor(int remaining) {
        if (remaining > 10) return BRIGHT_YELLOW;
        if (remaining > 0) return BRIGHT_CYAN;
        return BRIGHT_GREEN;
    }

    private static String getStatusIcon(String status) {
        switch (status.toUpperCase()) {
            case "COMPLETED": return "✅";
            case "PROCESSING": return "⚙️";
            case "SUBMITTED": return "📤";
            case "FAILED": return "❌";
            case "RETRY": return "🔄";
            case "ABANDONED": return "⚠️";
            default: return "📊";
        }
    }

    private static String getStatusColor(String status) {
        switch (status.toUpperCase()) {
            case "COMPLETED": return BRIGHT_GREEN;
            case "PROCESSING": return BRIGHT_YELLOW;
            case "SUBMITTED": return BRIGHT_CYAN;
            case "FAILED": return BRIGHT_RED;
            case "RETRY": return BRIGHT_MAGENTA;
            case "ABANDONED": return BRIGHT_RED;
            default: return RESET;
        }
    }
}