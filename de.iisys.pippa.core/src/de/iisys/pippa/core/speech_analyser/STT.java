package de.iisys.pippa.core.speech_analyser;

import java.io.File;

public interface STT {

	public String recognize(File audioFile);

}
