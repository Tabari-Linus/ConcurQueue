package lii.concurqueuesystem.logging;

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

        String timestamp = String.format("%1$tF %1$tT", record.getMillis());
        String loggerName = record.getLoggerName();
        String shortLoggerName = loggerName.substring(loggerName.lastIndexOf('.') + 1);

        return String.format("%s[%s] [%s] %s: %s%s%n",
                color,
                timestamp,
                record.getLevel(),
                shortLoggerName,
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

        if (lowerMessage.contains("submitted") ||
                lowerMessage.contains("completed") ||
                lowerMessage.contains("started") ||
                lowerMessage.contains("processing") ||
                lowerMessage.contains("success")) {
            return false;
        }

        return lowerMessage.contains("failed") ||
                lowerMessage.contains("failure") ||
                lowerMessage.contains("error") ||
                lowerMessage.contains("exception") ||
                lowerMessage.contains("abandoned") ||
                (lowerMessage.contains("retry") && !lowerMessage.contains("worker")) ||
                lowerMessage.contains("deadlock") ||
                lowerMessage.contains("stalled") ||
                lowerMessage.contains("interrupted") ||
                lowerMessage.contains("timeout");
    }
}