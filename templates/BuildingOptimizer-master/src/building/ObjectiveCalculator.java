package building;

import au.com.bytecode.opencsv.*;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ObjectiveCalculator{

	private String buildingFileName;
	private int insulationMaterial;
	private int glazingMaterial;
	private int hvacSystem;
	private double electricity;
	private double naturalGas;

	// Electricity rate for January 2014 in $/KWh
	public static final double NATURAL_GAS_RATE = 0.0831;
	// Natural gas rate for January 2014 in $/KWh
	public static final double ELECTRICTY_RATE = 0.0809;

	//EnergyPlus returns energy in Joules. Convert to KWh
	public static final double JOULE_KWH_CONVERSION_FACTOR = 2.77777778E-7d;
	
	//double array should be int array. Fix in ECJ
	public ObjectiveCalculator(String buildingFileName, double[] genome)
	{
		this.buildingFileName = buildingFileName;
		this.insulationMaterial =(int) genome[0];
		this.glazingMaterial = (int) genome[1];
		this.hvacSystem = (int) genome[2];
		this.electricity = -1;
		this.naturalGas = -1;
	}

	/**
	* @return Total energy in Joules
	*/
	public double calculateEnergy()
	{
		if(electricity < 0 || naturalGas < 0 )
			parseBuildingCSV();
		return electricity + naturalGas;
	}


	public double calculateCost()
	{
		if(electricity < 0 || naturalGas < 0 )
			parseBuildingCSV();
		return calculateOperationalCost() + calculateInstallationCost();
	}


	public double calculateInstallationCost()
	{
		double insulationCost = BuildingProperties.NO_WALLS * Materials.wall_materials_cost[insulationMaterial];
		double glazingCost = BuildingProperties.NO_WINDOWS * Materials.glazing_materials_cost[glazingMaterial];
		double hvacSystemCost = Materials.hvac_systems_cost[hvacSystem];

		return insulationCost + glazingCost + hvacSystemCost;
	}

	/**
	* @return the sum of the total cost of opeation
	*/
	public double calculateOperationalCost()
	{
		double naturalGasCost = naturalGas * NATURAL_GAS_RATE;
		double electricityCost = electricity * ELECTRICTY_RATE;
		return naturalGasCost + electricityCost;
	}

	/**
	* Parse the building.csv file return the columns
	*/
	public void parseBuildingCSV()
	{
		String[] lastRow = null;
		try
		{
			//String file = (buildingFileName.split("."))[0];
			CSVReader reader = new CSVReader(new FileReader("Output/building_base.csv"), ',', '\"', 1);
			List csvRows = reader.readAll();
			lastRow = (String[]) csvRows.get(csvRows.size()-1);
			this.electricity = Float.parseFloat(lastRow[1]) * JOULE_KWH_CONVERSION_FACTOR;
			this.naturalGas = Float.parseFloat(lastRow[3])  * JOULE_KWH_CONVERSION_FACTOR;
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
