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
 * - Bug fix for stopping execution for corrupted pdfs
 * @author Rushdi Shams, Sustainalytics
 * @version 3.1 May 25 2015
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

					InputStream fileStream = new FileInputStream(file);

					boolean error = false;
					//Load the PDF --->
					try {
						pd = PDDocument.load(fileStream, true);
						fileStream.close();
					} catch (IOException e) {
						System.out.println("Error loading PDF file: " + file.getAbsolutePath());
						error = true;
					} //<--- pdf is loaded

					//In case we could read the PDF file, the file is not corrupt --->
					if(!error){
						//work around to decrypt an encrypted pdf -->
						error = false;
						System.out.println(file.getAbsolutePath());
						if (pd.isEncrypted()){
							System.out.println(file.getAbsolutePath() + " is encrypted. Trying to decrypt...");
							try {
								pd.decrypt("");
								pd.setAllSecurityToBeRemoved(true);
							} catch (CryptographyException e) {
								error = true;
								System.out.println("Error decrypting file: " + file.getAbsolutePath());
							}

						}//<--work around to decrypt an encrypted pdf ends here

						if(!error){
							/*Paragraph mark-up --->*/
							PDFTextStripper stripper = null;
							try {
								stripper = new PDFTextStripper();
								stripper.setParagraphStart("<p>"); //detecting a paragraph start
								stripper.setParagraphEnd("</p>");
								stripper.setPageStart("<page>");
								stripper.setPageEnd("</page>");
								
							} catch (IOException e) {
								System.out.println("Error stripping file: " + file.getAbsolutePath());
							}

							//preparing write buffer and writing the converted pdfs --->
							try {
								wr = new BufferedWriter(new FileWriter(output));
							} catch (IOException e) {
								System.out.println("Error creating write buffer");
							}

							try {
								stripper.writeText(pd, wr); 
							} catch (IOException e) {
								System.out.println("Error writing PDF file with stripper");
							}
							// <--- file write ends
							/*Let's close the buffers --->*/
							try {
								pd.close();
							} catch (IOException e) {
								System.out.println("Error closing PDF Document object");
							}

							try {
								wr.close();
							} catch (IOException e) {
								System.out.println("Error closing writer");
							}
							//<--- buffer closes
						}// <--- the file was not corrupt or something
					}
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

		String rootFolder = "C:/Users/rushdi.shams/eclipse/workspace/Scrawl/convert-text/IQ30781/";
		/*Create object*/
		PDFTest fileConversion = new PDFTest(rootFolder);
		/*Collect the list of files/foders in the root folder and send them to conversion method*/
		File[] listOfFiles = fileConversion.getDirectory().listFiles();
		fileConversion.convertPDF(listOfFiles);

		Instant end = Instant.now();
		System.out.println("Completion time: " + Duration.between(start, end));

	}//end main

}//end class