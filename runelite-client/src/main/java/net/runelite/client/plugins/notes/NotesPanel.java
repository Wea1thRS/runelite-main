/*
 * Copyright (c) 2018 Charlie Waters
 * Copyright (c) 2018, Psikoi <https://github.com/psikoi>
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
package net.runelite.client.plugins.notes;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.inject.Singleton;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Slf4j
@Singleton
class NotesPanel extends PluginPanel
{
	private final JTextPane notesEditor = new JTextPane();
	private final UndoManager undoRedo = new UndoManager();

	void init(final NotesConfig config)
	{
		// this may or may not qualify as a hack
		// but this lets the editor pane expand to fill the whole parent panel
		getParent().setLayout(new BorderLayout());
		getParent().add(this, BorderLayout.CENTER);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		setBackground(ColorScheme.DARK_GRAY_COLOR);

//		notesEditor.setTabSize(2);
//		notesEditor.setLineWrap(true);
//		notesEditor.setWrapStyleWord(true);

		JPanel notesContainer = new JPanel();
		notesContainer.setLayout(new BorderLayout());
		notesContainer.setBackground(ColorScheme.DARKER_GRAY_COLOR);

		notesEditor.setOpaque(false);

		// load note text
		String data = config.notesData();
		notesEditor.setText(data);

		// setting the limit to a 500 as UndoManager registers every key press,
		// which means that be default we would be able to undo only a sentence.
		// note: the default limit is 100
		undoRedo.setLimit(500);
		notesEditor.getDocument().addUndoableEditListener(e -> undoRedo.addEdit(e.getEdit()));

		notesEditor.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
		notesEditor.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");
		notesEditor.getInputMap().put(KeyStroke.getKeyStroke("control B"), "Bold");
		notesEditor.getInputMap().put(KeyStroke.getKeyStroke("control I"), "Italic");
		notesEditor.getInputMap().put(KeyStroke.getKeyStroke("control U"), "Underline");
		notesEditor.getInputMap().put(KeyStroke.getKeyStroke("control S"), "Strikethrough");

		notesEditor.getActionMap().put("Undo", new AbstractAction("Undo")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					if (undoRedo.canUndo())
					{
						undoRedo.undo();
					}
				}
				catch (CannotUndoException ex)
				{
					log.warn("Notes Document Unable To Undo: " + ex);
				}
			}
		});

		notesEditor.getActionMap().put("Redo", new AbstractAction("Redo")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					if (undoRedo.canRedo())
					{
						undoRedo.redo();
					}
				}
				catch (CannotUndoException ex)
				{
					log.warn("Notes Document Unable To Redo: " + ex);
				}
			}
		});

		notesEditor.getActionMap().put("Bold", new AbstractAction("Bold")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				StyledDocument doc = (StyledDocument) notesEditor.getDocument();
				try
				{
					int selection = notesEditor.getSelectionStart();
					if (selection != notesEditor.getSelectionEnd())
					{
						MutableAttributeSet set = new SimpleAttributeSet(doc.getCharacterElement(notesEditor.getSelectionStart()).getAttributes().copyAttributes());
						StyleConstants.setBold(set, !StyleConstants.isBold(doc.getCharacterElement(notesEditor.getSelectionStart()).getAttributes()));
						doc.setCharacterAttributes(notesEditor.getSelectionStart(), notesEditor.getSelectedText().length(), set, true);
					}
					else
					{
						selection = notesEditor.getCaretPosition();
						MutableAttributeSet set = new SimpleAttributeSet(doc.getCharacterElement(selection).getAttributes().copyAttributes());
						StyleConstants.setBold(set, false);
						doc.setCharacterAttributes(selection, 1, set, true);
					}
				}
				catch (NullPointerException ex)
				{
					ex.printStackTrace();
				}
			}
		});

		notesEditor.getActionMap().put("Italic", new AbstractAction("Italic")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				StyledDocument doc = (StyledDocument) notesEditor.getDocument();
				try
				{
					int selection = notesEditor.getSelectionStart();
					if (selection != notesEditor.getSelectionEnd())
					{
						MutableAttributeSet set = new SimpleAttributeSet(doc.getCharacterElement(notesEditor.getSelectionStart()).getAttributes().copyAttributes());
						StyleConstants.setItalic(set, !StyleConstants.isItalic(doc.getCharacterElement(notesEditor.getSelectionStart()).getAttributes()));
						doc.setCharacterAttributes(notesEditor.getSelectionStart(), notesEditor.getSelectedText().length(), set, true);
					}
					else
					{
						selection = notesEditor.getCaretPosition();
						MutableAttributeSet set = new SimpleAttributeSet(doc.getCharacterElement(selection).getAttributes().copyAttributes());
						StyleConstants.setItalic(set, false);
						doc.setCharacterAttributes(selection, 1, set, true);
					}
				}
				catch (NullPointerException ex)
				{
					ex.printStackTrace();
				}
			}
		});

		notesEditor.getActionMap().put("Underline", new AbstractAction("Underline")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				StyledDocument doc = (StyledDocument) notesEditor.getDocument();
				try
				{
					int selection = notesEditor.getSelectionStart();
					if (selection != notesEditor.getSelectionEnd())
					{
						MutableAttributeSet set = new SimpleAttributeSet(doc.getCharacterElement(notesEditor.getSelectionStart()).getAttributes().copyAttributes());
						StyleConstants.setUnderline(set, !StyleConstants.isUnderline(doc.getCharacterElement(notesEditor.getSelectionStart()).getAttributes()));
						doc.setCharacterAttributes(notesEditor.getSelectionStart(), notesEditor.getSelectedText().length(), set, true);
					}
					else
					{
						selection = notesEditor.getCaretPosition();
						MutableAttributeSet set = new SimpleAttributeSet(doc.getCharacterElement(selection).getAttributes().copyAttributes());
						StyleConstants.setUnderline(set, false);
						doc.setCharacterAttributes(selection, 1, set, true);
					}
				}
				catch (NullPointerException ex)
				{
					ex.printStackTrace();
				}
			}
		});

		notesEditor.getActionMap().put("Strikethrough", new AbstractAction("Strikethrough")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				StyledDocument doc = (StyledDocument) notesEditor.getDocument();
				try
				{
					int selection = notesEditor.getSelectionStart();
					if (selection != notesEditor.getSelectionEnd())
					{
						MutableAttributeSet set = new SimpleAttributeSet(doc.getCharacterElement(notesEditor.getSelectionStart()).getAttributes().copyAttributes());
						StyleConstants.setStrikeThrough(set, !StyleConstants.isStrikeThrough(doc.getCharacterElement(notesEditor.getSelectionStart()).getAttributes()));
						doc.setCharacterAttributes(notesEditor.getSelectionStart(), notesEditor.getSelectedText().length(), set, true);
					}
					else
					{
						selection = notesEditor.getCaretPosition();
						MutableAttributeSet set = new SimpleAttributeSet(doc.getCharacterElement(selection).getAttributes().copyAttributes());
						StyleConstants.setStrikeThrough(set, false);
						doc.setCharacterAttributes(selection, 1, set, true);
					}
				}
				catch (NullPointerException ex)
				{
					ex.printStackTrace();
				}
			}
		});

		notesEditor.addFocusListener(new FocusListener()
		{
			@Override
			public void focusGained(FocusEvent e)
			{

			}

			@Override
			public void focusLost(FocusEvent e)
			{
				notesChanged(notesEditor.getDocument());
			}

			private void notesChanged(Document doc)
			{
				try
				{
					// get document text and save to config whenever editor is changed
					String data = doc.getText(0, doc.getLength());
					config.notesData(data);
				}
				catch (Exception ex)
				{
					log.warn("Notes Document Bad Location: " + ex);
				}
			}
		});
		notesContainer.add(notesEditor, BorderLayout.CENTER);
		notesContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

		add(notesContainer, BorderLayout.CENTER);
	}

	void setNotes(String data)
	{
		notesEditor.setText(data);
	}
}
