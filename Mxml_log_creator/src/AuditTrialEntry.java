
public class AuditTrialEntry {
	
	public enum eventType{
		start, complete
	}

	public String workflowModelElement;
	
	public String timestamp;
	public String originator;
	private eventType eventType;
	
	public AuditTrialEntry (String workflowModelElement1, String eventTypeString1, String timeStamp1, String originatorName1){
		this.workflowModelElement = workflowModelElement1;
		this.timestamp = timeStamp1;
		this.originator = originatorName1;
		if(eventTypeString1.equals("entered")){
			this.eventType = eventType.start;
		}else if(eventTypeString1.equals("completed")){
			this.eventType = eventType.complete;
		}
	}
	
	public String getWorkFlModlElement(){
		return workflowModelElement;
	}
	
	public String getTimestamp() {
		return timestamp;
	};
	
	public String getOriginator(){
		return originator;
	}
	
	public String getEventType(){
		return eventType.toString();
	}
}
