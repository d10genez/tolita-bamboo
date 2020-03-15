package building;

import ec.util.*;
import ec.*;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.*;
import ec.vector.*;

import java.io.File;
import java.io.IOException;

public class Building extends Problem implements SimpleProblemForm {

	public static final int NUM_DECISION_VARIABLES = 5;
	public void evaluate(final EvolutionState state, final Individual ind, final int subpopulation, final int threadnum)
	{
		
	//Ensure that the individual is of right type
	if( !( ind instanceof DoubleVectorIndividual ) )
	  state.output.fatal( "The individuals for this problem should be DoubleVectorIndividuals." );

	float[] objectives = ((MultiObjectiveFitness)ind.fitness).getObjectives();
	// Copy of ind
	DoubleVectorIndividual temp = (DoubleVectorIndividual)ind;
	double[] genome = temp.genome;

	// Ensure the number of decision variables is 5
	int numDecisionVars = genome.length; 
	if(numDecisionVars != NUM_DECISION_VARIABLES) throw new RuntimeException("Building needs exactly 5 decision variables (genes).");

	
	/** 
	* Create a modified IDF with the genomes and run E+ on the modified file.
	* @TODO Have the execute return the output file.
	*/
	IDFHelper idfHelper = new IDFHelper(); 
	File idf = null;
	try
	{
		idf = idfHelper.modifyIDF(genome);
		int exitValue = RunEnergyPlus.execute(idf);
	}
	catch(IOException e)
	{
	  	e.printStackTrace();
	}

	// Get cost and energy usage values from the building. 
  	ObjectiveCalculator objectiveCalculator = new ObjectiveCalculator(idf.getName(), genome);
	//Set the objectives 
	objectives[0] = (float) objectiveCalculator.calculateEnergy(); 
	objectives[1] = (float) objectiveCalculator.calculateCost();

	((MultiObjectiveFitness)ind.fitness).setObjectives(state, objectives);
	ind.evaluated = true;
  }
}