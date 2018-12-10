package de.iisys.pippa.core.executor;

import java.util.concurrent.BlockingQueue;

import de.iisys.pippa.core.message.AMessage;

public interface Executor {

	public BlockingQueue<AMessage> getSkillRegistryQueue();
	
	public void setSkillRegistryQueue(BlockingQueue<AMessage> skillRegistryQueue);
	
}
