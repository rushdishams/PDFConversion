import java.io.*;
import java.time.Duration;
import java.time.Instant;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.exceptions.CryptographyException;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.util.*;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

/**
 * Class to utilize the PDFBox api to convert PDFs and HTMLs to texts
 * CHANGE:
 * - Tika added for html parsing
 * - Both pdf and htmls can be converted
 * - Can avoid files that are neither html nor pdfs
 * @author Rushdi Shams, Sustainalytics
 * @version 3.0 May 25 2015
 *
 */

public class PDFTest {
	//---------------------------------------------------------
	// Instance Variables
	//---------------------------------------------------------
	private PDDocument pd;
	private BufferedWriter wr;
	private File directory;

	//---------------------------------------------------------
	// Constructor
	//---------------------------------------------------------	
	/**
	 * Constructor of the class. Takes the directory path.
	 * @param directory is a String to denote the directory path of the PDF files
	 */
	public PDFTest(String directory){
		this.directory = new File(directory);
	}//constructor ends

	//---------------------------------------------------------
	// Methods
	//---------------------------------------------------------
	public void convertHTML(File file) throws IOException, SAXException, TikaException{

		BodyContentHandler handler = new BodyContentHandler(-1);
		Metadata metadata = new Metadata();
		FileInputStream inputstream = new FileInputStream(file);
		ParseContext pcontext = new ParseContext();

		//parsing the document using PDF parser
		Parser parser = new AutoDetectParser();

		parser.parse(inputstream, handler, metadata,pcontext);

		//getting the content of the document
		File convertedHTML = new File(FilenameUtils.removeExtension(file.getPath()) + ".txt");
		/*What hapens if the output file already exists? --->*/
		if(convertedHTML.exists()){
			FileUtils.forceDelete(convertedHTML);
			convertedHTML = new File(FilenameUtils.removeExtension(file.getPath()) + ".txt");
		}// <--- handling file duplication ends
		try {
			FileUtils.write(convertedHTML, handler.toString().trim(), null);
		} catch (IOException e) {
			System.out.println("Error writing Duration File");
		}
	}
	/**
	 * Method to convert the PDFs to text files. This method also calls
	 * a conversion method if it encounters a file which is html.
	 * @param listOfFiles is an array of Files/Folders found in the root folder
	 * @throws TikaException 
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public void convertPDF(File[] listOfFiles) throws IOException, SAXException, TikaException{

		if(listOfFiles.length > 0){ //check empty directory
			/*Read file names one by one -->*/
			for (File file: listOfFiles){
				/*If a directory is found, recursively move forward to that directory --->*/
				if(file.isDirectory()){
					convertPDF(file.listFiles());
				}// <---recursively reading of directory ends because we found files!

				else{//we found a file, not a directory --->
					String fileExtension = FilenameUtils.getExtension(file.getPath()); //only extension

					/*If the file has extension html --->*/
					if(fileExtension.equalsIgnoreCase("html")){
						convertHTML(file);
						continue; //we are going back to the beginning of for loop because the rest of the code deals with pdfs
					} // <--- html handling ends
					
					if(fileExtension.equalsIgnoreCase("txt")){
						continue;
					}

					File output = new File(FilenameUtils.removeExtension(file.getPath()) + ".txt"); //converted pdf
					/*What hapens if the output file already exists? --->*/
					if(output.exists()){
						FileUtils.forceDelete(output);
						output = new File(FilenameUtils.removeExtension(file.getPath()) + ".txt"); //output file
					}// <--- handling file duplication ends

					//Load the PDF --->
					try {
						pd = PDDocument.load(file);
					} catch (IOException e) {
						System.out.println("Error loading file " + file.getAbsolutePath());
					} //<--- pdf is loaded

					//work around to decrypt an encrypted pdf -->
					if (pd.isEncrypted()){
						System.out.println(" is encrypted. Trying to decrypt");
						try {
							pd.decrypt("");
						} catch (CryptographyException e) {
							System.out.println(file.getAbsolutePath());
							e.printStackTrace();
						} catch (IOException e) {
							System.out.println(file.getAbsolutePath());
							e.printStackTrace();
						}
						pd.setAllSecurityToBeRemoved(true);
					}//<--work around to decrypt an encrypted pdf ends here

					/*Paragraph mark-up --->*/
					PDFTextStripper stripper = null;
					try {
						stripper = new PDFTextStripper();
						stripper.setParagraphStart("<paragraph>" + "\n"); //detecting a paragraph start
					} catch (IOException e) {
						e.printStackTrace();
					}

					//preparing write buffer and writing the converted pdfs --->
					try {
						wr = new BufferedWriter(new FileWriter(output));
					} catch (IOException e) {
						e.printStackTrace();
					}

					try {
						stripper.writeText(pd, wr); 
					} catch (IOException e) {
						e.printStackTrace();
					}
					// <--- file write ends
					/*Let's close the buffers --->*/
					if (pd != null) {
						try {
							pd.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

					try {
						wr.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					//<--- buffer closes
				}//<--- we have dealt with files up to this point
			}//<--Let's move towards the next file/directory
			
		}//<--- the directory was not empty
		
	}//end method of converting pdf to text

	/**
	 * Getter file to get the directory path
	 * @return File object
	 */
	public File getDirectory(){
		return directory;
	}

	/**
	 * Driver method for the class
	 * @param args
	 * @throws TikaException 
	 * @throws SAXException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, SAXException, TikaException{

		Instant start = Instant.now();

		String rootFolder = "C:/Users/rushdi.shams/eclipse/workspace/Scrawl/convert-text";
		/*Create object*/
		PDFTest fileConversion = new PDFTest(rootFolder);
		/*Collect the list of files/foders in the root folder and send them to conversion method*/
		File[] listOfFiles = fileConversion.getDirectory().listFiles();
		fileConversion.convertPDF(listOfFiles);

		Instant end = Instant.now();
		System.out.println("Completion time: " + Duration.between(start, end));

	}//end main

}//end class