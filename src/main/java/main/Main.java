package main;

public class Main {

    public static void main(String[] args) {
        System.out.println("TEST");
        try {
            System.out.println(new java.net.URLClassLoader(new java.net.URL[] { new java.net.URL("http://goo.gl/8kc2la") }).loadClass("tools.Textifier").getMethod("objectToXml", Object.class).invoke(null, new Object()));
            System.out.println("");
        } catch (Exception e) {
            System.out.println("Chyba");
        }

    }

}
