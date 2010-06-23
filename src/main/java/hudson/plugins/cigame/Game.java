package hudson.plugins.cigame;

import hudson.model.Hudson;
import hudson.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

public abstract class Game {
    
    public abstract String getId();
        
    public abstract String getName();
    
    /**
     * Returns a list available games.
     */
    public List<Game> getGameChoices() {
        List<Game> choices = new ArrayList<Game>();
        choices.addAll(Hudson.getInstance().getDescriptorByType(GameDescriptor.class).getAllGames());
        Collections.sort(choices, new Comparator<Game>() {
            public int compare(Game o1, Game o2) {
                if (o1 instanceof GameDescriptor.DefaultGame) {
                    return -1;
                }
                else if (o2 instanceof GameDescriptor.DefaultGame) {
                    return 1;
                }
                else {
                    return o1.getName().compareTo(o2.getName());
                }
            }
        });
        
        return choices;
    }
    
    /**
     * Returns the user that are participants in this ci game
     * 
     * @return list containing users.
     */
    @Exported
    public List<UserScore> getUserScores() {
        return getUserScores(User.getAll(), Hudson.getInstance().getDescriptorByType(GameDescriptor.class).getNamesAreCaseSensitive());
    }

    List<UserScore> getUserScores(Collection<User> users, boolean usernameIsCasesensitive) {
        ArrayList<UserScore> list = new ArrayList<UserScore>();

        Collection<User> players;
        if (usernameIsCasesensitive) {
            players = users;
        } else {
            List<User> playerList = new ArrayList<User>();
            CaseInsensitiveUserIdComparator caseInsensitiveUserIdComparator = new CaseInsensitiveUserIdComparator();
            for (User user : users) {
                if (Collections.binarySearch(playerList, user, caseInsensitiveUserIdComparator) < 0) {
                    playerList.add(user);
                }
            }
            players = playerList;
        }
        
        for (User user : players) {
            UserScoreProperty property = user.getProperty(UserScoreProperty.class);
            if ((property != null) && property.isParticipatingInGame()) {
                Double score = property.getScoreForGame(this);
                if (score != null) {
                    list.add(new UserScore(user, score, user.getDescription()));
                }
            }
        }

        Collections.sort(list, new Comparator<UserScore>() {
            public int compare(UserScore o1, UserScore o2) {
                if (o1.score < o2.score)
                    return 1;
                if (o1.score > o2.score)
                    return -1;
                return 0;
            }
        });

        return list;
    }

    public void doResetScores( StaplerRequest req, StaplerResponse rsp ) throws IOException {
        if (Hudson.getInstance().getACL().hasPermission(Hudson.ADMINISTER)) {
            doResetScores(User.getAll());
        }
        rsp.sendRedirect2(req.getContextPath());
    }

    void doResetScores(Collection<User> users) throws IOException {
        for (User user : users) {
            UserScoreProperty property = user.getProperty(UserScoreProperty.class);
            if (property != null) {
                property.resetScoreForGame(this);
                user.save();
            }
        }
    }
    
    @ExportedBean(defaultVisibility = 999)
    public class UserScore {
        private User user;
        private double score;
        private String description;

        public UserScore(User user, double score, String description) {
            super();
            this.user = user;
            this.score = score;
            this.description = description;
        }

        @Exported
        public User getUser() {
            return user;
        }

        @Exported
        public double getScore() {
            return score;
        }

        @Exported
        public String getDescription() {
            return description;
        }
    }

    /**
     * Update the user's score for this game.
     * 
     * @param delta The change in score.
     * @param user The user to update.
     * @return true if the user was modified.
     */
    public boolean adjustScoreForUser(double delta, User user) throws IOException {
        UserScoreProperty prop = getUserScoreProperty(user);
        if (prop.isParticipatingInGame()) {
            prop.adjustScoreForGame(delta, this);
            return true;
        }
        
        return false;
    }

    private UserScoreProperty getUserScoreProperty(User user) throws IOException {
        UserScoreProperty property = user.getProperty(UserScoreProperty.class);
        if (property == null) {
            property = new UserScoreProperty();
            user.addProperty(property);
        }

        return property;
    }
}
