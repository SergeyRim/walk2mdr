import javax.xml.bind.DatatypeConverter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

public class Convert {

    public static void main(String[] args) {

        System.out.println("");
        System.out.println("SNMP Walk to MDR file converter (v1.3)");
        System.out.println("(c) rimse01@ca.com");
        System.out.println("");

        if (args.length<1) {
            System.out.println("Usage: java -jar walk2mdr.jar <walk_file_location>");
            return;
        }

        Path path = Paths.get(args[0]);
        Path outputPath = Paths.get(args[0]+"_converted.mdr");
//        Path path = Paths.get("d:\\1\\device_mib.walk");
//        Path outputPath = Paths.get("d:\\1\\mibdump.txt_converted.mdr");

        Integer strNum = 0;

        try (Stream<String> lines = Files.lines(path)) {
            System.out.println("Converting file "+path);
            try (BufferedWriter outFile = Files.newBufferedWriter(outputPath)) {
                Iterator<String> iterator = lines.iterator();
                while (iterator.hasNext()) {
                    String curStr = iterator.next();
                    String newStr="";
                    strNum++;

                    //Check if string starts with digit (i.e. OID string)
                    if (!curStr.matches("^[1-9].*")) {
                        outFile.write(curStr+"\n");
                        continue;
                    }

                    //Remove 2 commas from string
                    newStr=curStr.replaceFirst(",","  ");
                    newStr=newStr.replaceFirst(",","  ");

                    //Change "ObjectID" to "OID"
                    newStr=newStr.replace("ObjectID","OID");
                    newStr="."+newStr;

                    //Add a dot at the beggining of OID type value
                    if (newStr.contains("OID")) {
                        String [] tmpStr = newStr.split("OID");
                        newStr=tmpStr[0].concat("  ").concat("OID").concat("  .").concat(tmpStr[1].trim());
                    }

                    //Needs to replace string with bytes for OctetString
                    if (newStr.contains("OctetString")) {

                        //Skip string if it has empty OctetString value
                        // OR add space to avoid array out of index exception
                        if (newStr.trim().split("OctetString").length == 1) {
                            //continue;
                            newStr=newStr.concat(" ");
                        }

                        String tmpStr = newStr.split("OctetString")[1].trim();
                        newStr = newStr.split("OctetString")[0].concat(" OctetString ");

                        byte[] byteArray;
                        //Check if OctetString value is a HEX string (starting with 0x)
                        if (tmpStr.startsWith("0x")) {
                            try {
                                byteArray = DatatypeConverter.parseHexBinary(tmpStr.substring(2, tmpStr.length()));
                            } catch (IllegalArgumentException e) {
                                System.out.println("Skipping line " + strNum + ": " + e);
                                continue;
                            }

                        } else {
                            byteArray = tmpStr.getBytes();
                        }

                        //Converting bytes array to output string
                        for (byte b : byteArray) {
                            int i = b & 0xff;
                            newStr = newStr + i + "-";
                        }

                        //Remove "-" symbol from end of line
                        newStr = newStr.substring(0, newStr.length() - 1);
                    }

                    //System.out.println(newStr);
                    outFile.write(newStr+"\n");
                }

            } catch (IOException ex) {
                System.out.println("Error write to file "+outputPath);
                return;
            }

        } catch (IOException ex) {
            System.out.println("Error reading from file "+path);
            return;
        }
        System.out.println("Converting succeed. File: "+outputPath);

    }


}
