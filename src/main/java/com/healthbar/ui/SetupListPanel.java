/*
 * Copyright (c) 2026, Abram
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.healthbar.ui;

import com.healthbar.model.IndicatorSetup;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import net.runelite.client.ui.ColorScheme;

public class SetupListPanel extends JPanel
{
	private static final int ADD_BTN_SIZE = 30;
	private static final int ADD_BTN_HEIGHT = 25;
	private static final int SMALL_BTN_SIZE = 25;
	private static final int ROW_HEIGHT = 45;
	private static final int ROW_SPACING = 2;

	private final JPanel listPanel;
	private final Consumer<IndicatorSetup> onSelect;
	private final Runnable onCreate;
	private final Consumer<IndicatorSetup> onRename;
	private final Consumer<IndicatorSetup> onDelete;

	public SetupListPanel(Consumer<IndicatorSetup> onSelect, Runnable onCreate,
		Consumer<IndicatorSetup> onRename, Consumer<IndicatorSetup> onDelete)
	{
		this.onSelect = onSelect;
		this.onCreate = onCreate;
		this.onRename = onRename;
		this.onDelete = onDelete;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);

		add(buildHeader(), BorderLayout.NORTH);
		add(buildScrollableList(), BorderLayout.CENTER);
	}

	private JPanel buildHeader()
	{
		JPanel header = new JPanel(new BorderLayout());
		header.setBackground(ColorScheme.DARK_GRAY_COLOR);
		header.setBorder(new EmptyBorder(10, 10, 5, 10));

		JLabel title = new JLabel("Indicator Setups");
		title.setForeground(Color.WHITE);
		header.add(title, BorderLayout.WEST);

		header.add(ButtonBuilder.create("+")
			.size(ADD_BTN_SIZE, ADD_BTN_HEIGHT)
			.tooltip("Create a new setup")
			.onClick(e -> onCreate.run())
			.build(), BorderLayout.EAST);

		return header;
	}

	private JScrollPane buildScrollableList()
	{
		JScrollPane scroll = new JScrollPane(listPanel);
		scroll.setBackground(ColorScheme.DARK_GRAY_COLOR);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		return scroll;
	}

	public void rebuild(List<IndicatorSetup> setups)
	{
		listPanel.removeAll();

		if (setups.isEmpty())
		{
			JLabel emptyLabel = new JLabel("No setups. Click + to create one.");
			emptyLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
			emptyLabel.setBorder(new EmptyBorder(20, 10, 20, 10));
			listPanel.add(emptyLabel);
		}
		else
		{
			for (IndicatorSetup setup : setups)
			{
				listPanel.add(buildRow(setup));
				listPanel.add(Box.createRigidArea(new Dimension(0, ROW_SPACING)));
			}
		}

		listPanel.revalidate();
		listPanel.repaint();
	}

	private JPanel buildRow(IndicatorSetup setup)
	{
		JPanel row = new JPanel(new BorderLayout(4, 0));
		row.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		row.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createMatteBorder(0, 0, 1, 0, ColorScheme.DARK_GRAY_COLOR),
			new EmptyBorder(8, 10, 8, 10)
		));
		row.setMaximumSize(new Dimension(Integer.MAX_VALUE, ROW_HEIGHT));
		row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

		JPanel textPanel = buildRowText(setup);
		row.add(textPanel, BorderLayout.CENTER);
		row.add(buildRowButtons(setup), BorderLayout.EAST);
		addRowClickListener(row, textPanel, setup);

		return row;
	}

	private JPanel buildRowText(IndicatorSetup setup)
	{
		JLabel nameLabel = new JLabel(setup.getName());
		nameLabel.setForeground(Color.WHITE);

		int count = setup.getEntries().size();
		JLabel countLabel = new JLabel(count + (count == 1 ? " effect" : " effects"));
		countLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);

		JPanel textPanel = new JPanel(new BorderLayout());
		textPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		textPanel.add(nameLabel, BorderLayout.NORTH);
		textPanel.add(countLabel, BorderLayout.SOUTH);
		return textPanel;
	}

	private JPanel buildRowButtons(IndicatorSetup setup)
	{
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, ROW_SPACING, 0));
		btnPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		btnPanel.add(ButtonBuilder.create("Rename")
			.margin(2, 4, 2, 4)
			.onClick(e -> onRename.accept(setup))
			.build());

		btnPanel.add(ButtonBuilder.create("X")
			.size(SMALL_BTN_SIZE, SMALL_BTN_SIZE)
			.margin(0, 0, 0, 0)
			.onClick(e -> onDelete.accept(setup))
			.build());

		return btnPanel;
	}

	private void addRowClickListener(JPanel row, JPanel textPanel, IndicatorSetup setup)
	{
		MouseAdapter clickListener = new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				onSelect.accept(setup);
			}
		};
		row.addMouseListener(clickListener);
		textPanel.addMouseListener(clickListener);
	}
}
