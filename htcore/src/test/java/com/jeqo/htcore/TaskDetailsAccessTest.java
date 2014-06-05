package com.jeqo.htcore;

import com.oracle.xmlns.bpel.workflow.task.Humantask1PayloadType;
import com.oracle.xmlns.bpel.workflow.taskqueryservice.TaskQueryService;
import com.oracle.xmlns.bpel.workflow.taskqueryservice.TaskQueryService_Service;
import com.oracle.xmlns.bpel.workflow.taskqueryservice.WorkflowErrorMessage;
import java.math.BigInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import oracle.bpel.services.workflow.common.model.CredentialType;
import oracle.bpel.services.workflow.common.model.WorkflowContextType;
import oracle.bpel.services.workflow.query.model.TaskDetailsByNumberRequestType;
import oracle.bpel.services.workflow.task.model.Task;
import org.example.schema.processschema.ProcessType;
import org.junit.Test;
import static org.junit.Assert.*;
import org.w3c.dom.Node;

/**
 *
 * @author Jorge Quilcate
 */
public class TaskDetailsAccessTest {
    
    public TaskDetailsAccessTest() {
    }
    
    @Test
    public void accessTaskDetails() {
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
            
            TaskDetailsByNumberRequestType taskDetailsRequest = new TaskDetailsByNumberRequestType();
            //Enter a task number running on Oracle BPM
            taskDetailsRequest.setTaskNumber(new BigInteger("200023"));
            taskDetailsRequest.setWorkflowContext(workflowContextType);
            
            Task task = taskQueryService.getTaskDetailsByNumber(taskDetailsRequest);
            
            System.out.println("Task: " + task.getSystemAttributes().getTaskId());
            
            Node payload = (Node) task.getPayload();

            //JAXB Unmarshalling
            JAXBContext context = JAXBContext.newInstance("com.oracle.xmlns.bpel.workflow.task");
            Unmarshaller unmarshaller = context.createUnmarshaller();
            JAXBElement<Humantask1PayloadType> payloadObject = (JAXBElement<Humantask1PayloadType>) unmarshaller.unmarshal(payload, Humantask1PayloadType.class);
            
            ProcessType dataObject = payloadObject.getValue().getDataObject();
            
            System.out.println("Payload: " + dataObject.getName());
            
            assertNotNull(dataObject);
            
        } catch (WorkflowErrorMessage | JAXBException ex) {
            Logger.getLogger(TaskDetailsAccessTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
