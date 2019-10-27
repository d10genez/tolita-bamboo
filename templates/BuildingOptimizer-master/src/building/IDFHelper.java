package building;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.nio.file.*;
import java.nio.charset.Charset;
import java.nio.ByteBuffer;

import java.util.List;

public class IDFHelper
{
	
	//Will be moved to Materials Class or to a db.
	//public static final String[] glazing_materials = {CLEAR 2.5MM,CLEAR 3MM,CLEAR 6MM,CLEAR 12MM,BRONZE 3MM,BRONZE 6MM,BRONZE 10MM,GREY 3MM,GREY 6MM,GREY 12MM,GREEN 3MM,GREEN 6MM,LOW IRON 2.5MM,LOW IRON 3MM,LOW IRON 4MM,LOW IRON 5MM,BLUE 6MM,REF A CLEAR LO 6MM,REF A CLEAR MID 6MM,REF A CLEAR HI 6MM,REF A TINT LO 6MM,REF A TINT MID 6MM,REF A TINT HI 6MM,REF B CLEAR LO 6MM,REF B CLEAR HI 6MM,REF B TINT LO 6MM,REF B TINT MID 6MM,REF B TINT HI 6MM,REF C CLEAR LO 6MM,REF C CLEAR MID 6MM,REF C CLEAR HI 6MM,REF C TINT LO 6MM,REF C TINT MID 6MM,REF C TINT HI 6MM,REF D CLEAR 6MM,REF D TINT 6MM,PYR A CLEAR 3MM,PYR B CLEAR 3MM,PYR B CLEAR 6MM,LoE CLEAR 3MM,LoE CLEAR 3MM Rev,LoE CLEAR 6MM,LoE CLEAR 6MM Rev,LoE TINT 6MM,LoE SPEC SEL CLEAR 3MM,  LoE SPEC SEL CLEAR 6MM,  LoE SPEC SEL CLEAR 6MM Rev,  LoE SPEC SEL TINT 6MM,   COATED POLY-88,COATED POLY-77,COATED POLY-66,COATED POLY-55,COATED POLY-44,COATED POLY-33,ECABS-1 BLEACHED 6MM,ECABS-1 COLORED 6MM,ECREF-1 BLEACHED 6MM,ECREF-1 COLORED 6MM,ECABS-2 BLEACHED 6MM,ECABS-2 COLORED 6MM,ECREF-2 BLEACHED 6MM,ECREF-2 COLORED 6MM};

	public static final String HEATING_SET_POINT_NAME = "Htg-SetP-Sch";

	public static final String COOLING_SET_POINT_NAME = "Clg-SetP-Sch";

	
	public static void main(String argv[])
	{
		// double[] genome = {4.0,1.0,1.0,18,26};
		// //modifyIDF(genome);
		// //parseBuildingCSV();
		// IDFHelper i = new IDFHelper();
		// i.modifyIDF(genome);
	}
	
	/**
	* Read through the IDF file and make necessary changes 
	* @param genome an array representing the genome of the building
	* @return the modified IDF File
	* @TODO pass in file as a param 
	*/
	public File modifyIDF(double[] genome)
	{
		
		String oldFileName = "building_base.idf";
		String tmpFileName = "temporary.idf";

		BufferedReader br = null;
		BufferedWriter bw = null;
		try
		{
			try 
			{
				br = new BufferedReader(new FileReader(oldFileName));
				bw = new BufferedWriter(new FileWriter(tmpFileName));
				String line;

				while ((line = br.readLine()) != null) 
				{
					//If we reach HVAC templates, break out of the loop. They will be appended later
					if(line.contains("!- Begin HVAC Zones and System")) {
						bw.write(line + "\n");
						break;
					}

					//See if it is a Construction E+ object
					if (line.contains("Construction,"))
					{	

						//Modify the class as needed and store it as a string.
						StringBuilder lineBuilder = new StringBuilder("Construction,");
						
						//Read and write the next line
						String nextLine = br.readLine();
						lineBuilder.append("\n" + nextLine + "\n");
						
						//if the line corresponds to a wall, change the insulation	
						if(nextLine.contains("!- NameWall"))
						{
							
							String modifiedWall = changeInsulation(br, (int) genome[0]);
						 	lineBuilder.append(modifiedWall);
						 }

						//If it corresponds to a window, change the glazing
						if(nextLine.contains("!- Name Window"))
						{	
							String modifiedWindow = changeGlazing(br, (int) genome[1]);
							lineBuilder.append(modifiedWindow);
						}

						//Finally, write the string to the temp file
						bw.write(lineBuilder.toString());
					}
					
					//Check if this is either a heating or a cooling point schedule
					else if (line.contains("Schedule:Compact,"))
					{	
						bw.write(line + "\n");
						String nextLine = br.readLine();
						if(nextLine.contains(HEATING_SET_POINT_NAME + ",")) 
						{
							removeObject(br);
							String schedule = generateSchedule(HEATING_SET_POINT_NAME, (int) genome[2]);
							bw.write(schedule);	
						}
						else if (nextLine.contains(COOLING_SET_POINT_NAME+","))
						{
							removeObject(br);
							String schedule = generateSchedule(COOLING_SET_POINT_NAME, (int) genome[3]);
							bw.write(schedule);
						}
						else
						{
							bw.write(nextLine+"\n");
						}
					}

					//Otherwise simply write the line back to the file
					else
					{
						bw.write(line+"\n");
					}
				}
			}
			finally {
				if(br != null)
					br.close();
				if(bw != null)
					bw.close();		
			}	
		}
		catch (IOException e) {
			e.printStackTrace();
		} 
		
      	// Once everything is complete, delete old file..
		File oldFile = new File(oldFileName);
		oldFile.delete();

      	// And rename tmp file's name to old file name
		File newFile = new File(tmpFileName);
		newFile.renameTo(oldFile); 

		// Append the string to the file
		appendHVACSystem((int) genome[2], oldFileName);
		
		return oldFile;
	}

	/**
	* @param br BufferedReader open with file
	* @param insulationMaterial index of insulation material
	* @return a string containing the changed insulation material
	*/
	public String changeInsulation(BufferedReader br, int insulationMaterial) throws IOException
	{
		String currentLine = null;	
		String layer3 = "\t" + Materials.wall_materials[insulationMaterial] 
		+ ",\t\t\t!- Layer 3";

		StringBuilder lineBuilder = new StringBuilder();
		
		//Append layer 1
		lineBuilder.append(br.readLine());
		lineBuilder.append("\n");
		
		//Append layer 2
		lineBuilder.append(br.readLine());
		lineBuilder.append("\n");
		
		//Change and append layer 3
		currentLine = br.readLine();
		currentLine = currentLine.replace(currentLine, layer3);
		lineBuilder.append(currentLine);
		lineBuilder.append("\n");
		
		//Append layer 4
		lineBuilder.append(br.readLine());
		lineBuilder.append("\n");

		return lineBuilder.toString();
	}

	/**
	* @param br BufferedReader open with file
	* @param insulationMaterial index of the glazing material
	* @return a string containing the changed glazing material
	*/
	public String changeGlazing(BufferedReader br, int glazingMaterial) throws IOException
	{
		StringBuilder windowBuilder = new StringBuilder();
		String layer = br.readLine(); //Discard the previous first layer
		layer = Materials.glazing_materials[glazingMaterial]; //Add First layer
		windowBuilder.append("\t");
		windowBuilder.append(layer);
		windowBuilder.append(",\t\t\t!- Layer1");
		windowBuilder.append("\n");
		windowBuilder.append(br.readLine()); //Add second layer
		windowBuilder.append("\n");
		windowBuilder.append("\t");
		windowBuilder.append(layer); //Add third layer
		windowBuilder.append(";\t\t\t!- Layer3");
		windowBuilder.append("\n");
		layer = br.readLine(); //Discard the previous third layer

		return windowBuilder.toString();
	}

	/**
	* @param path path to the file to be read
	* @param encoding Charset encoding usually set to default.
	* @return the file in a String
	*/
	public String readFile(String path, Charset encoding) throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}
	
	/**
	* @param hvacSystemType the index of the HVACSystemType
	* @param fileName the file where the HVACTemplate will be appended
	*/
	public void appendHVACSystem(int hvacSystemType, String fileName)
	{
		try
		{
			String hvacType = "idf_components/hvacSystems/hvac" + (int) hvacSystemType;
    		FileWriter fw = new FileWriter(fileName,true); //the true will append the new data
    		fw.write(readFile(hvacType,Charset.defaultCharset()));//appends the string to the file
    		fw.close();
    	}
    	catch(IOException ioe)
    	{
    		System.err.println("IOException: " + ioe.getMessage());
    	}
    }

    /**
	* Generate an E+ IDF temperature schedule. 
	* Used for heating and cooling setpoints
	* @TODO make this method have different temps for times of day.
	* @param name of the scedule object
	* @param temp temperature for the schedule
	* @return the IDF schedule object as a string
	*/
	public String generateSchedule(String name, int temp)
	{
		StringBuilder lineBuilder = new StringBuilder("\t"+name+",\t\t\t!-Name\n");
		lineBuilder.append("\tTemperature,\t\t\t!- Schedule Type Limits Name\n");
		lineBuilder.append("\tThrough: 12/31,\t\t\t!- Field 1\n");
		lineBuilder.append("\tFor: AllDays,\t\t\t!- Field 2\n");
		lineBuilder.append("\tUntil: 24:00,"+ temp +";\t\t!- Field 7\n");	
		return lineBuilder.toString();
	}

	/**
	* Simple Utility method to remove an IDF class.
	* Assume, BufferedReader object is on first line of the object.
	* Then, this will move the reader until the next semi-colon is encountered.
	* @param br Buffered Reader Object that has the file open
	*/
	public void removeObject(BufferedReader br) throws IOException
	{
		String line;
		do
		{
			line = br.readLine();
		}while(!line.contains(";"));
	}
}