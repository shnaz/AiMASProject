package mas.agentsUnderdogs.util;

import java.io.*;
import sun.audio.*;

public class Helper {
	
	public static void playTadaSound() throws IOException{
		// open the sound file as a Java input stream
		String gongFile = "tada.wav";
		InputStream in = new FileInputStream(gongFile);

		// create an audiostream from the inputstream
		AudioStream audioStream = new AudioStream(in);

		// play the audio clip with the audioplayer class
		AudioPlayer.player.start(audioStream);
	}

}
