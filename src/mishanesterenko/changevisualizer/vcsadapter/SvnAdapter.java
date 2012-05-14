package mishanesterenko.changevisualizer.vcsadapter;

import java.io.ByteArrayOutputStream;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import mishanesterenko.changevisualizer.projectmodel.CustomProject;

/**
 * @author Michael Nesterenko
 *
 */
public class SvnAdapter extends AbstractAdapter {
    private SVNRepository repository;

    public SvnAdapter(final CustomProject project) throws SVNException {
        super(project);
        init();
    }

    protected void init() throws SVNException {
        repository = SVNRepositoryFactory.create(SVNURL.parseURIDecoded(getProject().getRepositoryLocation()));
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(getProject().getUsername(),
                getProject().getUserPassword());
        repository.setAuthenticationManager(authManager);
    }

    public void close() {
        if (repository != null) {
            repository.closeSession();
            repository = null;
        }
    }

    public String loadTextFile(final String path, final long revision) throws SVNException {
        SVNProperties props = new SVNProperties();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        repository.getFile(path, revision, props, baos);
        return baos.toString();
    }
}
