/**
 * trida klienta
 */
public class Client {
    /**
     * nickname
     */
    private String userName = "";
    /**
     * stav klienta
     */
    private States state = States.LOGGING;
    /**
     * zdravi uzivatele
     */
    public int health = 100;

    public Client(String userName) {
        this.userName = userName;
    }

    public String getUserName() { return userName; }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public States getState() {
        return state;
    }

    public void setState(States state) {
        this.state = state;
    }

    public void sethealth(int s) {
        this.health = s;
    }
}
