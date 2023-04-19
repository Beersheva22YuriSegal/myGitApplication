package telran.git.data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public class CommitFile implements Serializable {

	private static final long serialVersionUID = 1L;

	public String path;
	public String commitName;
	public List<String> content;
	public Instant modifiedTime;

	public CommitFile(String path, String commitName, List<String> content, Instant modifiedTime) {
		this.path = path;
		this.commitName = commitName;
		this.content = content;
		this.modifiedTime = modifiedTime;
	}
}