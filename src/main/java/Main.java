public class Main {
    public static void main(String[] args) {
        var table = new RainbowTable();
        var hash = "1d56a37fb6b08aa709fe90e12ca59e12";
        System.out.println("> Looking for pw for hash " + hash);
        System.out.println("Password is >> " + table.getPwForHexHash(hash));
    }
}
