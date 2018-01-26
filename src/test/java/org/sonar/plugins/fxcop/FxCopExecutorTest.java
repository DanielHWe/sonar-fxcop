package org.sonar.plugins.fxcop;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.api.config.Settings;
import org.sonar.api.utils.command.CommandException;

public class FxCopExecutorTest {

	@Rule
	  public ExpectedException thrown = ExpectedException.none();
	
	@Test
	  public void testSetExecutable() throws IOException {		
		FxCopExecutor executor = new FxCopExecutor();
				
		executor.setExecutable("FxCopCmd.exe");
	    assertThat(executor.getExecutable()).isEqualTo("FxCopCmd.exe");		  
	  }
	
	@Test
	  public void testSetAspNet() throws IOException {		
		FxCopExecutor executor = new FxCopExecutor();
				
		executor.setAspnet(true);
	    assertThat(executor.isAspnet()).isEqualTo(true);		  
	  }
	
	@Test
	  public void testSetAspNetTimeout() throws IOException {		
		FxCopExecutor executor = new FxCopExecutor();
				
		executor.setTimeout(31);
	    assertThat(executor.getTimeout()).isEqualTo(31);		  
	  }
	
	@Test
	  public void check_fxcopcmd_missing() {
		    thrown.expect(CommandException.class);
		    thrown.expectMessage("Cannot run program");    

		    FxCopExecutor executor = new FxCopExecutor();
		    executor.setExecutable("FxCopCmd.exe");
		    executor.setAspnet(true);
		    executor.setTimeout(31);
		    List<String> emptyList = new ArrayList<>();
		    emptyList.add("/");
		    
		    executor.execute("myproj.fxcop", new File("rules"), new File("outreport.report"), emptyList, emptyList);
		  }
}
