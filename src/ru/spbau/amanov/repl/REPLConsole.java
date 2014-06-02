package ru.spbau.amanov.repl;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.undo.*;
import javax.swing.event.*;

/**
 * This class provide GUI.
 */
public class REPLConsole {

    public static final String GREETING = System.lineSeparator() + ">";
    public static final String SIMPLIFY = "Simplify";
    public static final String EVALUATE = "Evaluate";


    private void init() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        optionPane.addItem(SIMPLIFY);
        optionPane.addItem(EVALUATE);
        frame.add(optionPane, "North");

        initStyles();

        JTextPane textArea = new JTextPane(styledDocument);

        ((AbstractDocument) styledDocument).setDocumentFilter(new Filter());
        textArea.setText("Welcome to REPL Console! " + System.lineSeparator() + ">");
        textArea.setEditable(true);
        frame.add(textArea, "Center");

        undoOn();
        Action undoEditAction = new UndoEditAction();
        Action undoStmtAction = new UndoStmtAction(undoStmtManager);
        hightLightOn();
        textArea.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke("control Z"), undoEditAction);
        textArea.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke("control shift Z"), undoStmtAction);
        textArea.getKeymap().addActionForKeyStroke(KeyStroke.getKeyStroke("ENTER"), new ExecuteAction());

        frame.setVisible(true);
        frame.setSize(500, 300);
    }

    private class UndoEditAction extends AbstractAction {
        public void actionPerformed(ActionEvent evt) {
            try {
                if (!styledDocument.getText(endOffset(styledDocument) - 1, 1).equals(">")) {
                    if (undoEditManager.canUndo()) {
                        undoEditManager.undo();
                    }
                }
            } catch (CannotUndoException e) {
                Toolkit.getDefaultToolkit().beep();
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private class ExecuteAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                JTextPane source = (JTextPane) e.getSource();
                String text = styledDocument.getText(0, styledDocument.getLength());
                String userInput = text.substring(lastLineIndex(styledDocument) + GREETING.length());

                String result;
                if (simplifyMode()) {
                    result = interpreter.simplify();
                } else {
                    Float val = interpreter.execute();
                    if (val == null) {
                        result = null;
                    } else {
                        result = val.toString();
                    }
                }

                if (result == null) {
                    String msg = interpreter.getErrorMsg();
                    if (msg.isEmpty()) {
                        msg = "empty command";
                    }
                    result = "Error: " + msg;
                }
                interpreter.clean();
                highlightOff();
                styledDocument.insertString(endOffset(styledDocument), System.lineSeparator() + result, null);
                styledDocument.insertString(endOffset(styledDocument), GREETING, null);
                source.setCaretPosition(endOffset(styledDocument));
                hightLightOn();
                undoStmtListener.endEdit();
                interpreter.saveContext();
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class UndoStatementListener implements UndoableEditListener {
        public void endEdit() {
            edit.end();
            undoStmtManager.addEdit(edit);
            edit = new CompoundEdit();
        }

        public void undoableEditHappened(UndoableEditEvent e) {
            UndoableEdit ed = e.getEdit();
            if (ed.canUndo()) {
                edit.addEdit(ed);
            }
        }
        CompoundEdit edit = new CompoundEdit();
    }

    private class UndoStmtAction extends AbstractAction {
        public UndoStmtAction(UndoManager m) {
            manager = m;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (manager.canUndo()) {
                    highlightOff();
                    manager.undo();
                    interpreter.repareContext();
                    hightLightOn();
                }
            } catch (CannotUndoException e1) {
                //nothin to do
            }
        }

        private UndoManager manager;
    }

    private class HighlitListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
                process(e);
        }

        public void removeUpdate(DocumentEvent e) {
                process(e);
        }

        public void changedUpdate(DocumentEvent e) {
            //Plain text components do not fire these events
        }

        private void process(final DocumentEvent e) {
            try {
                final String text = styledDocument.getText(0, styledDocument.getLength());
                final int startPos = lastLineIndex(styledDocument) + GREETING.length();
                final String userInput = text.substring(startPos);
                if (userInput.isEmpty()) {
                    interpreter.clean();
                    return;
                }

                Runnable doHighlight = new Runnable() {
                    @Override
                    public void run() {
                        interpreter.interpret(userInput);
                        undoOff();
                        highlightOff();
                        styledDocument.setCharacterAttributes(startPos, userInput.length(), styledDocument.getStyle("default"), false);
                        for (Highliter.HighlightRegion r : interpreter.getHighlightRegions(!simplifyMode())) {
                            styledDocument.setCharacterAttributes(startPos + r.place.offset, r.place.len, styledDocument.getStyle(r.style), false);
                        }

                        int errorPos = interpreter.getErrorPos();

                        if (!interpreter.isValid() && errorPos != -1) {
                            styledDocument.setCharacterAttributes(startPos + errorPos, userInput.length() - errorPos, styledDocument.getStyle("error"), false);
                        }

                        undoOn();
                        hightLightOn();
                    }
                };

            SwingUtilities.invokeLater(doHighlight);
            } catch (BadLocationException exc) {
            }
        }
    }


    public static void main(String[] args) {
        REPLConsole replConsole = new REPLConsole();
        replConsole.init();
    }


    private class Filter extends DocumentFilter {


        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
            if (cursorOnLastLine(offset, fb)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        public void remove(final FilterBypass fb, final int offset, final int length) throws BadLocationException {
            if (offset > lastLineIndex(fb.getDocument()) + 1) {
                super.remove(fb, offset, length);
            }
        }

        public void replace(final FilterBypass fb, final int offset, final int length, final String text, final AttributeSet attrs)
                throws BadLocationException {
            if (cursorOnLastLine(offset, fb)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

    }

    private static boolean cursorOnLastLine(int offset, DocumentFilter.FilterBypass fb) {
        return cursorOnLastLine(offset, fb.getDocument());
    }

    private static boolean cursorOnLastLine(int offset, Document document) {
        int lastLineIndex = 0;
        try {
            lastLineIndex = lastLineIndex(document);
        } catch (BadLocationException e) {
            return false;
        }
        return offset > lastLineIndex;
    }

    private static int lastLineIndex(Document document) throws BadLocationException {
        return document.getText(0, document.getLength()).lastIndexOf(System.lineSeparator());
    }

    private int endOffset(Document document) {
        return document.getEndPosition().getOffset() - 1;
    }

    private void hightLightOn() {
        styledDocument.addDocumentListener(highliter);
    }

    private void highlightOff() {
        styledDocument.removeDocumentListener(highliter);
    }

    private void initStyles() {
        Style style = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style def = styledDocument.addStyle("default", style);
        def.addAttribute(StyleConstants.Underline, false);
        Style errorStyle = styledDocument.addStyle("error", style);
        errorStyle.addAttribute(StyleConstants.Foreground, Color.red);
        errorStyle.addAttribute(StyleConstants.Underline, true);
        Style operand = styledDocument.addStyle("operand", style);
        StyleConstants.setForeground(operand, Color.BLUE);
        StyleConstants.setBold(operand, true);
        Style operator = styledDocument.addStyle("operator", style);
        StyleConstants.setForeground(operator, Color.MAGENTA);
        StyleConstants.setBold(operator, true);

    }

    private void undoOn() {
        styledDocument.addUndoableEditListener(undoEditManager);
        styledDocument.addUndoableEditListener(undoStmtListener);
    }

    private void undoOff() {
        styledDocument.removeUndoableEditListener(undoEditManager);
        styledDocument.removeUndoableEditListener(undoStmtListener);
    }

    private boolean simplifyMode() {
        return SIMPLIFY.equals(optionPane.getSelectedItem());
    }

    private JFrame frame;
    private final Interpreter interpreter = new Interpreter();
    private final UndoManager undoEditManager = new UndoManager();
    private final UndoManager undoStmtManager = new UndoManager();
    private final UndoStatementListener undoStmtListener = new UndoStatementListener();
    private final StyledDocument styledDocument = new DefaultStyledDocument();
    private final JComboBox<String> optionPane = new JComboBox<>();

    private final HighlitListener highliter = new HighlitListener();
}