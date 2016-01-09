package org.shmidusic.sheet_music;

import org.klesun_model.*;
import org.shmidusic.MainPanel;
import org.shmidusic.sheet_music.staff.Staff;
import org.shmidusic.sheet_music.staff.chord.Chord;
import org.shmidusic.sheet_music.staff.StaffComponent;
import org.shmidusic.stuff.OverridingDefaultClasses.TruMap;
import org.shmidusic.stuff.graphics.Settings;
import org.shmidusic.stuff.tools.FileProcessor;
import org.shmidusic.stuff.tools.Fp;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class SheetMusicComponent extends JPanel implements IComponent
{
	final public MainPanel mainPanel;
	final public SheetMusic sheetMusic;
	final AbstractHandler handler;

	private Set<StaffComponent> staffComponentSet = new HashSet<>();

	public SheetMusicComponent(SheetMusic sheetMusic, MainPanel mainPanel)
	{
		this.mainPanel = mainPanel;
		this.sheetMusic = sheetMusic;

		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		sheetMusic.staffList.forEach(staff -> {
			StaffComponent staffComp = new StaffComponent(staff, this);
			staffComponentSet.add(staffComp);
			this.add(staffComp);
		});
		this.revalidate();

		this.setFocusable(true);

		this.handler = new AbstractHandler(this) {
			public LinkedHashMap<Combo, ContextAction> getMyClassActionMap() {
				return new TruMap<>()
					.p(new Combo(ctrl, k.VK_L), mkFailableAction(SheetMusicComponent::splitFocusedStaff))
//					.p(new Combo(ctrl, k.VK_P), mkAction(SheetMusicComponent::triggerPlayback).setCaption("Play/Stop"))
					.p(new Combo(0, k.VK_SPACE), mkAction(SheetMusicComponent::triggerPlayback).setCaption("Play/Stop"))
						// File
					.p(new Combo(ctrl, k.VK_S), mkFailableAction(FileProcessor::saveMusicPanel).setCaption("Save mid.js"))
					.p(new Combo(ctrl, k.VK_M), mkFailableAction(FileProcessor::saveMidi).setCaption("Save midi"))
					.p(new Combo(ctrl, k.VK_O), mkFailableAction(FileProcessor::openSheetMusic).setCaption("Open"))
					.p(new Combo(ctrl, k.VK_I), mkFailableAction(FileProcessor::openMidi).setCaption("Open midi"))

					.p(new Combo(ctrl, k.VK_E), mkFailableAction(FileProcessor::savePNG).setCaption("Export png"))
						;
			}
		};
		this.addKeyListener(handler);
		addMouseListener(Fp.onClick(e -> requestFocus()));
	}

	public void triggerPlayback() {
		getFocusedChild().getPlayback().trigger();
	}

	/** creating two staffs from one: to pointer pos and from pointer pos */
	public Explain splitFocusedStaff()
	{
		return new Explain(false, "Implementation not ready yet");

//		Staff s = getFocusedChild().staff;
//		return new Explain(s.getFocusedIndex() > 0, "im not plitting here").runIfSuccess(() -> {
//			Staff newStaff = this.sheetMusic.addNewStaffAfter(s);
//
//			List<Chord> staff2Chords = s.getChordList().subList(s.getFocusedIndex(), s.getChordList().size());
//
//			staff2Chords.stream().forEach(c -> {
//				s.remove(c);
//				newStaff.addNewAccord().reconstructFromJson(c.getJsonRepresentation());
//			});
//			this.revalidate();
//
//			newStaff.getConfig().reconstructFromJson(newStaff.getJsonRepresentation());
//		});
	}

	private int getFocusedSystemY() {
		int dy = Settings.inst().getStepHeight();
		return Staff.SISDISPLACE * dy * (getFocusedChild().staff.getFocusedIndex() / getAccordInRowCount());
	}

	private int getAccordInRowCount() {
		int result = (getWidth() - StaffComponent.getLeftMargin() - StaffComponent.getRightMargin()) / (2 * dx());
		return Math.max(result, 1);
	}

	public void checkCam()
	{
		int dy = Settings.inst().getStepHeight();

		JScrollPane staffScroll = mainPanel.sheetScroll;
		JScrollBar vertical = staffScroll.getVerticalScrollBar();

		if (vertical.getValue() + staffScroll.getHeight() < getFocusedSystemY() + Staff.SISDISPLACE * dy ||
				vertical.getValue() > getFocusedSystemY()) {
			vertical.setValue(getFocusedSystemY());
			repaint();
		}
	}

	@Override
	public Dimension getPreferredSize()
	{
		int height = getStaffPanelStream().map(c -> c.calcTrueHeight()).reduce(Math::addExact).get();
		return new Dimension(mainPanel.getWidth() - 30, height); // - 30 - love awt and horizontal scrollbars
	}

	private Stream<StaffComponent> getStaffPanelStream()
	{
		return sheetMusic.staffList.stream().map(this::getComponentByStaff);
	}

	private StaffComponent getComponentByStaff(Staff staff)
	{
		// TODO: change Set to Map for performance
		return staffComponentSet.stream().filter(p -> p.staff == staff).findAny().get();
	}

	@Override
	public SheetMusic getModel() {
		return this.sheetMusic;
	}

	@Override
	public IComponent getModelParent() {
		return null;
	}

	@Override
	public StaffComponent getFocusedChild() {
		// TODO: make changing focus possible
		return getComponentByStaff(sheetMusic.staffList.get(0));
	}

	@Override
	public AbstractHandler getHandler() {
		return this.handler;
	}

	private static ContextAction<SheetMusicComponent> mkAction(Consumer<SheetMusicComponent> lambda) {
		ContextAction<SheetMusicComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}

	private static ContextAction<SheetMusicComponent> mkFailableAction(Function<SheetMusicComponent, Explain> lambda) {
		ContextAction<SheetMusicComponent> action = new ContextAction<>();
		return action.setRedo(lambda);
	}
}
