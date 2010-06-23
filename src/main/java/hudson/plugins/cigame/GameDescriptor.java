package hudson.plugins.cigame;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.User;
import hudson.plugins.cigame.model.RuleBook;
import hudson.plugins.cigame.model.RuleSet;
import hudson.plugins.cigame.rules.build.BuildRuleSet;
import hudson.plugins.cigame.rules.plugins.checkstyle.CheckstyleRuleSet;
import hudson.plugins.cigame.rules.plugins.findbugs.FindBugsRuleSet;
import hudson.plugins.cigame.rules.plugins.opentasks.OpenTasksRuleSet;
import hudson.plugins.cigame.rules.plugins.pmd.PmdRuleSet;
import hudson.plugins.cigame.rules.plugins.violation.ViolationsRuleSet;
import hudson.plugins.cigame.rules.plugins.warnings.WarningsRuleSet;
import hudson.plugins.cigame.rules.unittesting.UnitTestingRuleSet;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

@Extension
public class GameDescriptor extends BuildStepDescriptor<Publisher> {

    public static final String ACTION_LOGO_LARGE = "/plugin/ci-game/icons/game-32x32.png"; //$NON-NLS-1$
    public static final String ACTION_LOGO_MEDIUM = "/plugin/ci-game/icons/game-22x22.png"; //$NON-NLS-1$
    
    private transient RuleBook rulebook;
    private boolean namesAreCaseSensitive = true;
    private List<CustomGame> customGames;

    public GameDescriptor() {
        super(GamePublisher.class);
        load();
    }

    /**
     * Returns the default rule book
     * 
     * @return the rule book that is configured for the game.
     */
    public RuleBook getRuleBook() {
        if (rulebook == null) {
            rulebook = new RuleBook();

            addRuleSetIfAvailable(rulebook, new BuildRuleSet());
            addRuleSetIfAvailable(rulebook, new UnitTestingRuleSet());
            addRuleSetIfAvailable(rulebook, new OpenTasksRuleSet());
            addRuleSetIfAvailable(rulebook, new ViolationsRuleSet());
            addRuleSetIfAvailable(rulebook, new PmdRuleSet());
            addRuleSetIfAvailable(rulebook, new FindBugsRuleSet());
            addRuleSetIfAvailable(rulebook, new WarningsRuleSet());
            addRuleSetIfAvailable(rulebook, new CheckstyleRuleSet());
        }
        return rulebook;
    }

    private void addRuleSetIfAvailable(RuleBook book, RuleSet ruleSet) {
        if (ruleSet.isAvailable()) {
            book.addRuleSet(ruleSet);
        }
    }

    @Override
    public String getDisplayName() {
        return Messages.Plugin_Title();
    }

    @Override
    public GamePublisher newInstance(StaplerRequest req, JSONObject formData)
            throws hudson.model.Descriptor.FormException {
        return new GamePublisher();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
        req.bindJSON(this, json);
        save();
        removeObsoleteGameScores();
        return true;
    }

    public boolean getNamesAreCaseSensitive() {
        return namesAreCaseSensitive;
    }

    public void setNamesAreCaseSensitive(boolean namesAreCaseSensitive) {
        this.namesAreCaseSensitive = namesAreCaseSensitive;
    }
    
    public List<CustomGame> getCustomGames() {
        if (customGames == null) {
            customGames = new ArrayList<CustomGame>();
        }
        return customGames;
    }
    
    public void setCustomGames(List<CustomGame> customGames) {
        this.customGames = customGames;
    }
    
    public Game getDefaultGame() {
        return new DefaultGame();
    }
    
    public List<Game> getAllGames() {
        List<Game> games = new ArrayList<Game>();
        games.add(getDefaultGame());
        games.addAll(getCustomGames());
        return games;
    }
    
    public List<Game> getApplicableGames(AbstractProject project) {
        List<Game> applicable = new ArrayList<Game>();
        applicable.add(getDefaultGame());
        for (CustomGame customGame : getCustomGames()) {
            if (customGame.isApplicable(project)) {
                applicable.add(customGame);
            }
        }
        
        return applicable;
    }
    
    private void removeObsoleteGameScores() {
        removeObsoleteGameScores(User.getAll());
    }
    
    void removeObsoleteGameScores(Collection<User> users) {
        Set<String> currentGameIds = new HashSet<String>();
        for (Game game : getCustomGames()) {
            currentGameIds.add(game.getId());
        }
        
        for (User user : users) {
            UserScoreProperty property = user.getProperty(UserScoreProperty.class);
            if (property != null) {
                if (property.removeObsoleteGameScores(currentGameIds)) {
                    try {
                        user.save();
                    } catch (IOException e) {
                        // oh well, a user will have stale data
                    }
                }
            }
        }
    }
    
    /**
     * This lets the multi-game code handle data persisted from older versions
     * of the CI-game.
     */
    static class DefaultGame extends Game {
        public String getId() {
            return getName();
        }
        
        public String getName() {
            return "Default game";
        }
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> arg0) {
        return true;
    }
}
