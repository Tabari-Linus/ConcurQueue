package lii.concurqueuesystem.config;

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ColoredConsoleFormatter extends Formatter {
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";

    @Override
    public String format(LogRecord record) {
        String color = getColorForLevel(record.getLevel());

        String message = record.getMessage();
        if (isFailureRelated(message)) {
            color = RED;
        }

        return String.format("%s[%1$tF %1$tT] [%2$s] %3$s: %4$s%s%n",
                color,
                record.getMillis(),
                record.getLevel(),
                record.getLoggerName().substring(record.getLoggerName().lastIndexOf('.') + 1),
                message,
                RESET);
    }

    private String getColorForLevel(Level level) {
        if (level.intValue() >= Level.SEVERE.intValue()) {
            return RED;
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            return YELLOW;
        } else if (level.intValue() >= Level.INFO.intValue()) {
            return CYAN;
        } else {
            return RESET;
        }
    }

    private boolean isFailureRelated(String message) {
        if (message == null) return false;

        String lowerMessage = message.toLowerCase();
        return lowerMessage.contains("failed") ||
                lowerMessage.contains("failure") ||
                lowerMessage.contains("error") ||
                lowerMessage.contains("exception") ||
                lowerMessage.contains("abandoned") ||
                lowerMessage.contains("retry") ||
                lowerMessage.contains("deadlock") ||
                lowerMessage.contains("stalled");
    }
}
