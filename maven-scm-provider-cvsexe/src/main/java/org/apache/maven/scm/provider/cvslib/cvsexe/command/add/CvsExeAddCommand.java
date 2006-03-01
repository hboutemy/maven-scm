package org.apache.maven.scm.provider.cvslib.cvsexe.command.add;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.cvslib.command.add.AbstractCvsAddCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsExeAddCommand
    extends AbstractCvsAddCommand
{
    protected AddScmResult executeCvsCommand( Commandline cl, List addedFiles )
        throws ScmException
    {
        CommandLineUtils.StringStreamConsumer consumer = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        // TODO: actually it may have partially succeeded - should we cvs update the files and parse "A " responses?
        if ( exitCode != 0 )
        {
            return new AddScmResult( cl.toString(), "The cvs command failed.", stderr.getOutput(), false );
        }

        return new AddScmResult( cl.toString(), addedFiles );
    }
}
