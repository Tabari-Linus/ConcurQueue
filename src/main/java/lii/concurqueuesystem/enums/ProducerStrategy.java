package lii.concurqueuesystem.enums;

import java.util.Random;

public enum ProducerStrategy {

    HIGH_PRIORITY_FOCUSED {
        @Override
        public int generatePriority(Random random) {
            return random.nextDouble() < 0.3 ?
                    1 + random.nextInt(4) : 4 + random.nextInt(6);
        }

        @Override
        public String generatePayload(Random random, int taskNumber) {
            return String.format("HIGH_PRIORITY_TASK_%d_DATA_%d",
                    taskNumber, random.nextInt(1000));
        }
    },

    BALANCED {
        @Override
        public int generatePriority(Random random) {
            return 1 + random.nextInt(10);
        }

        @Override
        public String generatePayload(Random random, int taskNumber) {
            return String.format("BALANCED_TASK_%d_PAYLOAD_%s",
                    taskNumber, generateRandomString(random, 20));
        }
    },

    LOW_PRIORITY_BATCH {
        @Override
        public int generatePriority(Random random) {
            return random.nextDouble() > 0.3 ?
                    4 + random.nextInt(7) : 1 + random.nextInt(3);
        }

        @Override
        public String generatePayload(Random random, int taskNumber) {
            return String.format("BATCH_PROCESSING_TASK_%d_CHUNK_%d",
                    taskNumber, random.nextInt(100));
        }
    };

    public abstract int generatePriority(Random random);
    public abstract String generatePayload(Random random, int taskNumber);

    protected String generateRandomString(Random random, int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
