package hudson.plugins.cigame;

import hudson.model.AbstractProject;
import hudson.model.Items;

import java.util.List;
import java.util.UUID;

import org.kohsuke.stapler.DataBoundConstructor;

public class CustomGame extends Game {
  
    private String id;
    private String name;
    private String jobNames;
    
    @DataBoundConstructor
    public CustomGame(String id, String name, String jobNames) {
        this.id = (id != null && !"".equals(id))?
                id : UUID.randomUUID().toString();

        this.name = name;
        this.jobNames = jobNames;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getJobNames() {
        return jobNames;
    }
    
    private List<AbstractProject> getJobs() {
        return Items.fromNameList(jobNames, AbstractProject.class);
    }
    
    public boolean isApplicable(AbstractProject job) {
        return getJobs().contains(job);
    }
}
