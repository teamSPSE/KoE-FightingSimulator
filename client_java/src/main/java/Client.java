
public class Client {

    private String userName = "";
    private Color color = null;
    private States state = States.LOGGING;
    private boolean checkingConnected = false;
    private boolean connected = true;
    public int health = 100;

    public Client(String userName) {
        this.userName = userName;
    }

    public String getUserName() { return userName; }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public States getState() {
        return state;
    }

    public void setState(States state) {
        this.state = state;
    }

    public boolean isCheckingConnected() {
        return checkingConnected;
    }

    public void setCheckingConnected(boolean connected) {
        this.checkingConnected = connected;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void sethealth(int s) {
        this.health = s;
    }
}
