/**
 * Created by Huang on 2015/12/29.
 */

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class UI extends JFrame {
    public static UI ui;
    public static Parse parser;
    private final static JToolBar TOOLBAR = new JToolBar("Tool Bar");
    private static UI.JCloseableTabbedPane editTP = new UI.JCloseableTabbedPane();
    private static HashMap<JScrollPane, JTextPane> map = new HashMap<JScrollPane,JTextPane>();
    /* 默认字体 */
    public static final JTextPane input = new JTextPane();
    final JTextPane synResult = new JTextPane();
    final JTextPane console = new JTextPane();
    private final static Font LABELFONT = new Font("幼圆", Font.BOLD, 20);
    /* 编辑区字体 */
    private Font font = new Font("Courier New", Font.PLAIN, 19);
    /* 控制台字体 */
    private Font consoleFont = new Font("微软雅黑", Font.PLAIN, 19);
    private JButton newButton;
    private JButton openButton;
    private JButton runButton;
    private JButton saveButton;
    FileFilter filter = new FileFilter() {
        public String getDescription() {
            return "CMM file(*.cmm)";
        }
        public boolean accept(File file) {
            String tmp = file.getName().toLowerCase();
            if (tmp.endsWith(".cmm") || tmp.endsWith(".CMM")||file.isDirectory()) {
                return true;
            }
            return false;
        }
    };

    public UI(){
        JFrame jf = new JFrame("CMM Compiler");
        Container container = jf.getContentPane();
        container.setLayout(null);
        jf.setVisible(true);
        jf.setResizable(false);
        jf.setSize(1108,800);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        container.setBackground(Color.white);
        //工具栏区域
        TOOLBAR.setFloatable(false);
        TOOLBAR.setBounds(0, 0, 1100,48);
        container.add(TOOLBAR);
        newButton = new JButton(new ImageIcon(getClass().getResource("/images/new.png")));
        newButton.setSize(50,50);
        newButton.setBorder(null);
        openButton = new JButton(new ImageIcon(getClass().getResource("/images/open.png")));
        openButton.setBorder(null);
        runButton = new JButton(new ImageIcon(getClass().getResource("/images/run.png")));
        runButton.setBorder(null);
        saveButton = new JButton(new ImageIcon(getClass().getResource("/images/save.png")));
        saveButton.setBorder(null);
        TOOLBAR.add(newButton);
        TOOLBAR.addSeparator();
        TOOLBAR.add(openButton);
        TOOLBAR.addSeparator();
        TOOLBAR.add(saveButton);
        TOOLBAR.addSeparator();
        TOOLBAR.add(runButton);
        //代码编辑区
        editTP.setBounds(0,0,850,500);
        editTP.setFont(LABELFONT);
        final JTextPane editor = new JTextPane();
        editor.setFont(font);
        editor.getDocument().addDocumentListener(new UI.Highlighter(editor));
        JScrollPane scrollPane = new JScrollPane(editor);
        TextLineNumber tln = new TextLineNumber(editor);
        scrollPane.setRowHeaderView(tln);
        map.put(scrollPane, editor);
        editTP.add(scrollPane, "test" + ".cmm");
        JPanel editPanel = new JPanel(null);
        editPanel.setBounds(0, TOOLBAR.getHeight(), 850, 500);
        editPanel.setBackground(getBackground());
        editPanel.setForeground(new Color(238, 238, 238));
        editPanel.add(editTP);
        container.add(editPanel);
        //运行结果区
        final JLabel resultLabel = new JLabel("| AST");
        resultLabel.setFont( new Font("微软雅黑", Font.BOLD, 20));
        //final JTextPane synResult = new JTextPane();
        synResult.setEditable(false);
        Border b1 = BorderFactory.createLineBorder(Color.lightGray);
        Border b2 = BorderFactory.createEtchedBorder();
        synResult.setBorder(BorderFactory.createCompoundBorder(b1,b2));
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBounds(850, TOOLBAR.getHeight(), 250, 500);
        resultPanel.setBackground(getBackground());
        resultPanel.setForeground(new Color(238, 238, 238));
        resultPanel.add(BorderLayout.NORTH,resultLabel);
        resultPanel.add(BorderLayout.CENTER,new JScrollPane(synResult));
        container.add(resultPanel);
        //控制台
        final JLabel consoleLabel = new JLabel("| Console");
        consoleLabel.setFont( new Font("微软雅黑", Font.BOLD, 20));
        JPanel consolePanel = new JPanel(new BorderLayout());
        //final JTextPane console = new JTextPane();
        console.setEditable(false);
        console.setFont(consoleFont);
        consolePanel.add(BorderLayout.NORTH,consoleLabel);
        consolePanel.add(BorderLayout.CENTER,new JScrollPane(console));
        consolePanel.setBounds(0,editPanel.getHeight()+TOOLBAR.getHeight(),850,210);
        consolePanel.setBackground(new Color(238, 238, 238));
        console.setBorder(BorderFactory.createCompoundBorder(b1,b2));
        container.add(consolePanel);
//        console.setBorder(BorderFactory.createLineBorder(Color.lightGray,3));
/*        consolePanel.add(consoleLabel,BorderLayout.NORTH);*/
        //控制台
        final JLabel inputLabel = new JLabel("| Input");
        inputLabel.setFont( new Font("微软雅黑", Font.BOLD, 20));
        JPanel inputPanel = new JPanel(new BorderLayout());
        //final JTextPane input = new JTextPane();
        input.setFont(consoleFont);
        inputPanel.add(BorderLayout.NORTH,inputLabel);
        inputPanel.add(BorderLayout.CENTER,new JScrollPane(input));
        inputPanel.setBounds(consolePanel.getWidth(),editPanel.getHeight()+TOOLBAR.getHeight(),250,210);
        inputPanel.setBackground(new Color(238, 238, 238));
        input.setBorder(BorderFactory.createCompoundBorder(b1,b2));
        container.add(inputPanel);

        openButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                open();
            }
        });
        newButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramActionEvent) {
                create(null);
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Complier comp = new Complier();


                //parser.ReInit(fs);
                //Parse parser = new Parse(fs);
                try {
                    synResult.setText("");
                    String text = map.get(editTP.getSelectedComponent())
                            .getText()+ "  ";
                    Preprocessor preprocessor = new Preprocessor();
                    text = preprocessor.preprocess(text);
                    //if(Preprocessor.checkComment();
                    //System.out.println(text);
                    InputStream fs = new ByteArrayInputStream((text+" end ").getBytes());
                    parser.ReInit(fs);
                    parser.start();
                    SimpleNode node = (SimpleNode) parser.jjtree.rootNode();
                    node.initString();
                    node.dump(">");
                    //System.out.print(node.getString());
                    synResult.setText(node.getString());
                    //node.initString();
                    Run run = new Run();
                    run.setMessage();
                    run.run(node);
                    if(run.error){
                        console.setText("Error\n"+run.getErrorMessage());
                        if(run.readCount>0){
                            console.setText("Need another"+run.readCount+" Input");

                        }
                    }else
                    {
                        console.setText("No Grammer Error\n" + run.getResult());

                    }
                    input.setText("");
                } catch (ParseException e1) {
                    console.setText(e1.getMessage());
                }
                catch (IOException e2){
                    console.setText(e2.getMessage());;
                }
                catch (TokenMgrError e3){
                    console.setText(e3.getMessage());;

                }
                catch (NullPointerException e4){
                    //System.out.print(e4.getMessage());
                    console.setText("Expression Condition should be the same type");
                }

                //console.setText(run.getResult());
                //console.setText("The error");
                //console.setText(run.getErrorMessage());

            }
        });
        input.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.cyan),BorderFactory.createEtchedBorder()));
            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.lightGray), BorderFactory.createEtchedBorder()));
            }
        });




    }
    private void open() {
        boolean isOpened = false;
        String str = "", fileName = "";
        StringBuilder text = new StringBuilder();
        FileDialog filedialog_load = new FileDialog(this, "打开文件", FileDialog.LOAD);
        filedialog_load.setVisible(true);
            if (filedialog_load.getFile() != null) {
                try {
                    File file = new File(filedialog_load.getDirectory(),filedialog_load.getFile());
                    fileName = file.getName();
                    FileReader file_reader = new FileReader(file);
                    BufferedReader in = new BufferedReader(file_reader);
                    while ((str = in.readLine()) != null)
                        text.append(str + '\n');
                    in.close();
                    file_reader.close();
                } catch (IOException e2) {
                    System.out.print("aaa");
                }
                for (int i = 0; i < editTP.getComponentCount(); i++) {
                    if (editTP.getTitleAt(i).equals(fileName)) {
                        isOpened = true;
                        editTP.setSelectedIndex(i);
                    }
                }
                if (!isOpened) {
                    create(fileName);
                    editTP.setTitleAt(
                            editTP.getComponentCount() - 1, fileName);
                    map.get(editTP.getSelectedComponent()).setText(
                            text.toString());
                    console.setText("");
                    synResult.setText("");

                }

            }

    }
    private void create(String filename) {
        if (filename == null) {
            filename = JOptionPane.showInputDialog("Please input the file name: ");
            if (filename == null || filename.equals("")) {
                JOptionPane.showMessageDialog(null, "file name cannot be empty!");
                return;
            }
        }

        filename += ".cmm";
       JTextPane editor = new JTextPane();
        editor.getDocument().addDocumentListener(new Highlighter(editor));
        editor.setFont(font);
        JScrollPane scrollPane = new JScrollPane(editor);
        TextLineNumber tln = new TextLineNumber(editor);
        scrollPane.setRowHeaderView(tln);

        map.put(scrollPane, editor);
        editTP.add(scrollPane, filename);
        editTP.setSelectedIndex(editTP.getTabCount() - 1);
    }
    private void save() {
        JTextPane temp = map.get(editTP.getSelectedComponent());
        FileDialog filedialog_save = new FileDialog(this, "保存文件", FileDialog.SAVE);
        filedialog_save.setVisible(false);
        if (temp.getText() != null) {
            filedialog_save.setVisible(true);
            if (filedialog_save.getFile() != null) {
                try {
                    File file = new File(filedialog_save.getDirectory(),
                            filedialog_save.getFile());
                    FileWriter fw = new FileWriter(file);
                    fw.write(map.get(editTP.getSelectedComponent())
                            .getText());
                    fw.close();
                } catch (IOException e2) {
                }
            }
        }
    }
    public static void main(String args[]){
        ui = new UI();
        parser = new Parse(System.in);
    }

    @SuppressWarnings("serial")
    public static class JCloseableTabbedPane extends JTabbedPane implements Serializable {
        public static final String ON_TAB_CLOSE = "ON_TAB_CLOSE";
        public static final String ON_TAB_DOUBLECLICK = "ON_TAB_DOUBLECLICK";
        private JPopupMenu popup = new JPopupMenu();
        private JMenuItem closeItem = new JMenuItem("关闭");

        public JCloseableTabbedPane() {
            super();
            popup.add(closeItem);
            closeItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent arg0) {
                    removeTabAt(getSelectedIndex());
                }
            });
            init();
        }

        public JCloseableTabbedPane(int tabPlacement) {
            super(tabPlacement);
            init();
        }

        public JCloseableTabbedPane(int tabPlacement, int tabLayoutPolicy) {
            super(tabPlacement, tabLayoutPolicy);
            init();
        }

        protected void init() {
            addMouseListener(new DefaultMouseAdapter());
            addCloseListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (e.getActionCommand().equals(
                            JCloseableTabbedPane.ON_TAB_CLOSE)) {
                        removeTabAt(getSelectedIndex());
                    }
                }
            });
        }

        public void setIconDrawCenter(int index, boolean drawCenter) {
            ((CloseIcon) getIconAt(index)).setDrawCenter(drawCenter);
            repaint();
        }

        public boolean isDrawCenter(int index) {
            return ((CloseIcon) getIconAt(index)).isDrawCenter();
        }

        protected EventListenerList closeListenerList = new EventListenerList();

        public void addCloseListener(ActionListener l) {
            closeListenerList.add(ActionListener.class, l);
        }

        public void removeCloseListener(ActionListener l) {
            closeListenerList.remove(ActionListener.class, l);
        }

        protected void fireClosed(ActionEvent e) {
            Object[] listeners = closeListenerList.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ActionListener.class) {
                    ((ActionListener) listeners[i + 1]).actionPerformed(e);
                }
            }
        }

        // 鼠标监听器
        class DefaultMouseAdapter extends MouseAdapter {
            CloseIcon icon;
            public void mousePressed(MouseEvent e) {
                int index = indexAtLocation(e.getX(), e.getY());
                if (index != -1) {
                    icon = (CloseIcon) getIconAt(index);
                    if (icon.getBounds().contains(e.getPoint())) {
                        icon.setPressed(true);
                        fireClosed(new ActionEvent(e.getComponent(),
                                ActionEvent.ACTION_PERFORMED, ON_TAB_CLOSE));
                    } else if (e.getClickCount() == 2) {
                        fireClosed(new ActionEvent(e.getComponent(),
                                ActionEvent.ACTION_PERFORMED, ON_TAB_DOUBLECLICK));
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (icon != null) {
                    icon.setPressed(false);
                    icon = null;
                    repaint();
                }
                if (popup != null) {
                    if (!SwingUtilities.isRightMouseButton(e)) {
                        return;
                    }

                    if (indexAtLocation(e.getX(), e.getY()) != -1) {
                        popup.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        }

        public Icon getIconAt(int index) {
            Icon icon = super.getIconAt(index);
            if (icon == null || !(icon instanceof CloseIcon)) {
                super.setIconAt(index, new CloseIcon());
            }
            return super.getIconAt(index);
        }

        // 画出关闭按钮
        class CloseIcon implements Icon {
            Rectangle rec = new Rectangle(0, 0, 15, 16);
            private boolean pressed = false;
            private boolean drawCenter = true;

            public synchronized void paintIcon(Component c, Graphics g, int x1,
                    int y1) {
                int x = x1, y = y1;
                if (pressed) {
                    x++;
                    y++;
                }
                rec.x = x;
                rec.y = y;
                Color oldColor = g.getColor();
                g.setColor(UIManager.getColor("TabbedPane.highlight"));
                g.drawLine(x, y, x, y + rec.height);
                g.drawLine(x, y, x + rec.width, y);
                g.setColor(UIManager.getColor("TabbedPane.shadow"));
                g.drawLine(x, y + rec.height, x + rec.width, y + rec.height);
                g.drawLine(x + rec.width, y, x + rec.width, y + rec.height);
                g.setColor(UIManager.getColor("TabbedPane.foreground"));
                // left top
                g.drawRect(x + 4, y + 4, 1, 1);
                g.drawRect(x + 5, y + 5, 1, 1);
                g.drawRect(x + 5, y + 9, 1, 1);
                g.drawRect(x + 4, y + 10, 1, 1);
                // center
                if (drawCenter) {
                    g.drawRect(x + 6, y + 6, 1, 1);
                    g.drawRect(x + 8, y + 6, 1, 1);
                    g.drawRect(x + 6, y + 8, 1, 1);
                    g.drawRect(x + 8, y + 8, 1, 1);
                }
                // right top
                g.drawRect(x + 10, y + 4, 1, 1);
                g.drawRect(x + 9, y + 5, 1, 1);
                // right bottom
                g.drawRect(x + 9, y + 9, 1, 1);
                g.drawRect(x + 10, y + 10, 1, 1);
                g.setColor(oldColor);
            }

            @SuppressWarnings("unused")
            private void drawRec(Graphics g, int x, int y) {
                g.drawRect(x, y, 1, 1);
            }

            public Rectangle getBounds() {
                return rec;
            }

            public void setBounds(Rectangle rec) {
                this.rec = rec;
            }

            public int getIconWidth() {
                return rec.width;
            }

            public int getIconHeight() {
                return rec.height;
            }

            public void setPressed(boolean pressed) {
                this.pressed = pressed;
            }

            public void setDrawCenter(boolean drawCenter) {
                this.drawCenter = drawCenter;
            }

            public boolean isPressed() {
                return pressed;
            }

            public boolean isDrawCenter() {
                return drawCenter;
            }
        };

    }

    /**
     * Created by Huang on 2016/1/4.
     */
    public static class Highlighter implements DocumentListener {

            private Set<String> keywords;
            private Style keywordStyle;
            private Style normalStyle;
            private Style commentStyle;
    //        private boolean comment = false;
            private static int commentNum=0;
            private static int type=-1;
            private static final int SINGLELINE = 0;
            private static final int MULTILINE = 1;

            public Highlighter(JTextPane editor) {
                // 准备着色使用的样式
                keywordStyle = ((StyledDocument) editor.getDocument()).addStyle("Keyword_Style", null);
                normalStyle = ((StyledDocument) editor.getDocument()).addStyle("Normal_Style", null);
                commentStyle = ((StyledDocument) editor.getDocument()).addStyle("Comment_Style", null);
                StyleConstants.setForeground(keywordStyle, Color.MAGENTA);
                StyleConstants.setForeground(normalStyle, Color.BLACK);
                StyleConstants.setForeground(commentStyle, Color.GREEN);

                // 准备关键字
                keywords = new HashSet<String>();
                keywords.add("int");
                keywords.add("real");
                keywords.add("if");
                keywords.add("while");
                keywords.add("else");
                keywords.add("double");
                keywords.add("read");
                keywords.add("write");
                keywords.add("break");
                keywords.add("boolean");
                keywords.add("char");
                keywords.add("true");
                keywords.add("false");
            }

            public void coloring(StyledDocument doc, int pos, int len) throws BadLocationException {
                // 取得插入或者删除后影响到的单词.
                // 例如"public"在b后插入一个空格, 就变成了:"pub lic", 这时就有两个单词要处理:"pub"和"lic"
                // 这时要取得的范围是pub中p前面的位置和lic中c后面的位置
                int start = indexOfWordStart(doc, pos);
                int end = indexOfWordEnd(doc, pos + len);

     //           System.out.print(start+" "+end);
                char ch;
                while (start < end) {
    //                System.out.println("1..");
    //                System.out.println(commentNum);
                    ch = getCharAt(doc, start);
                    if(commentNum!=0){
                        if(type==SINGLELINE){
                            if(ch=='\n')
                                commentNum--;
                            else {
                                SwingUtilities.invokeLater(new ColoringTask(doc, start, 1, commentStyle));
                                start++;
                            }
                        }
                        else{
                            if(ch=='/') {
                                char c = getCharAt(doc, start - 1);
                                if (c == '*') {
                                    commentNum--;
                                    SwingUtilities.invokeLater(new ColoringTask(doc, start - 1, 2, commentStyle));
                                    start++;
                                } else {
                                    SwingUtilities.invokeLater(new ColoringTask(doc, start, 1, commentStyle));
                                    start++;
                                }
                            }else if(ch=='*'){
                                char c= getCharAt(doc,start-1);
                                if(c=='/')
                                    commentNum++;
    //                            comment=true;
                                    SwingUtilities.invokeLater(new ColoringTask(doc,start,1, commentStyle));
                                    start++;
                            }
                            else {
                                SwingUtilities.invokeLater(new ColoringTask(doc, start, 1, commentStyle));
                                start++;
                            }
                        }
                    }else{
                        if (Character.isLetter(ch) || ch == '_') {
                            // 如果是以字母或者下划线开头, 说明是单词
                            // pos为处理后的最后一个下标
                            start = coloringWord(doc, start);
                        } else if(ch=='*'||ch=='/'){
                            if(start!=0) {
                                char c = getCharAt(doc, start - 1);
                                if (c == '/') {
                                    if (ch == '*')
                                        type = MULTILINE;
                                    else
                                        type = SINGLELINE;
                                    commentNum++;
    //                            comment=true;
                                    SwingUtilities.invokeLater(new ColoringTask(doc, start - 1, 2, commentStyle));

                                } else {
                                    SwingUtilities.invokeLater(new ColoringTask(doc, start, 1, normalStyle));
                                }
                            }
                            start++;
                        }
                        else {
                            SwingUtilities.invokeLater(new ColoringTask(doc, start, 1, normalStyle));
                            ++start;
                        }
                    }

                }
            }

            /**
             * 对单词进行着色, 并返回单词结束的下标.
             *
             * @param doc
             * @param pos
             * @return
             * @throws javax.swing.text.BadLocationException
             */
            public int coloringWord(StyledDocument doc, int pos) throws BadLocationException {
                int wordEnd = indexOfWordEnd(doc, pos);
                String word = doc.getText(pos, wordEnd - pos);

               if (keywords.contains(word)) {
                    // 如果是关键字, 就进行关键字的着色, 否则使用普通的着色.
                    // 这里有一点要注意, 在insertUpdate和removeUpdate的方法调用的过程中, 不能修改doc的属性.
                    // 但我们又要达到能够修改doc的属性, 所以把此任务放到这个方法的外面去执行.
                    // 实现这一目的, 可以使用新线程, 但放到swing的事件队列里去处理更轻便一点.
                    SwingUtilities.invokeLater(new ColoringTask(doc, pos, wordEnd - pos, keywordStyle));
                } else{
                    SwingUtilities.invokeLater(new ColoringTask(doc, pos, wordEnd - pos, normalStyle));
                }

                return wordEnd;
            }

            /**
             * 取得在文档中下标在pos处的字符.
             *
             * 如果pos为doc.getLength(), 返回的是一个文档的结束符, 不会抛出异常. 如果pos<0, 则会抛出异常.
             * 所以pos的有效值是[0, doc.getLength()]
             *
             * @param doc
             * @param pos
             * @return
             * @throws javax.swing.text.BadLocationException
             */
            public char getCharAt(Document doc, int pos) throws BadLocationException {
                return doc.getText(pos, 1).charAt(0);
            }

            /**
             * 取得下标为pos时, 它所在的单词开始的下标. Â±wor^dÂ± (^表示pos, Â±表示开始或结束的下标)
             *
             * @param doc
             * @param pos
             * @return
             * @throws javax.swing.text.BadLocationException
             */
            public int indexOfWordStart(Document doc, int pos) throws BadLocationException {
                // 从pos开始向前找到第一个非单词字符.
                for (; pos > 0 && isWordCharacter(doc, pos - 1); --pos);

                return pos;
            }

            /**
             * 取得下标为pos时, 它所在的单词结束的下标. Â±wor^dÂ± (^表示pos, Â±表示开始或结束的下标)
             *
             * @param doc
             * @param pos
             * @return
             * @throws javax.swing.text.BadLocationException
             */
            public int indexOfWordEnd(Document doc, int pos) throws BadLocationException {
                // 从pos开始向前找到第一个非单词字符.
                for (; isWordCharacter(doc, pos); ++pos);

                return pos;
            }

            /**
             * 如果一个字符是字母, 数字, 下划线, 则返回true.
             *
             * @param doc
             * @param pos
             * @return
             * @throws javax.swing.text.BadLocationException
             */
            public boolean isWordCharacter(Document doc, int pos) throws BadLocationException {
                char ch = getCharAt(doc, pos);
                if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_') { return true; }
                return false;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    coloring((StyledDocument) e.getDocument(), e.getOffset(), e.getLength());
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    // 因为删除后光标紧接着影响的单词两边, 所以长度就不需要了
                    coloring((StyledDocument) e.getDocument(), e.getOffset(), 0);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }

            /**
             * 完成着色任务
             *
             * @author Biao
             *
             */
            private class ColoringTask implements Runnable {
                private StyledDocument doc;
                private Style style;
                private int pos;
                private int len;

                public ColoringTask(StyledDocument doc, int pos, int len, Style style) {
                    this.doc = doc;
                    this.pos = pos;
                    this.len = len;
                    this.style = style;
                }

                public void run() {
                    try {
                        // 这里就是对字符进行着色
                        doc.setCharacterAttributes(pos, len, style, true);
                    } catch (Exception e) {}
                }
            }

    }

    /**
     * This class will display line numbers for a related text component. The text
     * component must use the same line height for each line. UI.TextLineNumber
     * supports wrapped lines and will highlight the line number of the current line
     * in the text component.
     *
     * This class was designed to be used as a component added to the row header of
     * a JScrollPane.
     */
    public static class TextLineNumber extends JPanel implements CaretListener,
            DocumentListener, PropertyChangeListener {

        private static final long serialVersionUID = 1L;
        public final static float LEFT = 0.0f;
        public final static float CENTER = 0.5f;
        public final static float RIGHT = 0.5f;

        private final static Border OUTER = new MatteBorder(0, 0, 0, 1, new Color(
                236, 235, 235));
        private final static int HEIGHT = Integer.MAX_VALUE - 1000000;

        // Text component this TextTextLineNumber component is in sync with
        private JTextComponent component;

        // Properties that can be changed
        private boolean updateFont;
        private int borderGap;
        private Color currentLineForeground;
        private float digitAlignment;
        private int minimumDisplayDigits;

        // Keep history information to reduce the number of times the component
        // needs to be repainted
        private int lastDigits;
        private int lastHeight;
        private int lastLine;
        private HashMap<String, FontMetrics> fonts;

        /**
         * Create a line number component for a text component. This minimum display
         * width will be based on 3 digits.
         *
         * @param component
         *            the related text component
         */
        public TextLineNumber(JTextComponent component) {
            this(component, 3);
        }

        /**
         * Create a line number component for a text component.
         *
         * @param component
         *            the related text component
         * @param minimumDisplayDigits
         *            the number of digits used to calculate the minimum width of
         *            the component
         */
        public TextLineNumber(JTextComponent component, int minimumDisplayDigits) {
            this.component = component;

            setFont(component.getFont());
            setForeground(new Color(120, 120, 120));
            setBackground(Color.white);
            setBorderGap(5);
            setCurrentLineForeground(Color.magenta);
            setDigitAlignment(RIGHT);
            setMinimumDisplayDigits(minimumDisplayDigits);

            component.getDocument().addDocumentListener(this);
            component.addCaretListener(this);
            component.addPropertyChangeListener("font",
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent evt) {
                            if (evt.getNewValue() instanceof Font) {
                                Font newFont = (Font) evt.getNewValue();
                                setFont(newFont);
                                lastDigits = 0;
                                setPreferredWidth();
                            }
                        }
                    });
        }

        /**
         * Gets the update font property
         *
         * @return the update font property
         */
        public boolean getUpdateFont() {
            return updateFont;
        }

        /**
         * Set the update font property. Indicates whether this Font should be
         * updated automatically when the Font of the related text component is
         * changed.
         *
         * @param updateFont
         *            when true update the Font and repaint the line numbers,
         *            otherwise just repaint the line numbers.
         */
        public void setUpdateFont(boolean updateFont) {
            this.updateFont = updateFont;
        }

        /**
         * Gets the border gap
         *
         * @return the border gap in pixels
         */
        public int getBorderGap() {
            return borderGap;
        }

        /**
         * The border gap is used in calculating the left and right insets of the
         * border. Default value is 5.
         *
         * @param borderGap
         *            the gap in pixels
         */
        public void setBorderGap(int borderGap) {
            this.borderGap = borderGap;
            Border inner = new EmptyBorder(0, borderGap, 0, borderGap);
            setBorder(new CompoundBorder(OUTER, inner));
            lastDigits = 0;
            setPreferredWidth();
        }

        /**
         * Gets the current line rendering Color
         *
         * @return the Color used to render the current line number
         */
        public Color getCurrentLineForeground() {
            return currentLineForeground == null ? getForeground()
                    : currentLineForeground;
        }

        /**
         * The Color used to render the current line digits. Default is Coolor.RED.
         *
         * @param currentLineForeground
         *            the Color used to render the current line
         */
        public void setCurrentLineForeground(Color currentLineForeground) {
            this.currentLineForeground = currentLineForeground;
        }

        /**
         * Gets the digit alignment
         *
         * @return the alignment of the painted digits
         */
        public float getDigitAlignment() {
            return digitAlignment;
        }

        /**
         * Specify the horizontal alignment of the digits within the component.
         * Common values would be:
         * <ul>
         * <li>UI.TextLineNumber.LEFT
         * <li>UI.TextLineNumber.CENTER
         * <li>UI.TextLineNumber.RIGHT (default)
         * </ul>
         *
         * @param currentLineForeground
         *            the Color used to render the current line
         */
        public void setDigitAlignment(float digitAlignment) {
            this.digitAlignment = digitAlignment > 1.0f ? 1.0f
                    : digitAlignment < 0.0f ? -1.0f : digitAlignment;
        }

        /**
         * Gets the minimum display digits
         *
         * @return the minimum display digits
         */
        public int getMinimumDisplayDigits() {
            return minimumDisplayDigits;
        }

        /**
         * Specify the minimum number of digits used to calculate the preferred
         * width of the component. Default is 3.
         *
         * @param minimumDisplayDigits
         *            the number digits used in the preferred width calculation
         */
        public void setMinimumDisplayDigits(int minimumDisplayDigits) {
            this.minimumDisplayDigits = minimumDisplayDigits;
            setPreferredWidth();
        }

        /**
         * Calculate the width needed to display the maximum line number
         */
        private void setPreferredWidth() {
            Element root = component.getDocument().getDefaultRootElement();
            int lines = root.getElementCount();
            int digits = Math.max(String.valueOf(lines).length(),
                    minimumDisplayDigits);

            // Update sizes when number of digits in the line number changes
            if (lastDigits != digits) {
                lastDigits = digits;
                FontMetrics fontMetrics = getFontMetrics(getFont());
                int width = fontMetrics.charWidth('0') * digits;
                Insets insets = getInsets();
                int preferredWidth = insets.left + insets.right + width;

                Dimension d = getPreferredSize();
                d.setSize(preferredWidth, HEIGHT);
                setPreferredSize(d);
                setSize(d);
            }
        }

        /**
         * Draw the line numbers
         *
         * @param g
         */
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            // Determine the width of the space available to draw the line number
            FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
            Insets insets = getInsets();
            int availableWidth = getSize().width - insets.left - insets.right;

            // Determine the rows to draw within the clipped bounds.
            Rectangle clip = g.getClipBounds();
            int rowStartOffset = component.viewToModel(new Point(0, clip.y));
            int endOffset = component
                    .viewToModel(new Point(0, clip.y + clip.height));

            while (rowStartOffset <= endOffset) {
                try {
                    if (isCurrentLine(rowStartOffset))
                        g.setColor(getCurrentLineForeground());
                    else
                        g.setColor(getForeground());

                    // Get the line number as a string and then determine the "X"
                    // and "Y" offsets for drawing the string.
                    String lineNumber = getTextLineNumber(rowStartOffset);
                    int stringWidth = fontMetrics.stringWidth(lineNumber);
                    int x = getOffsetX(availableWidth, stringWidth) + insets.left;
                    int y = getOffsetY(rowStartOffset, fontMetrics);
                    g.drawString(lineNumber, x, y);

                    // Move to the next row
                    rowStartOffset = Utilities.getRowEnd(component, rowStartOffset) + 1;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /**
         * We need to know if the caret is currently positioned on the line we are
         * about to paint so the line number can be highlighted.
         *
         * @param rowStartOffset
         */
        private boolean isCurrentLine(int rowStartOffset) {
            int caretPosition = component.getCaretPosition();
            Element root = component.getDocument().getDefaultRootElement();

            if (root.getElementIndex(rowStartOffset) == root
                    .getElementIndex(caretPosition))
                return true;
            else
                return false;
        }

        /**
         * Get the line number to be drawn. The empty string will be returned when a
         * line of text has wrapped.
         *
         * @param rowStartOffset
         */
        protected String getTextLineNumber(int rowStartOffset) {
            Element root = component.getDocument().getDefaultRootElement();
            int index = root.getElementIndex(rowStartOffset);
            Element line = root.getElement(index);

            if (line.getStartOffset() == rowStartOffset)
                return String.valueOf(index + 1);
            else
                return "";
        }

        /**
         * Determine the X offset to properly align the line number when drawn
         *
         * @param availableWidth
         * @param stringWidth
         */
        private int getOffsetX(int availableWidth, int stringWidth) {
            return (int) ((availableWidth - stringWidth) * digitAlignment);
        }

        /**
         * Determine the Y offset for the current row
         *
         * @param rowStartOffset
         * @param fontMetrics
         */
        private int getOffsetY(int rowStartOffset, FontMetrics fontMetrics)
                throws BadLocationException {
            // Get the bounding rectangle of the row
            Rectangle r = component.modelToView(rowStartOffset);
            int lineHeight = fontMetrics.getHeight();
            int y = r.y + r.height;
            int descent = 0;

            // The text needs to be positioned above the bottom of the bounding
            // rectangle based on the descent of the font(s) contained on the row.

            // default font is being used
            if (r.height == lineHeight) {
                descent = fontMetrics.getDescent();
            } else { // We need to check all the attributes for font changes
                if (fonts == null)
                    fonts = new HashMap<String, FontMetrics>();

                Element root = component.getDocument().getDefaultRootElement();
                int index = root.getElementIndex(rowStartOffset);
                Element line = root.getElement(index);

                for (int i = 0; i < line.getElementCount(); i++) {
                    Element child = line.getElement(i);
                    AttributeSet as = child.getAttributes();
                    String fontFamily = (String) as
                            .getAttribute(StyleConstants.FontFamily);
                    Integer fontSize = (Integer) as
                            .getAttribute(StyleConstants.FontSize);
                    String key = fontFamily + fontSize;

                    FontMetrics fm = fonts.get(key);

                    if (fm == null) {
                        Font font = new Font(fontFamily, Font.PLAIN, fontSize);
                        fm = component.getFontMetrics(font);
                        fonts.put(key, fm);
                    }

                    descent = Math.max(descent, fm.getDescent());
                }
            }

            return y - descent;
        }

        // Implement CaretListener interface
        public void caretUpdate(CaretEvent e) {
            // Get the line the caret is positioned on
            int caretPosition = component.getCaretPosition();
            Element root = component.getDocument().getDefaultRootElement();
            int currentLine = root.getElementIndex(caretPosition);

            // Need to repaint so the correct line number can be highlighted
            if (lastLine != currentLine) {
                repaint();
                lastLine = currentLine;
            } else if (currentLine == 0) {
                repaint();
                lastLine = currentLine;
            }
        }

        // Implement DocumentListener interface
        public void changedUpdate(DocumentEvent e) {
            documentChanged();
        }

        public void insertUpdate(DocumentEvent e) {
            documentChanged();
        }

        public void removeUpdate(DocumentEvent e) {
            documentChanged();
        }

        /**
         * A document change may affect the number of displayed lines of text.
         * Therefore the lines numbers will also change.
         */
        private void documentChanged() {
            // Preferred size of the component has not been updated at the time
            // the DocumentEvent is fired
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    int preferredHeight = component.getPreferredSize().height;

                    // Document change has caused a change in the number of lines.
                    // Repaint to reflect the new line numbers
                    if (lastHeight != preferredHeight) {
                        setPreferredWidth();
                        repaint();
                        lastHeight = preferredHeight;
                    }
                }
            });
        }

        // Implement PropertyChangeListener interface
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getNewValue() instanceof Font) {
                if (updateFont) {
                    Font newFont = (Font) evt.getNewValue();
                    setFont(newFont);
                    lastDigits = 0;
                    setPreferredWidth();
                } else {
                    repaint();
                }
            }
        }
    }
}
