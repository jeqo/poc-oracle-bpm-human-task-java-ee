package com.jeqo.htweb.view;

import com.oracle.xmlns.bpel.workflow.task.Humantask1PayloadType;
import com.oracle.xmlns.bpel.workflow.taskqueryservice.TaskQueryService;
import com.oracle.xmlns.bpel.workflow.taskqueryservice.TaskQueryService_Service;
import com.oracle.xmlns.bpel.workflow.taskqueryservice.WorkflowErrorMessage;
import com.oracle.xmlns.bpel.workflow.taskservice.StaleObjectFaultMessage;
import com.oracle.xmlns.bpel.workflow.taskservice.TaskService;
import com.oracle.xmlns.bpel.workflow.taskservice.TaskServiceContextTaskBaseType;
import com.oracle.xmlns.bpel.workflow.taskservice.TaskService_Service;
import com.oracle.xmlns.bpel.workflow.taskservice.UpdateTaskOutcomeType;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import oracle.bpel.services.workflow.common.model.WorkflowContextType;
import oracle.bpel.services.workflow.query.model.TaskDetailsByIdRequestType;
import oracle.bpel.services.workflow.query.model.WorkflowContextRequestType;
import oracle.bpel.services.workflow.task.model.Task;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Only approve is implemented
 * 
 * @author Jorge Quilcate
 */
@ManagedBean
@ViewScoped
public class ApprovalTaskBean {

    private Task task;
    private Humantask1PayloadType payload;
    private WorkflowContextType workflowContext;
    private JAXBElement<Humantask1PayloadType> payloadObject;

    @PostConstruct
    public void init() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

        String taskId = request.getParameter("bpmWorklistTaskId");
        String context = request.getParameter("bpmWorklistContext");

        System.out.println("Task ID: " + taskId);
        System.out.println("Context: " + context);

        TaskQueryService_Service taskQueryServiceClient = new TaskQueryService_Service();
        TaskQueryService taskQueryService = taskQueryServiceClient.getTaskQueryServicePort();

        try {
            WorkflowContextRequestType getWorkflowContextRequest = new WorkflowContextRequestType();
            getWorkflowContextRequest.setToken(context);

            workflowContext = taskQueryService.getWorkflowContext(getWorkflowContextRequest);
            TaskDetailsByIdRequestType getTaskDetailsByIdRequest = new TaskDetailsByIdRequestType();
            getTaskDetailsByIdRequest.setTaskId(taskId);
            getTaskDetailsByIdRequest.setWorkflowContext(workflowContext);

            task = taskQueryService.getTaskDetailsById(getTaskDetailsByIdRequest);

            Node payloadNode = (Node) task.getPayload();

            JAXBContext jaxbContext = JAXBContext.newInstance("com.oracle.xmlns.bpel.workflow.task");
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            payloadObject = (JAXBElement<Humantask1PayloadType>) unmarshaller.unmarshal(payloadNode, Humantask1PayloadType.class);
            payload = payloadObject.getValue();
        } catch (WorkflowErrorMessage | JAXBException ex) {
            Logger.getLogger(ApprovalTaskBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String approve() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document payloadDocument = db.newDocument();

            JAXBContext jaxbContext = JAXBContext.newInstance("com.oracle.xmlns.bpel.workflow.task");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.marshal(payloadObject, payloadDocument);

            task.setPayload(payloadDocument.getDocumentElement());

            TaskService_Service taskServiceClient = new TaskService_Service();
            TaskService taskService = taskServiceClient.getTaskServicePort();

            TaskServiceContextTaskBaseType updateTaskRequest = new TaskServiceContextTaskBaseType();
            updateTaskRequest.setTask(task);
            updateTaskRequest.setWorkflowContext(workflowContext);

            //Updating task details
            task = taskService.updateTask(updateTaskRequest);

            UpdateTaskOutcomeType updateTaskOutcomeRequest = new UpdateTaskOutcomeType();
            updateTaskOutcomeRequest.setOutcome("APPROVE");
            updateTaskOutcomeRequest.setTask(task);
            updateTaskOutcomeRequest.setWorkflowContext(workflowContext);

            //Updating task outcome
            taskService.updateTaskOutcome(updateTaskOutcomeRequest);
        } catch (StaleObjectFaultMessage | com.oracle.xmlns.bpel.workflow.taskservice.WorkflowErrorMessage | JAXBException | ParserConfigurationException ex) {
            Logger.getLogger(ApprovalTaskBean.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "completed";
    }

    public String reject() {
        
        return "completed";
    }

    public Humantask1PayloadType getPayload() {
        return payload;
    }

    public void setPayload(Humantask1PayloadType payload) {
        this.payload = payload;
    }
}
