import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RainbowTable {
    private final int pwLength;
    private final int pwCharactersAmount = 36;
    private final int chainLength;
    private final int pwAmount;

    private final String[][] table;

    public RainbowTable(int pwLength, int chainLength, int pwAmount) {
        this.pwLength = pwLength;
        this.chainLength = chainLength;
        this.pwAmount = pwAmount;
        table = new String[pwAmount][2];
        System.out.println("> Generating rainbow table ..");
        generateInitialTable();
        System.out.println("> Table generated!");
    }

    public RainbowTable() {
        this(7, 2000, 2000);
    }

    public String getPwForHexHash(String hexHash) {
        var hash = new BigInteger(hexHash, 16);
        var rainbowIndex = getRainbowTableEntryForHash(hash);
        System.out.println("> Found matching chain end for index " + rainbowIndex + ", chain start " + table[rainbowIndex][0] + ", chain end" + table[rainbowIndex][1]);
        return getPwForMatchingHash(table[rainbowIndex][0], hash);
    }

    private int getRainbowTableEntryForHash(BigInteger hash) {
        System.out.println("> Testing for reduction index");
        for(int startIndex = chainLength-1; startIndex >= 0; startIndex--) {
            var currentHash = hash;
            for (int i = startIndex; i < chainLength; i++) {
                var pw = reduce(currentHash.add(BigInteger.valueOf(i)));
                var rainbowTableIndex = lookupInRainbowTable(pw);
                if (rainbowTableIndex != -1) {
                    System.out.println("> Reduction index " + startIndex + " matched");
                    return rainbowTableIndex;
                }
                currentHash = md5(pw);
            }
        }
        throw new IllegalArgumentException("No entry in the rainbow table was found");
    }

    private String getPwForMatchingHash(String startPw, BigInteger matchingHash) {
        var pw = startPw;
        System.out.println(pw);
        System.out.println("> Checking chain with chain start " + startPw);
        for(int i = 0; i < chainLength; i++) {
            var hash = md5(pw);
            if(hash.equals(matchingHash)) {
                System.out.println("> Found pw " + pw + " with matching hash " + hash);
                return pw;
            }
            pw = reduce(hash.add(BigInteger.valueOf(i)));
        }
        throw new IllegalArgumentException("No entry for given table entry " + startPw + " was found");
    }


    // Generated with ChatGPT
    private static BigInteger md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            return new BigInteger(1, digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    private static char getCharacterFor(int index) {
        // assuming Z = {0-9a-z}
        if(index >= 0 && index <= 9) {
            // ascii 0 to 9
            return (char) (48 + index);
        } else if (index >= 10 && index <= 35) {
            // ascii a to z
            return (char) (97 + (index-10));
        }
        throw new IllegalArgumentException("Index " + index + " is invalid");
    }

    // Generated with ChatGPT
    private String getPwForIndex(int i) {
        int base = 36;
        int length = 7;
        char[] result = new char[length];

        for (int pos = length - 1; pos >= 0; pos--) {
            int remainder = i % base;
            result[pos] = getCharacterFor(remainder);
            i /= base;
        }

        if (i > 0) {
            throw new IllegalArgumentException("Index too large to fit in 7 characters with base 36");
        }

        return new String(result);
    }

    private int lookupInRainbowTable(String pw) {
        for(int i = 0; i < table.length; i++) {
            if(table[i][1].equals(pw)){
                return i;
            }
        }
        return -1;
    }

    private void generateInitialTable() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("rainbow_table_log.txt"))) {
            for (int pwCount = 0; pwCount < pwAmount; pwCount++) {
                var pw = getPwForIndex(pwCount);
                table[pwCount][0] = pw;
                writer.newLine();
                writer.newLine();
                writer.write("PW:"+pw);
                writer.newLine();
                for (int i = 0; i < chainLength; i++) {
                    var hash = md5(pw);
                    writer.write(pw+"@"+hash.toString(16));
                    writer.write(" ");

                    pw = reduce(hash.add(BigInteger.valueOf(i)));
                }
                writer.newLine();
                // ONLY storing chain end and not the different
                table[pwCount][1] = pw;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String reduce(BigInteger hash) {
        var output = new StringBuilder();
        var currentHash = hash;
        for(int i = 0; i < pwLength; i++){
            var divideAndRemainder = currentHash.divideAndRemainder(BigInteger.valueOf(pwCharactersAmount));
            var div = divideAndRemainder[0];
            var mod = divideAndRemainder[1];
            output.append(getCharacterFor(mod.intValue()));
            currentHash = div;
        }
        return output.reverse().toString();
    }
}
