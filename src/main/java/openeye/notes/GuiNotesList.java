package openeye.notes;

import com.google.common.base.Strings;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;
import openeye.notes.entries.NoteEntry;
import org.lwjgl.opengl.GL11;

public class GuiNotesList extends GuiScrollingList {

	private static final int ENTRY_HEIGHT = 50;
	private final List<NoteEntry> notes;
	private final GuiNotes owner;
	private final Minecraft mc;

	public GuiNotesList(GuiNotes owner, Minecraft mc, int width, int height, int top, int bottom, int screenWidth, int screenHeight, List<NoteEntry> notes) {
		super(mc, width, height, top, bottom, 0, ENTRY_HEIGHT, screenWidth, screenHeight);
		this.mc = mc;
		this.owner = owner;
		this.notes = notes;
	}

	@Override
	protected int getSize() {
		return notes.size();
	}

	@Override
	protected void elementClicked(int id, boolean var2) {
		owner.selectNote(id);
	}

	@Override
	protected boolean isSelected(int id) {
		return owner.isNoteSelected(id);
	}

	@Override
	protected void drawBackground() {}

	@Override
	protected void drawSlot(int slotId, int right, int top, int height, Tessellator tessellator) {
		NoteEntry entry = notes.get(slotId);

		GL11.glColor3f(1, 1, 1);
		int left = this.left + 10;
		mc.renderEngine.bindTexture(GuiButtonNotes.TEXTURE);
		NoteIcons icon = entry.category.icon;
		owner.drawTexturedModalRect(left, top, icon.textureU + 2, icon.textureV + 2, 16, 16);

		owner.drawString(mc.fontRenderer, entry.title().getFormattedText(), left + 20, top + 4, 0xFFFFFF);
		String description = entry.content().getFormattedText();

		int width = right - left;
		if (!Strings.isNullOrEmpty(description))
			mc.fontRenderer.drawSplitString(description, left, top + 20, left + width - 10, 0xCCCCCC);
	}
}
