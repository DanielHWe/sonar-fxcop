package org.sonar.plugins.fxcop;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
}
