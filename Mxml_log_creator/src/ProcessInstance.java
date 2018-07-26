import java.util.ArrayList;

public class ProcessInstance implements Comparable<ProcessInstance>{
	
	int processInstaceId;
	public ArrayList<AuditTrialEntry> auditTrialEntries;
	
	public ProcessInstance (int instanceId){
		this.processInstaceId = instanceId;
		this.auditTrialEntries = new ArrayList<AuditTrialEntry>();
	}
	
	public int getInstanceId() {
		return processInstaceId;
	}
	
	public int compareTo(ProcessInstance i)
	{
	     return(processInstaceId - i.processInstaceId);
	}

}
