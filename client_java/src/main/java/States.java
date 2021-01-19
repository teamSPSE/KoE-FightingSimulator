public enum States {
    LOGGING(-1),
    IN_LOBBY(0),
    WANNA_PLAY(1),
    DISCONNECT(2),
    YOU_PLAYING(3),
    OPPONENT_PLAYING(4);

    private int index;

    private States(int index) {
        this.index = index;
    }

    public static States getState(int index) {
        States st = null;
        for(States stt : States.values()) {
            if (stt.index == index) {
                st = stt;
                break;
            }
        }

        return st;
    }
}