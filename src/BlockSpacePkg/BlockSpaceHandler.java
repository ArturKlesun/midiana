package BlockSpacePkg;

import Model.*;
import Stuff.Tools.FileProcessor;
import Stuff.Tools.Logger;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlockSpaceHandler extends AbstractHandler {

	public BlockSpaceHandler(BlockSpace context) { super(context); }

	@Override
	protected void initActionMap() {

		JFileChooser jsonChooser = new JFileChooser("/home/klesun/yuzefa_git/storyspaceContent/");
		jsonChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith(".stsp.json") || f.isDirectory();
			}

			public String getDescription() {
				return "Json BlockSpace data";
			}
		});

		addCombo(ctrl, k.VK_M).setDo((this.getContext())::addMusicBlock);
		addCombo(ctrl, k.VK_T).setDo((this.getContext())::addTextBlock);
		addCombo(ctrl, k.VK_I).setDo((this.getContext())::addImageBlock);

		addCombo(ctrl, k.VK_G).setDo(makeSaveFileDialog(FileProcessor::saveStoryspace, jsonChooser, "stsp.json"));
		addCombo(ctrl, k.VK_R).setDo(combo -> {
			int sVal = jsonChooser.showOpenDialog(getContext().getWindow());
			if (sVal == JFileChooser.APPROVE_OPTION) {
				FileProcessor.openStoryspace(jsonChooser.getSelectedFile(), getContext());
			}
			makeSaveFileDialog(FileProcessor::saveStoryspace, jsonChooser, "stsp.json");
		});

		addCombo(ctrl, k.VK_EQUALS).setDo((this.getContext())::scale);
		addCombo(ctrl, k.VK_MINUS).setDo((this.getContext())::scale);
		addCombo(ctrl, k.VK_MINUS).setDo(() -> this.getContext().getSettings().scale(-1));
		addCombo(ctrl, k.VK_EQUALS).setDo(() -> this.getContext().getSettings().scale(+1));

//		addCombo(ctrl, k.VK_K).setDo(() -> { Logger.fatal("Artificial fatal was generated (sorry if you pressed this shortcut occasionally D= )"); });
	}
	@Override
	public Boolean mousePressedFinal(ComboMouse mouse) {
		if (mouse.leftButton) {
			getContext().requestFocus();
		}
		return true;
	}
	@Override
	public Boolean mouseDraggedFinal(ComboMouse mouse) {
		Component eventOrigin = getFirstParentComponent(mouse.getOrigin());
		eventOrigin.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

		Arrays.asList(getContext().getComponents()).stream().forEach(component
			-> component.setLocation(component.getX() + mouse.dx, component.getY() + mouse.dy));
		mouseLocation.move(mouse.dx, mouse.dy);
		return true;
	}
	@Override
	public Boolean mouseReleasedFinal(ComboMouse mouse) {
		IComponentModel eventOrigin = (IComponentModel)getFirstParentComponent(mouse.getOrigin());
		eventOrigin.setCursor(eventOrigin.getModelHelper().getDefaultCursor());
		return true;
	}

	@Override
	public BlockSpace getContext() { return (BlockSpace)super.getContext(); }

	private static Component getFirstParentComponent(IModel model) {
		while (!(model instanceof Component)) {
			if (model == null) { Logger.fatal("orphan model detected! " + model.getClass().getSimpleName()); }
			model = model.getModelParent();
		}
		return Component.class.cast(model);
	}

	final private Consumer<Combo> makeSaveFileDialog(BiConsumer<File, BlockSpace> lambda, JFileChooser chooser, String ext) {
		return combo -> {
			int rVal = chooser.showSaveDialog(getContext());
			if (rVal == JFileChooser.APPROVE_OPTION) {
				File fn = chooser.getSelectedFile();
				if (!chooser.getFileFilter().accept(fn)) { fn = new File(fn + "." + ext); }
				// TODO: prompt on overwrite
				lambda.accept(fn, getContext());
			}
		};
	}
}