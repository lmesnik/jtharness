/*
 * $Id$
 *
 * Copyright (c) 2006, 2009, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.mrep;

import com.sun.javatest.tool.Desktop;
import com.sun.javatest.tool.Tool;
import com.sun.javatest.tool.ToolAction;
import com.sun.javatest.tool.ToolManager;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.Map;

public class ReportToolManager extends ToolManager {

    private ReportTool tool;

    public ReportToolManager(Desktop desktop) {
        super(desktop);
    }

    @Override
    public Tool startTool() {
        ReportTool t = getTool();

        Desktop d = getDesktop();
        if (!d.containsTool(t)) {
            d.addTool(t);
        }

        d.setSelectedTool(t);

        return t;
    }

    @Override
    public Action[] getWindowOpenMenuActions() {
        Action a = new ToolAction(i18n, "tmgr.openReport") {
            @Override
            public void actionPerformed(ActionEvent e) {
                startTool();
            }
        };
        return new Action[]{a};
    }

    @Override
    public Tool restoreTool(Map<String, String> m) {
        ReportTool t = getTool();
        t.restore(m);
        return t;
    }

    ReportTool getTool() {
        if (tool == null) {
            tool = new ReportTool(this, getDesktop());
            tool.addObserver(new Tool.Observer() {
                @Override
                public void shortTitleChanged(Tool t, String newValue) {
                }

                @Override
                public void titleChanged(Tool t, String newValue) {
                }

                @Override
                public void toolDisposed(Tool t) {
                    if (t == tool) {
                        tool = null;
                    }
                }
            });
        }

        return tool;
    }

}
