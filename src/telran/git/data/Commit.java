package telran.git.data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class Commit implements Serializable {

	private static final long serialVersionUID = 1L;

	public String commitName;
	public String commitMessage;
	public List<CommitFile> commitFiles;
	public Commit prevCommit;
	public Instant commitTime;

}