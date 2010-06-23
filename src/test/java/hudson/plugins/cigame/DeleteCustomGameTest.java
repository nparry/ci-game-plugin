package hudson.plugins.cigame;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import hudson.model.User;

import org.junit.Test;

public class DeleteCustomGameTest {
    
    @Test
    public void assertScoreIsErasedFromUserWhenGameIsRemoved() throws Exception {
        GameDescriptor descriptor = new GameDescriptor() {
            @Override public void load() {
                // force a no-op
            }
        };
        
        descriptor.getCustomGames().add(new CustomGame("id", "name", "jobs"));
        
        User userWithObsoleteGame = mock(User.class);
        UserScoreProperty property1 = new UserScoreProperty(2.0, true);
        property1.getCustomGameScores().put("id", 15d);
        property1.getCustomGameScores().put("id2", 15d);
        when(userWithObsoleteGame.getProperty(UserScoreProperty.class)).thenReturn(property1);
        
        User userWithoutObsoleteGame = mock(User.class);
        UserScoreProperty property2 = new UserScoreProperty(2.0, true);
        property2.getCustomGameScores().put("id", 15d);
        when(userWithoutObsoleteGame.getProperty(UserScoreProperty.class)).thenReturn(property2);
        
        descriptor.removeObsoleteGameScores(Arrays.asList(userWithObsoleteGame, userWithoutObsoleteGame));
        
        assertThat(property1.getCustomGameScores().containsKey("id"), is(true));
        assertThat(property1.getCustomGameScores().containsKey("id2"), is(false));
        assertThat(property2.getCustomGameScores().containsKey("id"), is(true));
        
        verify(userWithoutObsoleteGame).getProperty(UserScoreProperty.class);
        verifyNoMoreInteractions(userWithoutObsoleteGame);
        verify(userWithObsoleteGame).getProperty(UserScoreProperty.class);
        verify(userWithObsoleteGame).save();
        verifyNoMoreInteractions(userWithObsoleteGame);
    }
}
