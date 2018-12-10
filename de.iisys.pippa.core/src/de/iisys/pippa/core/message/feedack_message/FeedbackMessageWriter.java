package de.iisys.pippa.core.message.feedack_message;

public interface FeedbackMessageWriter {

	void setIsRejected(boolean isRejected);

	void setIsRunning(boolean isRunning);

	void setIsWaiting(boolean isWaiting);

	void setIsScheduled(boolean isScheduled);

	void setStartedDialog(boolean startedDialog);

	void setEndedDialog(boolean endedDialog);

	void setIsUnscheduled(boolean isUnscheduled);

	void setIsStopped(boolean isStopped);
}
