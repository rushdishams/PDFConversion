import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;


public class CreationDateCheck {
	private final static int thresholdYear = 2011;
	private static String validFiles = "";

	public static void main(String[] args){
		File directory = new File("C:/Users/rushdi.shams/eclipse/workspace/Scrawl/convert-text/Nestle");
		File[] files = directory.listFiles();
		for (File file: files){

			PDDocument document = null;
			boolean isDamaged = false;
			String filePath = file.toString();
			System.out.println("--- filePath ---");
			try {
				document = PDDocument.load(filePath);
				if(!isDamaged){
					if (document.isEncrypted()){
						System.out.println("File is encrypted. Trying to decrypt...");
						try {
							document.decrypt("");
							document.setAllSecurityToBeRemoved(true);
						} catch (CryptographyException e) {
							System.out.println("Error decrypting file: ");
							isDamaged = true;
						}

					}//<--work around to decrypt an encrypted pdf ends here
					PDDocumentInformation info = document.getDocumentInformation();
					Calendar calendar = info.getCreationDate();
					if(calendar != null){
						int creationYear = calendar.get(Calendar.YEAR);
						System.out.println( file.getName() + " --> " + creationYear);
						if(creationYear >= thresholdYear){
							validFiles += filePath + "\n";
						}
					}
				}
			} catch (IOException e) {
				System.out.println("Error processing: " + file.toString());
				isDamaged = true;
			}
			finally{
				try {
					if(!isDamaged){
						document.close();
					}
				} catch (IOException e) {
					System.out.println("Error closing document: " + file.toString());				}
			}
			
		}
		try {
			FileUtils.write(new File(directory.toString() + "/validfiles.txt") , validFiles);
		} catch (IOException e) {
			System.out.println("Error writing valid files");
		}
	}

}
