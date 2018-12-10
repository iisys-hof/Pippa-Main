package de.iisys.pippa.core.resolver;

import java.util.concurrent.BlockingQueue;

import de.iisys.pippa.core.message.AMessage;

public interface Resolver {

	public BlockingQueue<AMessage> getDispatcherQueue();
	public BlockingQueue<AMessage> getSchedulerQueue();
	public BlockingQueue<AMessage> getExecutorQueue();
	public BlockingQueue<AMessage> getSkillRegistryQueue();
	
	public void setDispatcherQueue(BlockingQueue<AMessage> dispatcherQueue);
	public void setSchedulerQueue(BlockingQueue<AMessage> schedulerQueue);
	public void setExecutorQueue(BlockingQueue<AMessage> executorQueue);
	public void setSkillRegistryQueue(BlockingQueue<AMessage> skillRegistryQueue);
	
}
