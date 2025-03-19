package ch.unibas.dmi.dbis.cs108;

public class SETTINGS {
    public enum Config {
        PING_INTERVAL(1000),
        TIMEOUT(10 * 1000);

        private final int value;

        Config(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
