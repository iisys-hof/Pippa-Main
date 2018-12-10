package de.iisys.pippa.core.speech_out.TTS;

import java.io.File;

public interface TTS {

	public File convert(String text, boolean Markup);
	
}
