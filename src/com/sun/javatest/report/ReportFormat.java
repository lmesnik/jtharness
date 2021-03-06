/*
 * $Id$
 *
 * Copyright (c) 2004, 2012, Oracle and/or its affiliates. All rights reserved.
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
package com.sun.javatest.report;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Interface for a report output format.
 */
interface ReportFormat {
    /**
     * @param s   Settings to use to create the report.
     * @param dir Directory in which the report output should be written.
     * @return ReportLink object for the report file (used in report index)
     */
    ReportLink write(ReportSettings s, File dir) throws IOException;

    String getReportID();

    String getBaseDirName();

    String getTypeName();

    boolean acceptSettings(ReportSettings s);

    List<ReportFormat> getSubReports();

    class ReportLink {
        public final String linkText;
        public final String linkID;
        public final File linkFile;
        public final String linkDesk;

        ReportLink(String name, String id, String desc, File f) {
            linkText = name;
            linkID = id;
            linkFile = f;
            linkDesk = desc;
        }

        @Override
        public String toString() {
            return "ReportLink{" +
                    "linkText='" + linkText + '\'' +
                    ", linkID='" + linkID + '\'' +
                    ", linkFile=" + linkFile +
                    ", linkDesk='" + linkDesk + '\'' +
                    '}';
        }
    }

}
