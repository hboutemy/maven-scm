package org.apache.maven.scm.provider.clearcase.command.status;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 */
public class ClearCaseStatusConsumerTest
    extends ScmTestCase
{
    public void testConsumer()
        throws IOException
    {
        InputStream inputStream = getResourceAsStream( "/clearcase/status/status.txt" );

        BufferedReader in = new BufferedReader( new InputStreamReader( inputStream ) );

        String s = in.readLine();

        ClearCaseStatusConsumer consumer = new ClearCaseStatusConsumer( new DefaultLog(), getWorkingDirectory() );

        while ( s != null )
        {
            consumer.consumeLine( s );

            s = in.readLine();
        }

        Collection entries = consumer.getCheckedOutFiles();

        assertEquals( "Wrong number of entries returned", 1, entries.size() );

        ScmFile scmFile = (ScmFile) entries.iterator().next();
        assertEquals( new File( getWorkingDirectory(), "test.java" ).getAbsolutePath(), scmFile.getPath() );
        assertEquals( ScmFileStatus.CHECKED_OUT, scmFile.getStatus() );
    }
}
