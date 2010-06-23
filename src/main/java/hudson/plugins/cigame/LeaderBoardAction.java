package hudson.plugins.cigame;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.RootAction;
import hudson.security.ACL;
import hudson.security.AccessControlled;
import hudson.security.Permission;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerProxy;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Leader board for users participaing in the game.
 * 
 * @author Erik Ramfelt
 */
@ExportedBean(defaultVisibility = 999)
@Extension
public class LeaderBoardAction implements RootAction, AccessControlled, StaplerProxy {

    private static final long serialVersionUID = 1L;

    public String getDisplayName() {
        return Messages.Leaderboard_Title();
    }

    public String getIconFileName() {
        return GameDescriptor.ACTION_LOGO_MEDIUM;
    }

    public String getUrlName() {
        return "/cigame"; //$NON-NLS-1$
    }

    public ACL getACL() {
        return Hudson.getInstance().getACL();
    }

    public void checkPermission(Permission p) {
        getACL().checkPermission(p);
    }

    public boolean hasPermission(Permission p) {
        return getACL().hasPermission(p);
    }

    public Object getTarget() {
        // If we are not looking for any specific game, return the default
        if (Stapler.getCurrentRequest().getRestOfPath().isEmpty()) {
            return Hudson.getInstance().getDescriptorByType(GameDescriptor.class).getDefaultGame();
        }
        
        return this;
    }
    
    public Game getGameById(String id) {
        for (Game game : Hudson.getInstance().getDescriptorByType(GameDescriptor.class).getAllGames()) {
            if (game.getId().equals(id)) {
                return game;
            }
        }

        return null;
    }
}
