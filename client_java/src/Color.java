public enum Color {
    Black("black", "#000000"),
    White("white", "#ffffff"),
    Red("red", "#f44242"),
    Green("green", "#5c9952"),
    Blue("blue", "#4c02f9"),
    Yellow("yellow", "#ffe900")
    ;

    private String name;
    private String hexColor;

    private Color(String name) {
        this.name = name;
        this.hexColor = null;
    }

    private Color(String name, String hexColor) {
        this.name = name;
        this.setHexColor(hexColor);
    }

    public String geColorName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public static Color getColor(String color) {
        Color clr = null;
        for(Color cl : Color.values()) {
            if (cl.name.equals(color)) {
                clr = cl;
                break;
            }
        }

        return clr;
    }
}