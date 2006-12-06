package org.apache.maven.scm.provider.bazaar.command.checkin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;
import org.apache.maven.scm.provider.bazaar.command.status.BazaarStatusCommand;
import org.apache.maven.scm.provider.bazaar.repository.BazaarScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a>
 */
public class BazaarCheckInCommand
    extends AbstractCheckInCommand
{

    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      String tag )
        throws ScmException
    {

        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "This provider can't handle tags." );
        }

        // Get files that will be committed (if not specified in fileSet)
        List commitedFiles = new ArrayList();
        File[] files = fileSet.getFiles();
        if ( files.length == 0 )
        { //Either commit all changes
            BazaarStatusCommand statusCmd = new BazaarStatusCommand();
            statusCmd.setLogger( getLogger() );
            StatusScmResult status = statusCmd.executeStatusCommand( repo, fileSet );
            List statusFiles = status.getChangedFiles();
            for ( Iterator it = statusFiles.iterator(); it.hasNext(); )
            {
                ScmFile file = (ScmFile) it.next();
                if ( file.getStatus() == ScmFileStatus.ADDED || file.getStatus() == ScmFileStatus.DELETED ||
                    file.getStatus() == ScmFileStatus.MODIFIED )
                {
                    commitedFiles.add( new ScmFile( file.getPath(), ScmFileStatus.CHECKED_IN ) );
                }
            }

        }
        else
        { //Or commit spesific files
            for ( int i = 0; i < files.length; i++ )
            {
                commitedFiles.add( new ScmFile( files[i].getPath(), ScmFileStatus.CHECKED_IN ) );
            }
        }

        // Commit to local branch
        String[] commitCmd = new String[]{BazaarCommand.COMMIT_CMD, BazaarCommand.MESSAGE_OPTION, message};
        commitCmd = BazaarUtils.expandCommandLine( commitCmd, fileSet );
        ScmResult result =
            BazaarUtils.execute( new BazaarConsumer( getLogger() ), getLogger(), fileSet.getBasedir(), commitCmd );

        // Push to parent branch if any
        BazaarScmProviderRepository repository = (BazaarScmProviderRepository) repo;
        if ( !repository.getURI().equals( fileSet.getBasedir().getAbsolutePath() ) )
        {
            String[] push_cmd = new String[]{BazaarCommand.PUSH_CMD, repository.getURI()};
            result =
                BazaarUtils.execute( new BazaarConsumer( getLogger() ), getLogger(), fileSet.getBasedir(), push_cmd );
        }

        return new CheckInScmResult( commitedFiles, result );
    }
}
