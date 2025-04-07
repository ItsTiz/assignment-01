package pcd.version2.utils;

public class Utils {

    public static void log(String message, String threadName) {
        System.out.println("[" + System.currentTimeMillis() + "][" + threadName + "] " + message);
    }
}
