/*
 * $Id$
 *
 * Copyright (c) 2002, 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.interview.wizard;

import com.sun.interview.DirectoryFileFilter;
import com.sun.interview.ExtensionFileFilter;
import com.sun.interview.FileFilter;
import com.sun.interview.FileQuestion;
import com.sun.interview.Question;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class FileQuestionRenderer
        implements QuestionRenderer {
    private static final I18NResourceBundle i18n = I18NResourceBundle.getDefaultBundle();

    /**
     * Create a chooser with the associated filters.
     */
    static JFileChooser createChooser(String title, FileFilter... filters) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(title);

        if (filters == null || filters.length == 0) {
            chooser.setAcceptAllFileFilterUsed(true);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        } else {
            int mode = -1;
            // removed to implement
            // add API to disable the all files filter perhaps?
            // chooser.setAcceptAllFileFilterUsed(false);

            for (FileFilter filter : filters) {
                chooser.addChoosableFileFilter(SwingFileFilter.wrap(filter));
                if (filter.acceptsDirectories()) {
                    if (mode == -1) {
                        // setting mode to DIRECTORIES_ONLY ignores the possibility
                        // that the filter might accept (some) files, so set it to
                        // FILES_AND_DIRECTORIES and leave to filter to hide any
                        // unacceptable files
                        // Same issue in FileListQuestionRenderer
                        mode = JFileChooser.FILES_AND_DIRECTORIES;
                    } else if (mode == JFileChooser.FILES_ONLY) {
                        mode = JFileChooser.FILES_AND_DIRECTORIES;
                    }
                } else {
                    if (mode == -1) {
                        mode = JFileChooser.FILES_ONLY;
                    } else if (mode == JFileChooser.DIRECTORIES_ONLY) {
                        mode = JFileChooser.FILES_AND_DIRECTORIES;
                    }
                }
            }
            chooser.setFileSelectionMode(mode);
        }

        return chooser;
    }

    @Override
    public JComponent getQuestionRendererComponent(Question qq, ActionListener listener) {
        final FileQuestion q = (FileQuestion) qq;
        final JFileChooser chooser = createChooser(q.getSummary(), q.getFilters());
        File f = q.getValue();
        if (f == null) {
            File dir = q.getBaseDirectory();
            if (dir == null) {
                dir = new File(System.getProperty("user.dir"));
            }
            chooser.setCurrentDirectory(dir);
        } else {
            chooser.setSelectedFile(f);
        }

        final JButton browseBtn = new JButton(i18n.getString("file.browse.btn"));
        browseBtn.setName("file.browse.btn");
        browseBtn.setMnemonic(i18n.getString("file.browse.mne").charAt(0));
        browseBtn.setToolTipText(i18n.getString("file.browse.tip"));

        File[] suggs = q.getSuggestions();
        String[] strSuggs;
        if (suggs == null) {
            strSuggs = null;
        } else {
            strSuggs = new String[suggs.length];
            for (int i = 0; i < suggs.length; i++) {
                strSuggs[i] = suggs[i].getPath();
            }
        }

        String fileType = "file";   // default
        if (isSelectingDir(q.getFilters())) {
            // response will be a dir, select special directory text
            fileType = "file.dir";
        }

        final TypeInPanel p = new TypeInPanel(fileType,
                q,
                0,
                strSuggs,
                browseBtn,
                listener);

        browseBtn.addActionListener(e -> {
            // default chooser to point at specified entry
            String s = p.getValue();
            if (s != null && !s.isEmpty()) {
                File f1 = new File(s);
                File baseDir = q.getBaseDirectory();
                if (!f1.isAbsolute() && baseDir != null) {
                    f1 = new File(baseDir, s);
                }
                chooser.setSelectedFile(f1);
            }

            int opt = chooser.showDialog(browseBtn, "Select");
            if (opt == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getPath();
                FileFilter ff = SwingFileFilter.unwrap(chooser.getFileFilter());
                if (ff != null && ff instanceof ExtensionFileFilter) {
                    ExtensionFileFilter eff = (ExtensionFileFilter) ff;
                    path = eff.ensureExtension(path);
                }
                File baseDir = q.getBaseDirectory();
                if (baseDir != null) {
                    String bp = baseDir.getPath();
                    if (path.startsWith(bp + File.separatorChar)) {
                        path = path.substring(bp.length() + 1);
                    }
                }
                p.setValue(path);
            }
        });

        return p;
    }

    @Override
    public String getInvalidValueMessage(Question q) {
        return null;
    }

    /**
     * Internal routine to help determine if we'll be selecting a dir or file.
     *
     * @return True if a dir (folder) will be the result.
     */
    private boolean isSelectingDir(FileFilter... filters) {
        if (filters == null || filters.length == 0) {
            return false;
        }

        boolean allDirType = true;
        for (FileFilter f : filters) {
            allDirType = allDirType && (f instanceof DirectoryFileFilter);
        }

        return allDirType;
    }
}
