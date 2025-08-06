package com.ms4systems.devs.core.util;


import java.io.*;
import javax.swing.*;

public class fileHandler {

    static public String projectPath =
            "C:/DDEProjWDEVS3.1NFDDEVS/src/";
    public fileHandler() {
    }

// Pop-up menu to choose a file within the folder path
    public static String chooseFileString(String path) {
        File file = null;
// create a file-chooser, to be used below
        JFileChooser chooser = new JFileChooser(new File(path));
        // keep doing this
        while (true) {
            // pop up the file chooser so the user may select which
            // files to convert
            int result = chooser.showOpenDialog(null);
            // if the user clicked 'ok' in the file chooser
            if (result == JFileChooser.APPROVE_OPTION) {
                file = chooser.getSelectedFile();
            }
            if (file != null) {
                break;
            }
        }
        return file.getPath();
    }

// Pop-up menu for current directory
    public static String chooseFileString() {
        return chooseFileString(".");
    }

// Gets file qua File with name fileString
    public static File getFile(String fileString) {
        return new File(fileString);
    }

// Gets file from chosen fileString
    public static File getChooseFile(String fileString) {
        return getFile(chooseFileString(fileString));
    }

    public static void writeToFile(File file, String text) {
        if (queryMode) {
            if (file.exists()) {
                javax.swing.JPanel pan = new javax.swing.JPanel();
                javax.swing.JOptionPane p = new javax.swing.JOptionPane();
                int res = p.showConfirmDialog(pan,
                        "Write over " + file.getPath() + "?");
                if (p.NO_OPTION == res) {
                    return;
                } else if (p.CANCEL_OPTION == res) {
                    System.exit(3);
                }
            }
        }
        //by saurabh 1/18/08
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            OutputStream stream = new FileOutputStream(file);
            stream.write(text.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static boolean queryMode = false;

    public static void writeToCleanFile(File file, String text) {
        if (file.exists()) {
            if (queryMode) {
                javax.swing.JPanel pan = new javax.swing.JPanel();
                javax.swing.JOptionPane p = new javax.swing.JOptionPane();
                int res = p.showConfirmDialog(pan,
                        "Write over " + file.getPath() + "?");
                if (p.NO_OPTION == res) {
                    return;
                } else if (p.CANCEL_OPTION == res) {
                    System.exit(3);
                }
            }
        }
        try {
            OutputStream stream = new FileOutputStream(file);
           text = cleanfiletext(text);
            stream.write(text.getBytes());
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String cleanfiletext(String text) {
        text = text.replaceAll("\n", "");
        text = text.replaceAll("\t", "");
        text = text.replaceAll(">", ">\n");
        while (text.indexOf(" >") != -1) {
            text = text.replaceAll(" >", ">");
        }
        while (text.indexOf("> ") != -1) {
            text = text.replaceAll("> ", ">");
        }
        while (text.indexOf("< ") != -1) {
            text = text.replaceAll("< ", "<");
        }
        while (text.indexOf(" <") != -1) {
            text = text.replaceAll(" <", "<");
        }
        text = addtabs(text);
        return text;
    }

    public static String addtabs(String text) {
        int tabcount = -1;
        String textlines[] = text.split("\n", 0);
        if (textlines.length < 3) {
            return text;
        }//bpz
        text = textlines[0] + "\n" + textlines[1] + "\n" + textlines[2] + "\n";
        int numlines = textlines.length;
        for (int i = 3; i < numlines; i++) {
            String spaces = "";
            if (!textlines[i].startsWith("</") && !textlines[i].endsWith("/>")) {
                tabcount++;
            }
            for (int tabs = 0; tabs < tabcount; tabs++) {
                spaces = spaces + "\t";
            }
            if (textlines[i].startsWith("</")) {
                tabcount--;
            }
            textlines[i] = spaces + textlines[i];
            text = text + textlines[i] + "\n";
        }
        return text;
    }

    public static void writeToFile(String fileString, String text) {
        writeToFile(getFile(fileString), text);
    }

    public static void writeToCleanFile(String fileString, String text) {
        writeToCleanFile(getFile(fileString), text);
    }

    public static void writeToChooseFile(String fileString, String text) {
        writeToFile(getChooseFile(fileString), text);
    }

    public static void writeToChooseFile(String text) {
        writeToChooseFile(".", text);
    }

    static public String getContentsAsString(File file) {
          if (!file.exists()) {
              System.out.println("File does not exist: "
                      +file);
          }
        try {
            StringBuffer buffer = new StringBuffer();
            BufferedReader in = new BufferedReader(new FileReader(file));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                buffer.append(line);
                buffer.append("\n");
            }

            return buffer.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static public String getContentsAsString(String fileString) {
        return getContentsAsString(getFile(fileString));
    }

// Returns file contents as string
    public static String readFromFile(File file) {
        return getContentsAsString(file);
    }

// UI for readFromFile(File)
    public static String readFromFile(String fileString) {
        return readFromFile(getFile(fileString));
    }

// UI for readFromFile(ChooseFile)
    public static String readFromChooseFile(String fileString) {
        return readFromFile(getChooseFile(fileString));
    }

// Read from chosen file in current directory
    public static String readFromChooseFile() {
        return readFromChooseFile(".");
    }

        public static class forInstFilter implements FileFilter {

        public boolean accept(File f) {
            String name = f.getName();
            return name.contains("inst") || name.contains("Inst");
        }
    }
        
// Pop-up to choose multiple files at a time (CTRL+click to select multiple)
    public static File[] chooseMultiFileString(String path) {
        File[] files = {};
// create a file-chooser, to be used below
        JFileChooser chooser = new JFileChooser(new File(path));
        chooser.setMultiSelectionEnabled(true);
        FileFilter filter = new forInstFilter();   
    
        
        // keep doing this
        while (true) {
            // pop up the file chooser so the user may select which
            // files to convert
            int result = chooser.showOpenDialog(null);
            // if the user clicked 'ok' in the file chooser
            if (result == JFileChooser.APPROVE_OPTION) {
                // for each file selected by the user
                files = chooser.getSelectedFiles();
            }
            if (files != null) {
                break;
            }
        }
        return files;
    }

// Retrieves contents of fileString, appends moreStuff, and stores back to file
    public static void appendToFile(String fileString, String moreStuff) {
        String fileContents = "";
        File file = new File(fileString);
        if (file.exists()) {
            fileContents = readFromFile(fileString);
        }

        fileContents += "\n" + moreStuff;
        writeToFile(fileString, fileContents);
    }

    public static void copyFile(String fromFileName, String fromFolder,String toFolder) {
        String contents = readFromFile(fromFileName);
        writeToFile(toFolder+fromFileName, contents);
    }
}
