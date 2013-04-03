package com.matt.remotenotifier;

class JobHolder{

	/**
	 * 
	 */
	private final JobCoordinator jobCoordinator;

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getOuterType().hashCode();
		result = prime * result
				+ ((jobName == null) ? 0 : jobName.hashCode());
		result = prime * result
				+ (int) (jobCreateTime ^ (jobCreateTime >>> 32));
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobHolder other = (JobHolder) obj;
		if (!getOuterType().equals(other.getOuterType()))
			return false;
		if (jobName == null) {
			if (other.jobName != null)
				return false;
		} else if (!jobName.equals(other.jobName))
			return false;
		if (jobCreateTime != other.jobCreateTime)
			return false;
		return true;
	}

	private int jobId;
	private int jobProgress;
	private int deviceId;
	private boolean jobComplete;
	private boolean jobRecieved;
	private int jobStatus;
	private long jobCreateTime;
	private String jobRetval;
	private String jobName;
	private String runDateTime;
	private CommandHolder jobCommandHolder;
	
	public JobHolder(JobCoordinator jobCoordinator, String jobName, CommandHolder jobCommand, int deviceId){
		this.jobCoordinator = jobCoordinator;
		this.jobName = jobName;
		this.deviceId = deviceId;
		this.jobCommandHolder = jobCommand;
		jobCreateTime = System.currentTimeMillis();
		jobComplete = false;
		jobRecieved = false;
		jobProgress = 0;	
		jobStatus = 0;
	}

	private JobCoordinator getOuterType() {
		return this.jobCoordinator;
	}

	/**
	 * @return the jobProgress
	 */
	public int getJobProgress() {
		return jobProgress;
	}

	/**
	 * @return the jobComplete
	 */
	public boolean isJobComplete() {
		return jobComplete;
	}

	/**
	 * @param jobProgress the jobProgress to set
	 */
	public void setJobProgress(int jobProgress) {
		this.jobProgress = jobProgress;
	}

	/**
	 * @return the jobId
	 */
	public int getJobId() {
		return jobId;
	}

	/**
	 * @param jobId the jobId to set
	 * IMPORTANT: This must be called directly after the constructor
	 */
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public int getDeviceId(){
		return deviceId; 
	}
	
	public boolean isJobRecieved(){
		return jobRecieved;
	}
	
	/**
	 * @param set jobComplete to true
	 */
	public void setJobComplete() {
		jobComplete = true;
	}

	/**
	 * @param set jobRecieved to true
	 */
	public void setJobRecieved() {
		jobRecieved = true;
	}

	public CommandHolder getCommandHolder(){
		return jobCommandHolder;
	}

	public String getJobName() {
		return jobName;
	}

	/**
	 * @return the jobCreateTime
	 */
	public long getJobCreateTime() {
		return jobCreateTime;
	}

	/**
	 * @return the jobStatus
	 * 1: Job Received (Not Complete)
	 * 2: Job Complete
	 * 3: Job error
	 * 0: Unknown Job Status
	 */
	public int getJobStatus() {
		return jobStatus;
	}

	/**
	 * @param jobStatus the jobStatus to set
	 * 0: Job not yet started
	 * 1: Job currently running on device
	 * 2: Job completed okay
	 * 3: Job in error
	 */
	public void setJobStatus(int jobStatus) {
		this.jobStatus = jobStatus;
	}

	/**
	 * @return the jobRetval
	 */
	public String getJobRetval() {
		return jobRetval;
	}

	/**
	 * @param jobRetval the jobRetval to set
	 */
	public void setJobRetval(String jobRetval) {
		this.jobRetval = jobRetval;
	}

	/**
	 * @return the runDateTime
	 */
	public String getRunDateTime() {
		return runDateTime;
	}

	/**
	 * @param runDateTime the runDateTime to set
	 */
	public void setRunDateTime(String runDateTime) {
		this.runDateTime = runDateTime;
	}
}