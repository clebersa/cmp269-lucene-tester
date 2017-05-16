
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.Charset;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;

public class DocumentSplitter {

    public static void main(String args[]) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage:\nDocumentSplitter source/folder destination/folder");
            System.exit(0);
        }

        String[] files = (new File(args[0])).list();
        File file;
        String fileContent, fullFileName;

        for (String fileName : files) {
            fullFileName = args[0] + "/" + fileName;
            System.out.println("[INFO] File " + fullFileName);
            
            file = new File(fullFileName);
            if (file.isDirectory()) {
                System.out.println("[WARN] Ignoring directory " + fullFileName);
                continue;
            }
            
            try {
                //System.out.println(Charset.availableCharsets().toString());
                fileContent = readFile(fullFileName, Charset.forName("ISO-8859-15"));
            }catch(IOException exception){
                System.out.println("[ERROR] Unable to read file " + fullFileName + ". Error: " + exception.getMessage());
                continue;
            }
            
            if (fileContent == null) {
                System.out.println("[ERROR] Null content for file " + fullFileName);
                continue;
            }
            splitDocuments(fileContent, args[1]);
        }
        System.out.println("[INFO] Sppliting finished.");
    }

    /**
     * Reads the content of a file and stores it in a string.
     * @param path Path to the file to be read.
     * @param encoding Encoding used to read the file.
     * @return The content of the file in a string.
     * @throws IOException if any I/O error occurs.
     */
    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    /**
     * Splits a SGML file in as many documents it has.
     * @param fileContent The content of the file.
     * @param newFolder The directory where the document will be stored.
     */
    private static void splitDocuments(String fileContent, String newFolder) {
        String documents[] = fileContent.split("(?<=</DOC>)");
        String documentNumber;
        for (String document : documents) {
            document = document.trim();
            if (document.isEmpty()) {
                continue;
            }
            try {
                documentNumber = ((document.split("<DOCNO>", 2))[1].split("</DOCNO>", 2))[0];
                System.out.println("[INFO]\t Document Number: " + documentNumber);
            
                Files.write(Paths.get(newFolder + "/" + documentNumber), 
                        document.getBytes(StandardCharsets.UTF_8), 
                        StandardOpenOption.CREATE);
            } catch (Exception exception) {
                System.out.println("[ERROR]\t Unable to write document " + fileContent + ". Error: " + exception.getMessage());
            }
        }
    }
}
