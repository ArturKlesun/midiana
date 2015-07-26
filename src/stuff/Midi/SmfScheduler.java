package stuff.Midi;

import blockspace.staff.StaffConfig.StaffConfig;
import blockspace.staff.accord.nota.Nota;
import jm.midi.SMF;
import jm.midi.Track;
import jm.midi.event.NoteOff;
import jm.midi.event.NoteOn;
import org.apache.commons.math3.fraction.Fraction;
import stuff.tools.jmusic_integration.INota;

import java.util.Map;

public class SmfScheduler implements IMidiScheduler
{
	final private Map<Integer, Track> trackDict;
	final private StaffConfig config;

	public SmfScheduler(Map<Integer, Track> trackDict, StaffConfig config) {
		this.trackDict = trackDict;
		this.config = config;
	}

	public void addNoteOnTask(Fraction when, INota nota) {
		if (!trackDict.containsKey(nota.getChannel())) {
			trackDict.put(nota.getChannel(), new Track());
		}
		trackDict.get(nota.getChannel()).addEvent(new NoteOn(nota, time(when)));
	}

	public void addNoteOffTask(Fraction when, INota nota) {
		if (!trackDict.containsKey(nota.getChannel())) {
			trackDict.put(nota.getChannel(), new Track());
		}
		trackDict.get(nota.getChannel()).addEvent(new NoteOff(nota, time(when)));
	}

	private int time(Fraction f) {
		return Nota.getTimeMilliseconds(f, config.getTempo());
	}
}
