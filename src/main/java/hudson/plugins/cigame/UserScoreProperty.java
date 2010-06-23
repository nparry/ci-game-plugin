package hudson.plugins.cigame;

import hudson.model.User;
import hudson.model.UserProperty;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.collections.map.DefaultedMap;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * 
 * @author Erik Ramfelt
 */
@ExportedBean(defaultVisibility = 999)
public class UserScoreProperty extends UserProperty {

    private double score;
    private Map<String, Double> customGameScores;
    
    /** Inversed name as default value is false when serializing from data that
     * has doesnt have the value. */
    private boolean isNotParticipatingInGame;

    public UserScoreProperty() {
        score = 0;
        isNotParticipatingInGame = false;
    }
    
    @DataBoundConstructor
    public UserScoreProperty(double score, boolean participatingInGame) {
        this.score = score;
        this.isNotParticipatingInGame = !participatingInGame;
    }

    @Exported
    public User getUser() {
        return user;
    }

    @Exported
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Exported
    public boolean isParticipatingInGame() {
        return !isNotParticipatingInGame;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Double> getCustomGameScores() {
        if (customGameScores == null) {
            customGameScores = DefaultedMap.decorate(new HashMap<String, Double>(), 0d);
        }
        
        return customGameScores;
    }
    
    public void adjustScoreForGame(double delta, Game game) {
        if (game instanceof GameDescriptor.DefaultGame) {
            // This lets us stay backwards and forwards compatible with older
            // persisted data
            score += delta;
        }
        else {
            Map<String, Double> scores = getCustomGameScores();
            double newScore = scores.get(game.getId()) + delta;
            scores.put(game.getId(), newScore);
        }
    }
    
    /**
     * Return the score for a given game, or null if the user has no score
     * recorded for the game.
     */
    public Double getScoreForGame(Game game) {
        if (game instanceof GameDescriptor.DefaultGame) {
            return getScore();
        }
        
        Map<String, Double> scores = getCustomGameScores();
        if (scores.containsKey(game.getId())) {
            return scores.get(game.getId());
        }
        
        return null;
    }
    
    public void resetScoreForGame(Game game) {
        if (game instanceof GameDescriptor.DefaultGame) {
            setScore(0);
        }
        
        Map<String, Double> scores = getCustomGameScores();
        if (scores.containsKey(game.getId())) {
            scores.put(game.getId(), 0d);
        }
    }
    
    public boolean removeObsoleteGameScores(Set<String> remainingGameIds) {
        boolean changed = false;
        Iterator<Entry<String, Double>> iter = getCustomGameScores().entrySet().iterator();
        while (iter.hasNext()) {
            if (!remainingGameIds.contains(iter.next().getKey())) {
                iter.remove();
                changed = true;
            }
        }
        
        return changed;
    }
    
    @Override
    public String toString() {
        return String.format("UserScoreProperty [isNotParticipatingInGame=%s, score=%s, user=%s]", isNotParticipatingInGame, score, user); //$NON-NLS-1$
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
            + ((customGameScores == null) ? 0 : customGameScores.hashCode());
        result = prime * result + (isNotParticipatingInGame ? 1231 : 1237);
        long temp;
        temp = Double.doubleToLongBits(score);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof UserScoreProperty))
            return false;
        UserScoreProperty other = (UserScoreProperty) obj;
        if (customGameScores == null) {
            if (other.customGameScores != null)
                return false;
        } else if (!customGameScores.equals(other.customGameScores))
            return false;
        if (isNotParticipatingInGame != other.isNotParticipatingInGame)
            return false;
        if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
            return false;
        return true;
    }
}
