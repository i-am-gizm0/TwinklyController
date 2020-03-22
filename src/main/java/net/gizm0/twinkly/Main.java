package net.gizm0.twinkly;

public class Main {

    /**
     * The main class run when the jar is executed
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                CLI.main(args);
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        } else {
            GUI.main(args);
        }
    }
}
