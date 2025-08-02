package memorius.application;

public class ApplicationRunner {
    public static void main(String[] args) {
        int port = 6379;
        Starter starter = new Starter();
        starter.start(port);
    }
}
