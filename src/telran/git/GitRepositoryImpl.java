package telran.git;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import telran.git.data.*;

public class GitRepositoryImpl implements GitRepository {

	private static final long serialVersionUID = 1L;

	private String head;
	private HashMap<String, Commit> commits = new HashMap<>();
	private HashMap<String, Branch> branches = new HashMap<>();
	private HashMap<String, CommitFile> commitFiles = new HashMap<>();
	private String ignoreExpressions = "(\\..*)";
	private Instant lastCommitTime;


	public static GitRepositoryImpl init() {
		File file = new File(GIT_FILE);
		GitRepositoryImpl res = new GitRepositoryImpl();
		if (file.exists()) {
			try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(file))) {
				res = (GitRepositoryImpl) input.readObject();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} 
		}
		return res;
	}


	@Override
	public String commit(String commitMessage) {
		if (commits.containsKey(head)) {
			return "We are on commit but must be on the branch to make commit";
		}
		if (info().isEmpty() || info().stream().allMatch(fs -> fs.status == Status.COMMITTED)) {
			return "Nothing to commit";
		}
		return head == null ? commitHeadNull(commitMessage) : commitHeadNotNull(commitMessage);
	}

	private String commitHeadNull(String commitMessage) {
		Commit commit = createCommit(commitMessage, null);
		createInnerBranch("master", commit);
		return "commit performed successfully";
	}

	private void createInnerBranch(String branchName, Commit commit) {
		if (branches.containsKey(branchName)) {
			throw new IllegalStateException(String.format("Branch %s is already exists", branchName));
		}
		Branch branch = new Branch();
		branch.branchName = branchName;
		branch.commitName = commit.commitName;
		branches.put(branchName, branch);
		head = branchName;

	}

	private Commit createCommit(String commitMessage, Commit prev) {
		Commit newCommit = new Commit();
		newCommit.commitName = getCommitName();
		newCommit.commitMessage = commitMessage;
		newCommit.prevCommit = prev;
		newCommit.commitTime = Instant.now();
		newCommit.commitFiles = getCommitFiles(newCommit.commitName);
		commits.put(newCommit.commitName, newCommit);
		lastCommitTime = newCommit.commitTime;

		return newCommit;
	}

	private List<CommitFile> getCommitFiles(String commitName) {
		List<FileState> files = info();
		return files.stream().filter(fs -> fs.status != Status.COMMITTED).map(fs -> toCommitFile(fs, commitName))
				.toList();
	}

	private CommitFile toCommitFile(FileState fs, String commitName) {
		Instant timeModified;
		try {
			timeModified = Files.getLastModifiedTime(fs.path).toInstant();
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
		List<String> fileContent = getFileContent(fs.path);
		CommitFile res = new CommitFile(fs.path.toString(), commitName, fileContent, timeModified);
		commitFiles.put(fs.path.toString(), res);
		return res;
	}

	private List<String> getFileContent(Path path) {
		try {
			return Files.lines(path).toList();
		} catch (IOException e) {
			throw new RuntimeException(e.toString());
		}
	}

	private String commitHeadNotNull(String commitMessage) {
		String res = null;
		Branch branch = branches.get(head);
		if (branch == null) {
			res = "must commit only to branch";
		} else {
			Commit commit = createCommit(commitMessage, commits.get(branch.commitName));
			branch.commitName = commit.commitName;
			res = "commit performed successfully";
		}
		return res;
	}

	private String getCommitName() {
		String res = "";
		do {
			res = UUID.randomUUID().toString().substring(0, 8);
		} while (commits.containsKey(res));
		return res;
	}

	@Override
	public List<FileState> info() {
		Path dirPath = Path.of(".");
		try {
			return Files.list(dirPath).map(p -> p.normalize()).filter(p -> !ignoreFilter(p)).map(p -> {
				try {
					return new FileState(p, getStatus(p));
				} catch (IOException e) {
					throw new RuntimeException(e.toString());
				}
			}).toList();
		} catch (IOException e) {
			return Collections.emptyList();
		}
	}

	private Status getStatus(Path path) throws IOException {
		CommitFile commitFile = commitFiles.get(path.toString());
		Status res;
		if (commitFile == null) {
			res = Status.UNTRACKED;
		} else {
			res = getStatusFromCommitFile(commitFile, path);
		}
		return res;
	}

	private Status getStatusFromCommitFile(CommitFile commitFile, Path path) throws IOException {
		return Files.getLastModifiedTime(path).toInstant().compareTo(lastCommitTime) > 0 ? Status.MODIFIED : Status.COMMITTED;
	}

	private boolean ignoreFilter(Path path) {
		return path.toString().matches(ignoreExpressions) || !Files.isRegularFile(path);
	}

	@Override
	public String createBranch(String branchName) {
		String res = null;
		if (commits.isEmpty()) {
			res = "Branch may be created only for existing commit";
		} else if (branches.containsKey(branchName)) {
			res = "Branch already exists";
		} else {
			Commit commit = getCommit();
			createInnerBranch(branchName, commit);
			res = "branch created successfully";
		}
		return res;
	}

	private Commit getCommit() {
		Branch branch = branches.get(head);
		String commitName = branch != null ? branch.commitName : head;
		Commit res = commits.get(commitName);
		if (res == null) {
			throw new IllegalStateException("no commit with the name " + commitName);
		}
		return res;
	}

	@Override
	public String renameBranch(String branchName, String newBranchName) {
		Branch branch = branches.get(branchName);
		String res = branchName + " doesn't exists";
		if (branch != null) {
			if (branches.containsKey(newBranchName)) {
				res = newBranchName + " is already exists";
			} else {
				branch.branchName = newBranchName;
				branches.remove(branchName);
				branches.put(newBranchName, branch);
				if (head.equals(branchName)) {
					head = newBranchName;
				}
				res = "branch renamed successfully";
			}
		}
		return res;
	}

	@Override
	public String deleteBranch(String branchName) {
		String res = "branch doesn't exists";
		if (branches.containsKey(branchName)) {
			if (head.equals(branchName)) {
				res = "this branch is active it couldn't be deleted";
			} else if (branches.size() == 1) {
				res = "should be at least one branch";
			} else {
				branches.remove(branchName);
				res = "branch deleted successfully";
			}
		}
		return res;
	}

	@Override
	public List<CommitMessage> log() {
		List<CommitMessage> res = new ArrayList<>();
		if (head != null) {
			Branch branch = branches.get(head);
			String commitName = branch != null ? branch.commitName : head;
			Commit commit = commits.get(commitName);
			if (commit == null) {
				throw new IllegalStateException("No commit with name " + commitName);
			}
			while (commit != null) {
				res.add(new CommitMessage(commit.commitName, commit.commitMessage));
				commit = commit.prevCommit;
			}
		}
		return res;
	}

	@Override
	public List<String> branches() {

		return branches.values().stream().map(bn -> {
			String res = bn.branchName;
			if (head.equals(res)) {
				res += "*";
			}
			return res;
		}).toList();
	}

	@Override
	public List<Path> commitContent(String commitName) {
		Commit commit = commits.get(commitName);
		if (commit == null) {
			throw new IllegalArgumentException(commitName + " doesn't exists");
		}
		return commit.commitFiles.stream().map(c -> Path.of(c.path)).toList();
	}

	@Override
	public String switchTo(String name) {
		List<FileState> fileStates = info();
		String res = String.format("Switched to %s", name);
		Commit commitTo = commitSwitched(name);
		Commit commitHead = getCommit();
		if (commitTo != null) {
			if (commitTo.commitName.equals(commitHead.commitName) || head.equals(name)) {
				res = name + " is the same commit as current";
			} else if (fileStates.stream().anyMatch(fs -> fs.status != Status.COMMITTED)) {
				res = "make commit before switching";
			} else {
				info().stream().forEach(fs -> {
					try {
						Files.delete(fs.path);
					} catch (IOException e) {
						throw new IllegalStateException(e.getMessage());
					}
				});
				switchProcess(commitTo);
				head = name;
				lastCommitTime = Instant.now();
			}
		}
		return res;
	}

	private void switchProcess(Commit commitTo) {
		Set<String> restoredFiles = new HashSet<>();
		try {
			while (commitTo != null) {
				commitTo.commitFiles.stream().forEach(cf -> {
					if (!restoredFiles.contains(cf.path)) {
						try (PrintWriter writer = new PrintWriter(cf.path)) {
							cf.content.stream().forEach(writer::println);
							Files.setLastModifiedTime(Path.of(cf.path), FileTime.from(cf.modifiedTime));
						} catch (Exception e) {
							throw new IllegalStateException(e.toString());
						}
						restoredFiles.add(cf.path);
					}
				});
				commitTo = commitTo.prevCommit;
			}
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	private Commit commitSwitched(String name) {
		Commit res = null;
		String commitName = name;
		Branch branch = branches.get(name);
		if (branch != null) {
			commitName = branch.commitName;
		}
		res = commits.get(commitName);
		if (res == null) {
			throw new IllegalArgumentException("no commit with name " + commitName);
		}
		return res;
	}

	@Override
	public String getHead() {
		String res = "There is no head";
		if (head != null) {
			res = branches.containsKey(head) ? "branch name " : "commit name ";
			res += head;
		}
		return res;
	}

	@Override
	public void save() {
		try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(GIT_FILE))) {
			output.writeObject(this);
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
	}

	@Override
	public String addIgnoredFileNameExp(String regex) {
		try {
			"test".matches(regex);
		} catch (Exception e) {
			throw new IllegalArgumentException(regex + " wrong regex");
		}
		ignoreExpressions += String.format("|(%s)", regex);
		return String.format("Regex for files ignored is %s", ignoreExpressions);
	}

}
