import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;


public class TikaPDFExtraction {
	 public static void main(final String[] args) throws IOException,TikaException, SAXException {

	      BodyContentHandler handler = new BodyContentHandler();
	      Metadata metadata = new Metadata();
	      FileInputStream inputstream = new FileInputStream(new File("01.html"));
	      ParseContext pcontext = new ParseContext();
	      

	      
	      //parsing the document using PDF parser
	      Parser pdfparser = new AutoDetectParser();
	      
	      pdfparser.parse(inputstream, handler, metadata,pcontext);
	      
	      //getting the content of the document
	      System.out.println("Contents of the file :" + handler.toString().trim());
	      

	  
	   }
}
