package com.healthbar.ui;

import com.healthbar.model.BlinkMode;
import com.healthbar.model.IndicatorSetup;
import com.healthbar.model.TrackedEffectEntry;
import java.awt.Component;
import java.awt.Container;
import java.util.Collections;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class EffectListPanelTimeoutTest
{
	@Test
	public void editsSecondsIndependentlyAndRestoresSavedValues() throws Exception
	{
		SwingUtilities.invokeAndWait(() -> {
			TrackedEffectEntry entry = new TrackedEffectEntry("STAMINA", BlinkMode.ON_EXPIRE, 0, 20);
			Runnable save = mock(Runnable.class);
			EffectListPanel panel = new EffectListPanel(mock(ItemManager.class), mock(SpriteManager.class),
				() -> {}, save, () -> {});
			panel.setActiveSetup(new IndicatorSetup("Test", Collections.singletonList(entry)));
			panel.rebuild();
			JTextField minutes = findField(panel, "timeoutMinutes");
			JTextField seconds = findField(panel, "timeoutSeconds");
			assertEquals("20", minutes.getText());
			assertEquals("0", seconds.getText());
			verifyNoInteractions(save);
			minutes.setText("0");
			seconds.setText("30");
			assertEquals(30_000L, entry.getTimeoutMillis());
			verify(save, times(2)).run();
			seconds.setText("-1");
			seconds.setText("nope");
			seconds.setText("999999999999999999");
			assertEquals(30, entry.getTimeoutSeconds());
			verify(save, times(2)).run();
			panel.rebuild();
			assertEquals("0", findField(panel, "timeoutMinutes").getText());
			assertEquals("30", findField(panel, "timeoutSeconds").getText());
			findField(panel, "timeoutSeconds").setText("0");
			assertEquals(0, entry.getTimeoutMillis());
		});
	}

	private JTextField findField(Container parent, String name)
	{
		for (Component child : parent.getComponents())
		{
			if (child instanceof JTextField && name.equals(child.getName()))
			{
				return (JTextField) child;
			}
			if (child instanceof Container)
			{
				JTextField found = findField((Container) child, name);
				if (found != null) return found;
			}
		}
		return null;
	}
}
