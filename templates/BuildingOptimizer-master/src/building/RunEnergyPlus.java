package building;
import org.apache.commons.exec.*;
import java.io.File;
import java.lang.InterruptedException;
import java.io.IOException;

public class RunEnergyPlus
{
	public static int execute(File idf) throws java.io.IOException
	{
		try
		{
			CommandLine cmdLine = new CommandLine("runenergyplus");
			cmdLine.addArgument(idf.getName());

			// This should be a user input file//
			cmdLine.addArgument("USA_IL_Chicago-OHare.Intl.AP.725300_TMY3.epw");

			DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

			//ExecuteWatchdog watchdog = new ExecuteWatchdog(600*1000);
			Executor executor = new DefaultExecutor();
			executor.setExitValue(1);
			//executor.setWatchdog(watchdog);
			executor.execute(cmdLine, resultHandler);

			// some time later the result handler callback was invoked so we
			// can safely request the exit value
			resultHandler.waitFor();
			int exitValue = resultHandler.getExitValue();
			return exitValue;
		}
		catch(IOException e)
		{
			System.out.println("IO Exception Found");
			e.printStackTrace();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();	
		}

		//Something went wrong
		return -1;
	}
}