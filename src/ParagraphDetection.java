import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;


public class ParagraphDetection {
	
	public static void main(String[] args) throws IOException{
		File dataFile = new File("out/csr2014-supplement-en.txt");
		FileWriter arffFileWriter;
		BufferedWriter bw = null;
		try {
			arffFileWriter = new FileWriter("out/" + "csr2014-supplement-en-mod.txt");
			 bw = new BufferedWriter(arffFileWriter);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			String dataContent = FileUtils.readFileToString(dataFile);
			dataContent = dataContent.replaceAll(" \r\n", " ");
			dataContent = dataContent.replaceAll(" +", " ");
			dataContent = dataContent.replaceAll("-\r\n", "");
			//String[] data = dataContent.split("\r\n");
			//String finalText = "";
			/*for (int i = 0; i < data.length; i ++){
				data[i] = data[i].trim();
				if (!data[i].endsWith(".")){
					finalText += data[i] + " ";
				}
				else{
					finalText += data[i] + "\n";
				}
			}*/
			bw.write(dataContent);
			//bw.write(finalText);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bw.close();
	}

}
