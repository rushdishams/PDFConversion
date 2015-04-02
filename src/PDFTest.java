import java.io.*;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.*;

public class PDFTest {

	public static void main(String[] args){
		PDDocument pd;
		BufferedWriter wr;
		try {
			File input = new File("C:/test_pdf/Mitsubishi Materials Corp/csr2014-supplement-en.pdf");  // The PDF file from where you would like to extract
			File output = new File("out/csr2014-supplement-en.txt"); // The text file where you are going to store the extracted data
			pd = PDDocument.load(input);
			System.out.println(pd.getNumberOfPages());
			System.out.println(pd.isEncrypted());
			if (pd.isEncrypted()){
				pd.decrypt("");
				//pd.setAllSecurityToBeRemoved(true);
			}
			//pd.save("CopyOfInvoice.pdf"); // Creates a copy called "CopyOfInvoice.pdf"
			PDFTextStripper stripper = new PDFTextStripper();
			//stripper.setStartPage(3); //Start extracting from page 3
			//stripper.setEndPage(5); //Extract till page 5
			wr = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));
			stripper.writeText(pd, wr);
			if (pd != null) {
				pd.close();
			}
			// I use close() to flush the stream.
			wr.close();
		} catch (Exception e){
			e.printStackTrace();
		} 
	}
}