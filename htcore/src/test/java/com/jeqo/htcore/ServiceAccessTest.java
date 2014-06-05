package com.jeqo.htcore;

import com.oracle.xmlns.bpel.workflow.taskqueryservice.TaskQueryService;
import com.oracle.xmlns.bpel.workflow.taskqueryservice.TaskQueryService_Service;
import com.oracle.xmlns.bpel.workflow.taskqueryservice.WorkflowErrorMessage;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.bpel.services.workflow.common.model.CredentialType;
import oracle.bpel.services.workflow.common.model.WorkflowContextType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jorge Quilcate
 */
public class ServiceAccessTest {
    
    public ServiceAccessTest() {
    }

    @Test
    public void testAccess() {
        try {
            TaskQueryService_Service taskQueryServiceClient = new TaskQueryService_Service();
            
            TaskQueryService taskQueryService = taskQueryServiceClient.getTaskQueryServicePort();
            
            CredentialType credentialType = new CredentialType();
            credentialType.setLogin("weblogic");
            credentialType.setPassword("welcome1");
            credentialType.setIdentityContext("jazn.com");
            
            System.out.println("Authenticating...");
            WorkflowContextType workflowContextType = taskQueryService.authenticate(credentialType);
            System.out.println("Authenticated to TaskQueryService");
            
            assertNotNull(workflowContextType);
            
        } catch (WorkflowErrorMessage ex) {
            Logger.getLogger(ServiceAccessTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
